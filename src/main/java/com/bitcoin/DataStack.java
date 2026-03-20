package com.bitcoin;

import java.util.Stack;

public class DataStack {
    private final Stack<byte[]> stack;

    public DataStack() {
        this.stack = new Stack<>();
    }

    public void push(byte[] data) {
        stack.push(data);
    }

    public byte[] pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack underflow");
        }
        return stack.pop();
    }

    public byte[] peek() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack underflow");
        }
        return stack.peek();
    }

    public int size() {
        return stack.size();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    // Interpreta byte[] como long (Bitcoin usa Little-Endian, simplificado aquí)
    public long popAsLong() {
        byte[] bytes = pop();
        if (bytes.length == 0)
            return 0;

        // Parsea Big-Endian (simplificado para el proyecto)
        long value = 0;
        for (int i = 0; i < bytes.length && i < 8; i++) {
            value |= ((long) (bytes[i] & 0xFF)) << (8 * i);
        }

        // Manejo de signo (Sign-Magnitude)
        if ((bytes[bytes.length - 1] & 0x80) != 0) {
            long mask = ~(0x80L << (8 * (bytes.length - 1)));
            value &= mask;
            value = -value;
        }

        return value;
    }

    public void pushLong(long value) {
        if (value == 0) {
            push(new byte[0]);
            return;
        }

        boolean negative = value < 0;
        long absValue = Math.abs(value);

        // Serialización simplificada
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        while (absValue > 0) {
            bos.write((byte) (absValue & 0xFF));
            absValue >>= 8;
        }
        byte[] raw = bos.toByteArray();

        // Manejo de signo
        if ((raw.length > 0) && ((raw[raw.length - 1] & 0x80) != 0)) {
            if (negative) {
                byte[] newRaw = new byte[raw.length + 1];
                System.arraycopy(raw, 0, newRaw, 0, raw.length);
                newRaw[newRaw.length - 1] = (byte) 0x80;
                raw = newRaw;
            } else {
                byte[] newRaw = new byte[raw.length + 1];
                System.arraycopy(raw, 0, newRaw, 0, raw.length);
                raw = newRaw;
            }
        } else if (negative) {
            if (raw.length > 0) {
                raw[raw.length - 1] |= 0x80;
            }
        }

        push(raw);
    }

    public boolean popAsBool() {
        byte[] bytes = pop();
        // Cero negativo (0x80) también es falso
        if (bytes.length == 0)
            return false;
        if (bytes.length == 1 && bytes[0] == (byte) 0x80)
            return false;

        for (byte b : bytes) {
            if (b != 0)
                return true;
        }
        return false;
    }

    public void pushBool(boolean val) {
        if (val) {
            pushLong(1);
        } else {
            pushLong(0); // Array vacío
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Stack: [");
        for (int i = 0; i < stack.size(); i++) {
            byte[] b = stack.get(i);
            sb.append(hex(b));
            if (i < stack.size() - 1)
                sb.append(", ");
        }
        sb.append("] (Top)");
        return sb.toString();
    }

    private String hex(byte[] bytes) {
        if (bytes.length == 0)
            return "0x0";
        StringBuilder sb = new StringBuilder("0x");
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
