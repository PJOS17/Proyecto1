package com.bitcoin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OpCodeTest {

    @Test
    public void testArithmetic() {
        Interpreter interpreter = new Interpreter();
        DataStack stack = interpreter.getStack();

        stack.pushLong(5);
        stack.pushLong(3);
        OpCode.OP_ADD.execute(stack, interpreter);
        assertEquals(8, stack.popAsLong());

        stack.pushLong(10);
        stack.pushLong(4);
        OpCode.OP_SUB.execute(stack, interpreter);
        assertEquals(6, stack.popAsLong());
    }

    @Test
    public void testComparison() {
        Interpreter interpreter = new Interpreter();
        DataStack stack = interpreter.getStack();

        stack.pushLong(5);
        stack.pushLong(10);
        OpCode.OP_LESSTHAN.execute(stack, interpreter); // 10 < 5 ? False.
        // Wait, stack order: 5 (bottom), 10 (top).
        // OP_LESSTHAN pops b (10), pops a (5).
        // checks a < b (5 < 10). True.
        assertTrue(stack.popAsBool());
    }

    @Test
    public void testLogic() {
        Interpreter interpreter = new Interpreter();
        DataStack stack = interpreter.getStack();

        stack.pushBool(true);
        stack.pushBool(false);
        OpCode.OP_BOOLAND.execute(stack, interpreter);
        assertFalse(stack.popAsBool());

        stack.pushBool(true);
        OpCode.OP_NOT.execute(stack, interpreter);
        assertFalse(stack.popAsBool());
    }

    @Test
    public void testStackOps() {
        Interpreter interpreter = new Interpreter();
        DataStack stack = interpreter.getStack();

        stack.pushLong(1);
        OpCode.OP_DUP.execute(stack, interpreter);
        assertEquals(2, stack.size());
        assertEquals(1, stack.popAsLong());
        assertEquals(1, stack.popAsLong());
    }
}
