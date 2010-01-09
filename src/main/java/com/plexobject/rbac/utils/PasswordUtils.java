package com.plexobject.rbac.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.plexobject.rbac.Configuration;

public class PasswordUtils {
    private static final byte[] ENCRYPTION_KEY = Configuration.getInstance()
            .getProperty("encryption_key", "plexrbac").getBytes();

    public static String generatePassword() {
        // Uses a secure Random not a simple Random
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            // generation 96 bits long
            byte[] bytes = new byte[12];
            random.nextBytes(bytes);
            // Digest computation
            String password = byteToBase64(bytes);

            return getHash(password);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(String text) throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        byte[] input = text.getBytes();

        SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");

        // encryption pass

        byte[] cipherText = new byte[input.length];
        cipher.init(Cipher.ENCRYPT_MODE, key);
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
        return new String(cipherText);
    }

    public static String decrypt(String text) throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        final byte[] cipherText = text.getBytes();
        // decryption pass
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
        SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY, "AES");

        byte[] plainText = new byte[cipherText.length];
        cipher.init(Cipher.DECRYPT_MODE, key);
        int ptLength = cipher.update(cipherText, 0, cipherText.length,
                plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        return new String(plainText);
    }

    /**
     * From a base 64 representation, returns the corresponding byte[]
     * 
     * @param data
     *            String The base64 representation
     * @return byte[]
     * @throws IOException
     */
    public static byte[] base64ToByte(String data) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(data);
    }

    /**
     * From a byte[] returns a base 64 representation
     * 
     * @param data
     *            byte[]
     * @return String
     * @throws IOException
     */
    public static String byteToBase64(byte[] data) {
        BASE64Encoder endecoder = new BASE64Encoder();
        return endecoder.encode(data);
    }

    public static String getHash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            byte[] input = digest.digest(password.getBytes("UTF-8"));
            return byteToBase64(input);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
