package com.sportsequipment.util;

import java.util.regex.Pattern;

public class PasswordValidator {

    // 密码至少包含6个字符
    private static final int MIN_LENGTH = 6;
    // 密码至少包含一个字母（不区分大小写）
    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    // 密码至少包含一个数字
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    /**
     * 根据用户角色验证密码强度
     * @param password 要验证的密码
     * @return 密码是否有效
     */
    public static boolean isValid(String password) {
        // 默认行为：管理员密码规则（至少6个字符，包含字母和数字）
        return isValid(password, "ADMIN");
    }

    /**
     * 根据用户角色验证密码强度
     * @param password 要验证的密码
     * @param role 用户角色（ADMIN或USER）
     * @return 密码是否有效
     */
    public static boolean isValid(String password, String role) {
        // 密码不能为空
        if (password == null || password.isEmpty()) {
            return false;
        }

        // 对于管理员，应用原有的密码强度规则
        if ("ADMIN".equals(role)) {
            if (password.length() < MIN_LENGTH) {
                return false;
            }

            boolean hasLetter = LETTER_PATTERN.matcher(password).find();
            boolean hasDigit = DIGIT_PATTERN.matcher(password).find();

            return hasLetter && hasDigit;
        }

        // 对于普通用户，没有特殊限制
        return true;
    }

    public static String getValidationMessage() {
        return "密码必须至少包含6个字符，且包含至少一个字母和一个数字。";
    }
}