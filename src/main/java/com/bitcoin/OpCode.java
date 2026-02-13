package com.bitcoin;

import java.util.Arrays;

public enum OpCode {
    // Constants
    OP_0(0x00) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.push(new byte[0]);
        }
    },
    OP_FALSE(0x00) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.push(new byte[0]);
        }
    },
    OP_TRUE(0x51) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(1);
        }
    },
    OP_1(0x51) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(1);
        }
    },
    OP_2(0x52) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(2);
        }
    },
    OP_3(0x53) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(3);
        }
    },
    OP_4(0x54) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(4);
        }
    },
    OP_5(0x55) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(5);
        }
    },
    OP_6(0x56) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(6);
        }
    },
    OP_7(0x57) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(7);
        }
    },
    OP_8(0x58) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(8);
        }
    },
    OP_9(0x59) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(9);
        }
    },
    OP_10(0x5a) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(10);
        }
    },
    OP_11(0x5b) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(11);
        }
    },
    OP_12(0x5c) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(12);
        }
    },
    OP_13(0x5d) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(13);
        }
    },
    OP_14(0x5e) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(14);
        }
    },
    OP_15(0x5f) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(15);
        }
    },
    OP_16(0x60) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pushLong(16);
        }
    },

    // Global
    OP_NOP(0x61) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            /* Do nothing */ }
    },
    OP_RETURN(0x6a) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            throw new RuntimeException("OP_RETURN encountered");
        }
    },

    // Pila
    OP_DUP(0x76) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.push(stack.peek().clone());
        }
    },
    OP_DROP(0x75) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.pop();
        }
    },
    OP_SWAP(0x7c) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            byte[] a = stack.pop();
            byte[] b = stack.pop();
            stack.push(a);
            stack.push(b);
        }
    },
    OP_OVER(0x78) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            byte[] a = stack.pop();
            byte[] b = stack.peek();
            stack.push(a);
            stack.push(b.clone());
        }
    },

    // Lógica
    OP_EQUAL(0x87) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            byte[] a = stack.pop();
            byte[] b = stack.pop();
            stack.pushBool(Arrays.equals(a, b));
        }
    },
    OP_EQUALVERIFY(0x88) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            byte[] a = stack.pop();
            byte[] b = stack.pop();
            if (!Arrays.equals(a, b))
                throw new RuntimeException("OP_EQUALVERIFY falló");
        }
    },
    OP_NOT(0x91) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            boolean val = stack.popAsBool();
            stack.pushBool(!val);
        }
    },
    OP_BOOLAND(0x9a) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            boolean a = stack.popAsBool();
            boolean b = stack.popAsBool();
            stack.pushBool(a && b);
        }
    },
    OP_BOOLOR(0x9b) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            boolean a = stack.popAsBool();
            boolean b = stack.popAsBool();
            stack.pushBool(a || b);
        }
    },
    OP_VERIFY(0x69) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            if (!stack.popAsBool())
                throw new RuntimeException("OP_VERIFY falló");
        }
    },

    // Aritmética
    OP_ADD(0x93) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            long b = stack.popAsLong();
            long a = stack.popAsLong();
            stack.pushLong(a + b);
        }
    },
    OP_SUB(0x94) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            long b = stack.popAsLong();
            long a = stack.popAsLong();
            stack.pushLong(a - b);
        }
    },
    OP_LESSTHAN(0x9f) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            long b = stack.popAsLong();
            long a = stack.popAsLong();
            stack.pushBool(a < b);
        }
    },
    OP_GREATERTHAN(0xa0) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            long b = stack.popAsLong();
            long a = stack.popAsLong();
            stack.pushBool(a > b);
        }
    },
    OP_LESSTHANOREQUAL(0xa1) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            long b = stack.popAsLong();
            long a = stack.popAsLong();
            stack.pushBool(a <= b);
        }
    },
    OP_GREATERTHANOREQUAL(0xa2) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            long b = stack.popAsLong();
            long a = stack.popAsLong();
            stack.pushBool(a >= b);
        }
    },
    OP_NUMEQUALVERIFY(0x9d) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            long b = stack.popAsLong();
            long a = stack.popAsLong();
            if (a != b)
                throw new RuntimeException("OP_NUMEQUALVERIFY falló");
        }
    },

    // Criptografía
    OP_HASH160(0xa9) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.push(Crypto.hash160(stack.pop()));
        }
    },
    OP_HASH256(0xaa) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            stack.push(Crypto.hash256(stack.pop()));
        }
    },
    OP_CHECKSIG(0xac) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            byte[] pubKey = stack.pop();
            byte[] sig = stack.pop();
            // Simulación: Asumimos firma válida si cumple criterios mock
            stack.pushBool(Crypto.checkSig(sig, pubKey, new byte[0]));
        }
    },
    OP_CHECKSIGVERIFY(0xad) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            byte[] pubKey = stack.pop();
            byte[] sig = stack.pop();
            if (!Crypto.checkSig(sig, pubKey, new byte[0])) {
                throw new RuntimeException("OP_CHECKSIGVERIFY falló");
            }
        }
    },

    // Control de Flujo
    OP_IF(0x63) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            interpreter.handleIf();
        }
    },
    OP_NOTIF(0x64) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            interpreter.handleNotIf();
        }
    },
    OP_ELSE(0x67) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            interpreter.handleElse();
        }
    },
    OP_ENDIF(0x68) {
        @Override
        public void execute(DataStack stack, Interpreter interpreter) {
            interpreter.handleEndIf();
        }
    };

    private final int value;

    OpCode(int value) {
        this.value = value;
    }

    public abstract void execute(DataStack stack, Interpreter interpreter);
}
