package com.bitcoin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTest {

    @Test
    public void testExampleScript() {
        // "1 2 OP_ADD 5 OP_GREATERTHAN"
        // 1, 2 -> stack: [1, 2]
        // ADD -> stack: [3]
        // 5 -> stack: [3, 5]
        // GREATERTHAN -> 3 > 5? False.
        // Result: False (Invalid)
        String scriptStr = "1 2 OP_ADD 5 OP_GREATERTHAN";
        Script script = Script.parse(scriptStr);
        Interpreter interpreter = new Interpreter();

        boolean result = interpreter.execute(script);
        assertFalse(result, "Script should return false");
        // Verify stack top is boolean false (0 or empty)
        assertFalse(interpreter.getStack().popAsBool());
    }

    @Test
    public void testControlFlow() {
        // "OP_TRUE OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF" -> Should result in 1
        String scriptStr = "OP_TRUE OP_IF OP_1 OP_ELSE OP_0 OP_ENDIF";
        Script script = Script.parse(scriptStr);
        Interpreter interpreter = new Interpreter();

        assertTrue(interpreter.execute(script));
        assertEquals(1, interpreter.getStack().popAsLong());
    }

    @Test
    public void testP2PKH() {
        // <sig> <pubKey> OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG
        // Mock data:
        // PubKey: "mykey" (bytes)
        // PubKeyHash: HASH160("mykey")
        // Sig: "mysig"

        String pubKey = "mykey";
        String sig = "mysig";

        // Calculate hash
        byte[] pkBytes = pubKey.getBytes();
        byte[] hash = Crypto.hash160(pkBytes);
        String hashHex = bytesToHex(hash);
        String pkHex = bytesToHex(pkBytes);
        String sigHex = bytesToHex(sig.getBytes());

        String scriptStr = "0x" + sigHex + " " +
                "0x" + pkHex + " " +
                "OP_DUP OP_HASH160 0x" + hashHex + " OP_EQUALVERIFY OP_CHECKSIG";

        Script script = Script.parse(scriptStr);
        Interpreter interpreter = new Interpreter();
        interpreter.setTrace(true);

        boolean result = interpreter.execute(script);
        assertTrue(result, "P2PKH should succeed with valid signature");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
