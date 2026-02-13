package com.bitcoin;

import java.util.Stack;
import java.util.List;

public class Interpreter {
    private final DataStack stack;
    private final Stack<ConditionalState> flowStack;
    private boolean trace = false;

    private static class ConditionalState {
        boolean conditionMatches;
        boolean parentWasExecuting;

        ConditionalState(boolean conditionMatches, boolean parentWasExecuting) {
            this.conditionMatches = conditionMatches;
            this.parentWasExecuting = parentWasExecuting;
        }

        boolean isExecuting() {
            return conditionMatches && parentWasExecuting;
        }
    }

    public Interpreter() {
        this.stack = new DataStack();
        this.flowStack = new Stack<>();
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public DataStack getStack() {
        return stack;
    }

    public boolean execute(Script script) {
        List<Object> operations = script.getOperations();

        for (Object op : operations) {
            if (trace) {
                System.out.println("Executing: " + op);
            }

            // Check execution state (Control Flow)
            boolean currentlyExecuting = isExecuting();

            if (op instanceof OpCode) {
                OpCode opcode = (OpCode) op;

                // Manejar control de flujo incluso si no se está ejecutando
                if (isControlOp(opcode)) {
                    opcode.execute(stack, this);
                } else if (currentlyExecuting) {
                    opcode.execute(stack, this);
                }

            } else if (op instanceof byte[]) {
                if (currentlyExecuting) {
                    stack.push((byte[]) op);
                }
            }

            if (trace) {
                System.out.println(stack);
            }
        }

        if (!flowStack.isEmpty()) {
            throw new RuntimeException("Condicional no balanceado: Falta ENDIF");
        }

        // El script es válido si la pila no está vacía y el tope es verdadero
        if (stack.isEmpty())
            return false;

        // In Bitcoin, "true" result means top element is non-zero
        // Our DataStack.popAsBool() handles the logic
        // We peek to not consume it? Typically execute returns boolean.
        // But verifying scriptSig + scriptPubKey usually involves leaving stack state.
        // For this project: "valid if ... top is true"
        // We'll peek.
        byte[] top = stack.peek();
        // Verificar si es verdadero (diferente de cero)
        boolean result = false;
        for (byte b : top) {
            if (b != 0) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean isExecuting() {
        return flowStack.isEmpty() || flowStack.peek().isExecuting();
    }

    // Manejadores de Control de Flujo ========================================= //

    private boolean isControlOp(OpCode op) {
        return op == OpCode.OP_IF || op == OpCode.OP_NOTIF ||
                op == OpCode.OP_ELSE || op == OpCode.OP_ENDIF;
    }

    public void handleIf() {
        boolean parentExecuting = isExecuting();
        if (parentExecuting) {
            boolean condition = stack.popAsBool();
            flowStack.push(new ConditionalState(condition, true));
        } else {
            flowStack.push(new ConditionalState(false, false));
        }
    }

    public void handleNotIf() {
        boolean parentExecuting = isExecuting();
        if (parentExecuting) {
            boolean condition = stack.popAsBool();
            flowStack.push(new ConditionalState(!condition, true));
        } else {
            flowStack.push(new ConditionalState(false, false));
        }
    }

    public void handleElse() {
        if (flowStack.isEmpty())
            throw new RuntimeException("OP_ELSE without OP_IF");
        ConditionalState state = flowStack.pop();
        if (state.parentWasExecuting) {
            flowStack.push(new ConditionalState(!state.conditionMatches, true));
        } else {
            flowStack.push(new ConditionalState(false, false));
        }
    }

    public void handleEndIf() {
        if (flowStack.isEmpty())
            throw new RuntimeException("OP_ENDIF without OP_IF");
        flowStack.pop();
    }
}
