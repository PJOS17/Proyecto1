package com.bitcoin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * Punto de entrada del intérprete de Bitcoin Script.
 * Soporta modo interactivo (REPL), ejecución por archivo y demos integradas.
 */
public class Main {
    public static void main(String[] args) {
        boolean trace = false;
        String rawScript = null;
        File fileToRead = null;

        // Parseo de argumentos
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--trace")) {
                trace = true;
            } else if (arg.equals("--file") && i + 1 < args.length) {
                fileToRead = new File(args[++i]);
            } else {
                if (rawScript == null) {
                    rawScript = arg;
                } else {
                    rawScript += " " + arg;
                }
            }
        }

        // Selección de modo
        if (fileToRead != null) {
            runFile(fileToRead, trace);
        } else if (hasArg(args, "--p2pkh")) {
            runP2PKHDemo(trace);
        } else if (hasArg(args, "--p2pkh-bad")) {
            runP2PKHBadDemo(trace);
        } else if (hasArg(args, "--demo-if")) {
            runIfElseDemo(trace);
        } else if (hasArg(args, "--demo-multisig")) {
            runMultisigDemo(trace);
        } else if (hasArg(args, "--demo-all")) {
            System.out.println("========== DEMO 1: P2PKH CORRECTO ==========\n");
            runP2PKHDemo(true);
            System.out.println("\n========== DEMO 2: P2PKH INCORRECTO ==========\n");
            runP2PKHBadDemo(true);
            System.out.println("\n========== DEMO 3: OP_IF / OP_ELSE / OP_ENDIF ==========\n");
            runIfElseDemo(true);
            System.out.println("\n========== DEMO 4: MULTISIG 2 de 3 ==========\n");
            runMultisigDemo(true);
        } else if (rawScript != null && !rawScript.trim().isEmpty()) {
            runScript("CommandLine", rawScript, trace);
        } else {
            runInteractiveSpec(trace);
        }
    }

    private static boolean hasArg(String[] args, String flag) {
        for (String a : args) {
            if (a.equals(flag)) return true;
        }
        return false;
    }

    // ==================== DEMOS ====================

    /** Demo P2PKH correcto: firma y pubKey coinciden. */
    private static void runP2PKHDemo(boolean trace) {
        System.out.println("=== P2PKH Correcto ===");
        String pubKeyStr = "misupersecretpublickey";
        byte[] pubKey = pubKeyStr.getBytes();
        byte[] pubKeyHash = Crypto.hash160(pubKey);
        byte[] signature = "valida_signature".getBytes();

        String scriptStr = "0x" + bytesToHex(signature) + " " +
                "0x" + bytesToHex(pubKey) + " " +
                "OP_DUP OP_HASH160 0x" + bytesToHex(pubKeyHash) + " OP_EQUALVERIFY OP_CHECKSIG";

        System.out.println("Script: <sig> <pubKey> OP_DUP OP_HASH160 <pubKeyHash> OP_EQUALVERIFY OP_CHECKSIG");
        runScript("P2PKH Correcto", scriptStr, trace);
    }

    /** Demo P2PKH incorrecto: pubKey no coincide con el hash esperado. */
    private static void runP2PKHBadDemo(boolean trace) {
        System.out.println("=== P2PKH Incorrecto (clave equivocada) ===");
        String realPubKey = "claveReal";
        String wrongPubKey = "claveEquivocada";
        byte[] realHash = Crypto.hash160(realPubKey.getBytes());
        byte[] signature = "firma_valida".getBytes();

        String scriptStr = "0x" + bytesToHex(signature) + " " +
                "0x" + bytesToHex(wrongPubKey.getBytes()) + " " +
                "OP_DUP OP_HASH160 0x" + bytesToHex(realHash) + " OP_EQUALVERIFY OP_CHECKSIG";

        System.out.println("Script: <sig> <wrongPubKey> OP_DUP OP_HASH160 <realPubKeyHash> OP_EQUALVERIFY OP_CHECKSIG");
        System.out.println("Esperado: FALLO (OP_EQUALVERIFY detecta que el hash no coincide)");
        runScript("P2PKH Incorrecto", scriptStr, trace);
    }

    /** Demo de control de flujo con OP_IF / OP_ELSE / OP_ENDIF. */
    private static void runIfElseDemo(boolean trace) {
        System.out.println("=== Demo IF / ELSE / ENDIF ===\n");

        System.out.println("--- Caso 1: Condición TRUE ---");
        System.out.println("Script: OP_1 OP_IF OP_2 OP_ELSE OP_3 OP_ENDIF");
        runScript("IF(true)", "OP_1 OP_IF OP_2 OP_ELSE OP_3 OP_ENDIF", trace);

        System.out.println("\n--- Caso 2: Condición FALSE ---");
        System.out.println("Script: OP_0 OP_IF OP_2 OP_ELSE OP_3 OP_ENDIF");
        runScript("IF(false)", "OP_0 OP_IF OP_2 OP_ELSE OP_3 OP_ENDIF", trace);

        System.out.println("\n--- Caso 3: IF Anidado ---");
        System.out.println("Script: OP_1 OP_IF OP_1 OP_IF OP_5 OP_ENDIF OP_ENDIF");
        runScript("IF anidado", "OP_1 OP_IF OP_1 OP_IF OP_5 OP_ENDIF OP_ENDIF", trace);
    }

    /** Demo de Multisig 2 de 3 con OP_CHECKMULTISIG. */
    private static void runMultisigDemo(boolean trace) {
        System.out.println("=== Multisig 2 de 3 ===");
        // Pila: OP_0 <sig1> <sig2> 2 <pk1> <pk2> <pk3> 3 OP_CHECKMULTISIG
        String sig1 = "0x" + bytesToHex("firma1".getBytes());
        String sig2 = "0x" + bytesToHex("firma2".getBytes());
        String pk1 = "0x" + bytesToHex("pubkey1".getBytes());
        String pk2 = "0x" + bytesToHex("pubkey2".getBytes());
        String pk3 = "0x" + bytesToHex("pubkey3".getBytes());

        String scriptStr = "OP_0 " + sig1 + " " + sig2 + " OP_2 " +
                pk1 + " " + pk2 + " " + pk3 + " OP_3 OP_CHECKMULTISIG";

        System.out.println("Script: OP_0 <sig1> <sig2> 2 <pk1> <pk2> <pk3> 3 OP_CHECKMULTISIG");
        System.out.println("Esperado: TRUE (2 firmas válidas de 3 posibles)");
        runScript("Multisig 2-of-3", scriptStr, trace);
    }

    // ==================== EJECUCIÓN ====================

    /** Ejecuta un script desde un string. */
    private static void runScript(String sourceName, String scriptStr, boolean trace) {
        System.out.println("Procesando " + sourceName + "...");
        try {
            Script script = Script.parse(scriptStr);
            Interpreter interpreter = new Interpreter();
            interpreter.setTrace(trace);

            boolean result = interpreter.execute(script);
            System.out.println("Resultado: " + (result ? "VÁLIDO (TRUE)" : "INVÁLIDO (FALSE)"));
            if (trace) {
                System.out.println("Pila final: " + interpreter.getStack());
            }
        } catch (Exception e) {
            System.err.println("Ejecución fallida: " + e.getMessage());
            if (trace) e.printStackTrace();
        }
    }

    /** Lee y ejecuta un script desde un archivo .txt. */
    private static void runFile(File file, boolean trace) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            runScript(file.getName(), content, trace);
        } catch (IOException e) {
            System.err.println("Error leyendo archivo: " + e.getMessage());
        }
    }

    /** Modo interactivo REPL. */
    private static void runInteractiveSpec(boolean defaultTrace) {
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();
        interpreter.setTrace(defaultTrace);

        System.out.println("=== Bitcoin Script Interpreter (REPL) ===");
        System.out.println("Escribe operaciones (ej: 'OP_1', 'OP_ADD') o datos (ej: '5', '0x10').");
        System.out.println("Comandos: 'exit' salir, 'trace on/off' traza, 'clear' reiniciar pila.");
        System.out.print("> ");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) { System.out.print("> "); continue; }
            if (line.equalsIgnoreCase("exit")) break;

            if (line.equalsIgnoreCase("clear")) {
                interpreter = new Interpreter();
                interpreter.setTrace(defaultTrace);
                System.out.println("Pila limpiada.");
                System.out.print("> "); continue;
            }
            if (line.equalsIgnoreCase("trace on")) {
                defaultTrace = true; interpreter.setTrace(true);
                System.out.println("Traza activada.");
                System.out.print("> "); continue;
            }
            if (line.equalsIgnoreCase("trace off")) {
                defaultTrace = false; interpreter.setTrace(false);
                System.out.println("Traza desactivada.");
                System.out.print("> "); continue;
            }

            try {
                Script lineScript = Script.parse(line);
                interpreter.execute(lineScript);
                System.out.println("Stack: " + interpreter.getStack());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.print("> ");
        }
        scanner.close();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
