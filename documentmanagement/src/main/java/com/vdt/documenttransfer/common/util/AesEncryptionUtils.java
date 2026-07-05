package com.vdt.documenttransfer.common.util;

import java.nio.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

@Component
public class AesEncryptionUtils {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    public String encrypt(String plainText, String base64SecretKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64SecretKey);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encryptedBytes = cipher.doFinal(
                    plainText.getBytes(StandardCharsets.UTF_8)
            );

            ByteBuffer byteBuffer = ByteBuffer.allocate(
                    iv.length + encryptedBytes.length
            );

            byteBuffer.put(iv);
            byteBuffer.put(encryptedBytes);

            return Base64.getEncoder()
                    .encodeToString(byteBuffer.array());

        } catch (Exception e) {
            throw new RuntimeException(
                    "Mã hóa dữ liệu thất bại: " + e.getMessage(),
                    e
            );
        }
    }

    public String decrypt(String encryptedText, String base64SecretKey) {
        try {
            byte[] combinedBytes = Base64.getDecoder()
                    .decode(encryptedText);

            ByteBuffer byteBuffer = ByteBuffer.wrap(combinedBytes);

            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);

            byte[] encryptedBytes =
                    new byte[byteBuffer.remaining()];

            byteBuffer.get(encryptedBytes);

            byte[] keyBytes = Base64.getDecoder()
                    .decode(base64SecretKey);

            SecretKey secretKey =
                    new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = cipher.doFinal(encryptedBytes);

            return new String(
                    plainBytes,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Giải mã dữ liệu thất bại hoặc dữ liệu đã bị thay đổi",
                    e
            );
        }
    }
}
