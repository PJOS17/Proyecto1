package com.bitcoin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para el Intérprete.
 * Cubre P2PKH, control de flujo (IF/ELSE/ENDIF), y casos borde.
 */
public class InterpreterTest {

    // --- Script básico ---

    @Test
    public void testSimpleArithmetic() {
        // 1 + 2 = 3, 3 > 5 ? false
        Script script = Script.parse("1 2 OP_ADD 5 OP_GREATERTHAN");
        Interpreter interp = new Interpreter();
        assertFalse(interp.execute(script));
    }

    @Test
    public void testSimpleTrue() {
        // OP_1 deja 1 en la pila = true
        Script script = Script.parse("OP_1");
        Interpreter interp = new Interpreter();
        assertTrue(interp.execute(script));
    }

    // --- Control de Flujo ---

    @Test
    public void testIfTrue() {
        // TRUE -> entra al IF, empuja 2
        Script script = Script.parse("OP_TRUE OP_IF OP_2 OP_ELSE OP_3 OP_ENDIF");
        Interpreter interp = new Interpreter();
        assertTrue(interp.execute(script));
        assertEquals(2, interp.getStack().popAsLong());
    }

    @Test
    public void testIfFalse() {
        // FALSE -> entra al ELSE, empuja 3
        Script script = Script.parse("OP_FALSE OP_IF OP_2 OP_ELSE OP_3 OP_ENDIF");
        Interpreter interp = new Interpreter();
        assertTrue(interp.execute(script));
        assertEquals(3, interp.getStack().popAsLong());
    }

    @Test
    public void testNestedIf() {
        // IF anidado: ambos true, resultado = 5
        Script script = Script.parse("OP_1 OP_IF OP_1 OP_IF OP_5 OP_ENDIF OP_ENDIF");
        Interpreter interp = new Interpreter();
        assertTrue(interp.execute(script));
        assertEquals(5, interp.getStack().popAsLong());
    }

    @Test
    public void testUnbalancedIf() {
        // IF sin ENDIF debe lanzar error
        Script script = Script.parse("OP_1 OP_IF OP_2");
        Interpreter interp = new Interpreter();
        assertThrows(RuntimeException.class, () -> interp.execute(script));
    }

    // --- P2PKH ---

    @Test
    public void testP2PKHValid() {
        String pubKey = "mykey";
        String sig = "mysig";
        byte[] pkBytes = pubKey.getBytes();
        byte[] hash = Crypto.hash160(pkBytes);

        String scriptStr = "0x" + hex(sig.getBytes()) + " " +
                "0x" + hex(pkBytes) + " " +
                "OP_DUP OP_HASH160 0x" + hex(hash) + " OP_EQUALVERIFY OP_CHECKSIG";

        Script script = Script.parse(scriptStr);
        Interpreter interp = new Interpreter();
        assertTrue(interp.execute(script), "P2PKH válido debe ser TRUE");
    }

    @Test
    public void testP2PKHInvalidKey() {
        // PubKey incorrecta -> OP_EQUALVERIFY falla
        String realKey = "claveReal";
        String wrongKey = "claveWrong";
        byte[] realHash = Crypto.hash160(realKey.getBytes());

        String scriptStr = "0x" + hex("sig".getBytes()) + " " +
                "0x" + hex(wrongKey.getBytes()) + " " +
                "OP_DUP OP_HASH160 0x" + hex(realHash) + " OP_EQUALVERIFY OP_CHECKSIG";

        Script script = Script.parse(scriptStr);
        Interpreter interp = new Interpreter();
        assertThrows(RuntimeException.class, () -> interp.execute(script),
                "P2PKH con clave incorrecta debe fallar en EQUALVERIFY");
    }

    // --- Pila vacía ---

    @Test
    public void testEmptyScript() {
        // Script vacío = pila vacía = false
        Script script = Script.parse("");
        Interpreter interp = new Interpreter();
        assertFalse(interp.execute(script));
    }

    @Test
    public void testOnlyFalse() {
        // OP_0 empuja vacío = false
        Script script = Script.parse("OP_0");
        Interpreter interp = new Interpreter();
        assertFalse(interp.execute(script));
    }

    // --- OP_RETURN ---

    @Test
    public void testOpReturnFails() {
        Script script = Script.parse("OP_1 OP_RETURN");
        Interpreter interp = new Interpreter();
        assertThrows(RuntimeException.class, () -> interp.execute(script));
    }

    // --- Multisig ---

    @Test
    public void testMultisig2of3() {
        // OP_0 <sig1> <sig2> 2 <pk1> <pk2> <pk3> 3 OP_CHECKMULTISIG
        String scriptStr = "OP_0 " +
                "0x" + hex("s1".getBytes()) + " " +
                "0x" + hex("s2".getBytes()) + " " +
                "OP_2 " +
                "0x" + hex("p1".getBytes()) + " " +
                "0x" + hex("p2".getBytes()) + " " +
                "0x" + hex("p3".getBytes()) + " " +
                "OP_3 OP_CHECKMULTISIG";

        Script script = Script.parse(scriptStr);
        Interpreter interp = new Interpreter();
        assertTrue(interp.execute(script));
    }

    // --- Helper ---

    private String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
