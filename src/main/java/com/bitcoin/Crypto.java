package com.bitcoin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Crypto {

    public static byte[] hash256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static byte[] hash160(byte[] data) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            MessageDigest ripemd160 = MessageDigest.getInstance("SHA-1");
            // Bitcoin usa RIPEMD160 después de SHA256. Simulamos Hash160 con SHA-256 ->
            // SHA-1
            // ya que Java estándar no incluye RIPEMD160 por defecto.
            return ripemd160.digest(sha256.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo de hash no disponible", e);
        }
    }

    public static boolean checkSig(byte[] signature, byte[] pubKey, byte[] messageHash) {
        // Implementación Mock: Retorna true para propósitos educativos
        // En un escenario real, esto verificaría ECDSA secp256k1
        return true;
    }
}
