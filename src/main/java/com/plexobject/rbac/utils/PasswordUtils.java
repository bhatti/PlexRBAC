package com.plexobject.rbac.utils;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.plexobject.rbac.Configuration;

public class PasswordUtils {
    private static final byte[] KEY_BYTES = new byte[] { 0x73, 0x2f, 0x2d,
            0x33, (byte) 0xc8, 0x01, 0x73, 0x2b, 0x72, 0x06, 0x75, 0x6c,
            (byte) 0xbd, 0x44, (byte) 0xf9, (byte) 0xc1, (byte) 0xc1, 0x03,
            (byte) 0xdd, (byte) 0xd9, 0x7c, 0x7c, (byte) 0xbe, (byte) 0x8e };
    private static final String ENCRYPTION_PASSWORD = Configuration
            .getInstance().getProperty("encryption_credentials", "credentials");
    private static final byte[] IV_BYTES = new byte[] { (byte) 0xb0, 0x7b,
            (byte) 0xf5, 0x22, (byte) 0xc8, (byte) 0xd6, 0x08, (byte) 0xb8 };

    static {
        Security
                .addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static String generateCredentials() {
        // Uses a secure Random not a simple Random
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            // generation 96 bits long
            byte[] bytes = new byte[12];
            random.nextBytes(bytes);
            // Digest computation
            String credentials = byteToBase64(bytes);

            return getHash(credentials);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(String text) throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        byte[] input = text.getBytes();

        SecretKeySpec key = new SecretKeySpec(KEY_BYTES, "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS7Padding", "BC");
        // encryption pass
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV_BYTES));
        byte[] cipherText = cipher.doFinal(input);
        return new String(cipherText);
    }

    public static String decrypt(String text) throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeySpecException {
        final byte[] cipherText = text.getBytes();
        // decryption pass
        // Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS7Padding", "BC");
        // SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY, "DESede");
        // cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV_BYTES));
        //
        // byte[] plainText = new byte[cipherText.length];
        // int ptLength = cipher.update(cipherText, 0, cipherText.length,
        // plainText, 0);
        // ptLength += cipher.doFinal(plainText, ptLength);

        // decrypt the data using PBE
        byte[] salt = new byte[] { 0x7d, 0x60, 0x43, 0x5f, 0x02, (byte) 0xe9,
                (byte) 0xe0, (byte) 0xae };
        int iterationCount = 2048;
        PBEKeySpec pbeSpec = new PBEKeySpec(ENCRYPTION_PASSWORD.toCharArray());
        SecretKeyFactory keyFact = SecretKeyFactory.getInstance(
                "PBEWithSHAAnd3KeyTripleDES", "BC");

        Cipher cipher = Cipher.getInstance("PBEWithSHAAnd3KeyTripleDES", "BC");
        Key sKey = keyFact.generateSecret(pbeSpec);

        cipher.init(Cipher.DECRYPT_MODE, sKey, new PBEParameterSpec(salt,
                iterationCount));

        return new String(cipher.doFinal(cipherText));
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

    public static String getHash(String credentials) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            byte[] input = digest.digest(credentials.getBytes());
            return byteToBase64(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
