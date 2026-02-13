package com.bitcoin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean trace = false;
        String rawScript = null;
        File fileToRead = null;

        // Argument Parsing
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

        // Mode Selection
        if (fileToRead != null) {
            runFile(fileToRead, trace);
        } else if (args.length > 0 && args[0].equals("--p2pkh")) {
            runP2PKHDemo(trace);
        } else if (rawScript != null && !rawScript.trim().isEmpty()) {
            runScript("CommandLine", rawScript, trace);
        } else {
            runInteractiveSpec(trace); // Interactive REPL
        }
    }

    private static void runP2PKHDemo(boolean trace) {
        System.out.println("=== Running P2PKH Example ===");
        // Data
        String pubKeyStr = "misupersercretpublickey";
        byte[] pubKey = pubKeyStr.getBytes();
        byte[] pubKeyHash = Crypto.hash160(pubKey);
        byte[] signature = "valida_signature".getBytes(); // Mock signature

        // Hex formatting for script string
        StringBuilder scriptBuilder = new StringBuilder();
        // <sig>
        scriptBuilder.append("0x").append(bytesToHex(signature)).append(" ");
        // <pubKey>
        scriptBuilder.append("0x").append(bytesToHex(pubKey)).append(" ");
        // OP_DUP OP_HASH160
        scriptBuilder.append("OP_DUP OP_HASH160 ");
        // <pubKeyHash>
        scriptBuilder.append("0x").append(bytesToHex(pubKeyHash)).append(" ");
        // OP_EQUALVERIFY OP_CHECKSIG
        scriptBuilder.append("OP_EQUALVERIFY OP_CHECKSIG");

        String scriptStr = scriptBuilder.toString();
        System.out.println("Generated Script: " + scriptStr);
        runScript("P2PKH Demo", scriptStr, trace);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static void runScript(String sourceName, String scriptStr, boolean trace) {
        System.out.println("Processing " + sourceName + "...");
        try {
            Script script = Script.parse(scriptStr);
            Interpreter interpreter = new Interpreter();
            interpreter.setTrace(trace);

            boolean result = interpreter.execute(script);
            System.out.println("Execution Result: " + (result ? "VALID (TRUE)" : "INVALID (FALSE)"));
            if (trace) {
                System.out.println("Final Stack: " + interpreter.getStack());
            }

        } catch (Exception e) {
            System.err.println("Execution Failed: " + e.getMessage());
            if (trace)
                e.printStackTrace();
        }
    }

    private static void runFile(File file, boolean trace) {
        try {
            String content = Files.readString(file.toPath());
            runScript(file.getName(), content, trace);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static void runInteractiveSpec(boolean defaultTrace) {
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();
        interpreter.setTrace(defaultTrace); // Can toggle?

        System.out.println("=== Bitcoin Script Interpreter (REPL) ===");
        System.out.println("Type operations (e.g., 'OP_1', 'OP_ADD') or data (e.g., '5', '0x10').");
        System.out.println("Commands: 'exit' to quit, 'trace on/off' to toggle tracing, 'clear' to reset stack.");
        System.out.print("> ");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.print("> ");
                continue;
            }

            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            if (line.equalsIgnoreCase("clear")) {
                interpreter = new Interpreter();
                interpreter.setTrace(defaultTrace);
                System.out.println("Stack cleared.");
                System.out.print("> ");
                continue;
            }
            if (line.equalsIgnoreCase("trace on")) {
                defaultTrace = true;
                interpreter.setTrace(true);
                System.out.println("Trace enabled.");
                System.out.print("> ");
                continue;
            }
            if (line.equalsIgnoreCase("trace off")) {
                defaultTrace = false;
                interpreter.setTrace(false);
                System.out.println("Trace disabled.");
                System.out.print("> ");
                continue;
            }

            try {
                // Parsear línea como script
                Script lineScript = Script.parse(line);
                // Ejecutamos línea por línea manteniendo el estado del intérprete
                interpreter.execute(lineScript);

                System.out.println("Stack: " + interpreter.getStack());

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.print("> ");
        }
        scanner.close();
    }
}
