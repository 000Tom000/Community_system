package com.tom.community.common;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 密码工具 — SHA-256 + 随机盐
 * <p>
 * 存储格式：{@code saltHex$hashHex}
 * 后续若升级加密算法，只需修改此类。
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 加密明文密码，返回 "盐$哈希" 格式
     */
    public static String encrypt(String rawPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltHex = HexFormat.of().formatHex(salt);
        String hash = sha256(rawPassword + saltHex);
        return saltHex + "$" + hash;
    }

    /**
     * 校验明文密码是否匹配已加密的密码
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        if (encryptedPassword == null || !encryptedPassword.contains("$")) {
            return false;
        }
        String[] parts = encryptedPassword.split("\\$", 2);
        String saltHex = parts[0];
        String hash = parts[1];
        return sha256(rawPassword + saltHex).equals(hash);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 加密失败", e);
        }
    }
}
