package com.bitcoin;

import java.util.ArrayList;
import java.util.List;

public class Script {
    private final List<Object> operations = new ArrayList<>();

    public Script() {
    }

    public void add(Object op) {
        operations.add(op);
    }

    public List<Object> getOperations() {
        return operations;
    }

    /**
     * Parsea un string de script a un objeto Script.
     * Ejemplo: "1 2 OP_ADD 5 OP_GREATERTHAN"
     */
    public static Script parse(String scriptStr) {
        Script script = new Script();
        String[] tokens = scriptStr.trim().split("\\s+");

        for (String token : tokens) {
            if (token.isEmpty())
                continue;

            // Verificar si es OpCode
            try {
                OpCode op = OpCode.valueOf(token.toUpperCase());
                script.add(op);
                continue;
            } catch (IllegalArgumentException e) {
                // No es OpCode, probar datos
            }

            // Verificar si es hex (0x...)
            if (token.startsWith("0x")) {
                script.add(hexStringToByteArray(token.substring(2)));
                continue;
            }

            // Verificar si es número
            try {
                long val = Long.parseLong(token);
                script.add(encodeNumber(val));
                continue;
            } catch (NumberFormatException e) {
                // Tratar como bytes UTF-8 si falla
                script.add(token.getBytes());
            }
        }
        return script;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // Ayudante para codificar números a bytes
    private static byte[] encodeNumber(long value) {
        if (value == 0)
            return new byte[0];

        long absValue = Math.abs(value);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        while (absValue > 0) {
            bos.write((byte) (absValue & 0xFF));
            absValue >>= 8;
        }
        byte[] raw = bos.toByteArray();

        if ((raw.length > 0) && ((raw[raw.length - 1] & 0x80) != 0)) {
            byte[] newRaw = new byte[raw.length + 1];
            System.arraycopy(raw, 0, newRaw, 0, raw.length);
            if (value < 0)
                newRaw[newRaw.length - 1] = (byte) 0x80;
            return newRaw;
        } else if (value < 0) {
            raw[raw.length - 1] |= 0x80;
        }
        return raw;
    }
}
