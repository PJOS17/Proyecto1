package com.bitcoin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para OpCodes individuales.
 * Cubre éxito, fallo, y casos borde.
 */
public class OpCodeTest {

    // --- Aritmética ---

    @Test
    public void testAdd() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(5); s.pushLong(3);
        OpCode.OP_ADD.execute(s, interp);
        assertEquals(8, s.popAsLong());
    }

    @Test
    public void testSub() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(10); s.pushLong(4);
        OpCode.OP_SUB.execute(s, interp);
        assertEquals(6, s.popAsLong());
    }

    @Test
    public void testLessThan() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(5); s.pushLong(10);
        OpCode.OP_LESSTHAN.execute(s, interp);
        assertTrue(s.popAsBool()); // 5 < 10 = true
    }

    @Test
    public void testGreaterThan() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(3); s.pushLong(5);
        OpCode.OP_GREATERTHAN.execute(s, interp);
        assertFalse(s.popAsBool()); // 3 > 5 = false
    }

    // --- Lógica ---

    @Test
    public void testBoolAnd() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushBool(true); s.pushBool(false);
        OpCode.OP_BOOLAND.execute(s, interp);
        assertFalse(s.popAsBool());
    }

    @Test
    public void testNot() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushBool(true);
        OpCode.OP_NOT.execute(s, interp);
        assertFalse(s.popAsBool());
    }

    @Test
    public void testEqual() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(42); s.pushLong(42);
        OpCode.OP_EQUAL.execute(s, interp);
        assertTrue(s.popAsBool());
    }

    @Test
    public void testEqualVerifySuccess() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(7); s.pushLong(7);
        // No debe lanzar excepción
        OpCode.OP_EQUALVERIFY.execute(s, interp);
        assertTrue(s.isEmpty());
    }

    @Test
    public void testEqualVerifyFail() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(7); s.pushLong(8);
        assertThrows(RuntimeException.class, () -> {
            OpCode.OP_EQUALVERIFY.execute(s, interp);
        });
    }

    // --- Pila ---

    @Test
    public void testDup() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(1);
        OpCode.OP_DUP.execute(s, interp);
        assertEquals(2, s.size());
        assertEquals(1, s.popAsLong());
        assertEquals(1, s.popAsLong());
    }

    @Test
    public void testDrop() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(99);
        OpCode.OP_DROP.execute(s, interp);
        assertTrue(s.isEmpty());
    }

    @Test
    public void testSwap() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushLong(1); s.pushLong(2);
        OpCode.OP_SWAP.execute(s, interp);
        assertEquals(1, s.popAsLong()); // tope era 2, swap pone 1 arriba
        assertEquals(2, s.popAsLong());
    }

    // --- Stack underflow ---

    @Test
    public void testStackUnderflow() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        // Pila vacía, intentar OP_DUP debe fallar
        assertThrows(IllegalStateException.class, () -> {
            OpCode.OP_DUP.execute(s, interp);
        });
    }

    // --- Criptografía ---

    @Test
    public void testHash160() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        byte[] data = "test".getBytes();
        s.push(data);
        OpCode.OP_HASH160.execute(s, interp);
        byte[] result = s.pop();
        assertNotNull(result);
        assertEquals(20, result.length); // SHA-1 produce 20 bytes
    }

    @Test
    public void testCheckSig() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.push("firma".getBytes());
        s.push("pubkey".getBytes());
        OpCode.OP_CHECKSIG.execute(s, interp);
        assertTrue(s.popAsBool()); // Mock siempre retorna true
    }

    @Test
    public void testSHA256() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.push("hello".getBytes());
        OpCode.OP_SHA256.execute(s, interp);
        byte[] result = s.pop();
        assertNotNull(result);
        assertEquals(32, result.length); // SHA-256 produce 32 bytes
    }

    // --- Multisig ---

    @Test
    public void testCheckMultisig() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        // Construir pila: dummy, sig1, sig2, 2, pk1, pk2, pk3, 3
        s.push(new byte[0]);           // dummy (bug histórico)
        s.push("sig1".getBytes());     // firma 1
        s.push("sig2".getBytes());     // firma 2
        s.pushLong(2);                 // m = 2 firmas
        s.push("pk1".getBytes());      // pubKey 1
        s.push("pk2".getBytes());      // pubKey 2
        s.push("pk3".getBytes());      // pubKey 3
        s.pushLong(3);                 // n = 3 pubKeys
        OpCode.OP_CHECKMULTISIG.execute(s, interp);
        assertTrue(s.popAsBool());
    }

    // --- OP_RETURN ---

    @Test
    public void testOpReturn() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        assertThrows(RuntimeException.class, () -> {
            OpCode.OP_RETURN.execute(s, interp);
        });
    }

    // --- OP_VERIFY ---

    @Test
    public void testVerifySuccess() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushBool(true);
        OpCode.OP_VERIFY.execute(s, interp);
        assertTrue(s.isEmpty()); // OP_VERIFY consume el tope
    }

    @Test
    public void testVerifyFail() {
        Interpreter interp = new Interpreter();
        DataStack s = interp.getStack();
        s.pushBool(false);
        assertThrows(RuntimeException.class, () -> {
            OpCode.OP_VERIFY.execute(s, interp);
        });
    }
}
