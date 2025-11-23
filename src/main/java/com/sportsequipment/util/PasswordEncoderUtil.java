package com.sportsequipment.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类，用于生成BCrypt加密的密码哈希值
 */
public class PasswordEncoderUtil {

    /**
     * 生成BCrypt加密的密码哈希值
     * @param rawPassword 原始明文密码
     * @return BCrypt加密后的密码哈希值
     */
    public static String encodePassword(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }

    /**
     * 主方法，用于快速生成密码哈希值
     * 可以通过运行此类来获取指定密码的哈希值
     */
    public static void main(String[] args) {
        // 示例：生成密码"admin123"的BCrypt哈希值
        String password = "admin123";
        String encodedPassword = encodePassword(password);
        System.out.println("原始密码: " + password);
        System.out.println("BCrypt哈希值: " + encodedPassword);
    }
}