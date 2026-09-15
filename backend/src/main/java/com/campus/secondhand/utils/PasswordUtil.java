package com.campus.secondhand.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * 密码工具类：MD5 + 随机盐，禁止明文存储。
 */
public final class PasswordUtil {

    /** 盐与密文的分隔符 */
    private static final String SALT_SEPARATOR = "$";

    /** 盐的长度（字符数） */
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 加密密码：随机生成盐后调用 {@link #encrypt(String, String)}。
     *
     * @param rawPassword 明文密码
     * @return {@code <MD5>$<盐>} 形式的密文，可直接存库
     */
    public static String encrypt(String rawPassword) {
        return encrypt(rawPassword, randomSalt());
    }

    /**
     * 用指定盐加密，便于单元测试复现结果。
     *
     * @param rawPassword 明文密码
     * @param salt        盐，不能为空
     * @return {@code <MD5>$<盐>} 形式的密文
     */
    public static String encrypt(String rawPassword, String salt) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("盐不能为空");
        }
        return md5(salt + rawPassword) + SALT_SEPARATOR + salt;
    }

    /**
     * 校验明文密码是否匹配库里的密文，自动兼容「加盐」与「纯 MD5」两种格式。
     *
     * @param rawPassword       用户输入的明文密码
     * @param encryptedPassword 数据库里的密文
     * @return 匹配返回 true；任一参数为空返回 false
     */
    public static boolean matches(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null || encryptedPassword.isEmpty()) {
            return false;
        }
        int index = encryptedPassword.indexOf(SALT_SEPARATOR);
        if (index > 0) {
            // 加盐格式：密文$盐
            String hash = encryptedPassword.substring(0, index);
            String salt = encryptedPassword.substring(index + SALT_SEPARATOR.length());
            return equalsConstantTime(md5(salt + rawPassword), hash);
        }
        // 兼容 data.sql 种子数据的纯 MD5
        return equalsConstantTime(md5(rawPassword), encryptedPassword);
    }

    /**
     * 计算 MD5，返回 32 位小写十六进制字符串。
     * 与 {@code MD5('123456') = e10adc3949ba59abbe56e057f20f883e} 的结果一致，
     * 所以也可以直接用来给种子数据生成密文。
     */
    public static String md5(String text) {
        if (text == null) {
            throw new IllegalArgumentException("待加密内容不能为空");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持 MD5 算法", e);
        }
    }

    /**
     * 生成 16 位随机盐（32 位 UUID 去掉横线后截断）。
     */
    public static String randomSalt() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, SALT_LENGTH);
    }

    /**
     * 定长比较，避免通过响应耗时差异逐字符猜出哈希值。
     */
    private static boolean equalsConstantTime(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
