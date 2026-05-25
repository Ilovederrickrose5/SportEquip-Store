package com.sportsequipment.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 明文密码编码器 - 仅用于测试环境
 * 
 * <p>注意：此编码器不进行任何加密，直接比较明文密码。
 * 生产环境请使用 BCryptPasswordEncoder。</p>
 */
@Component
public class PlainTextPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // 测试环境：直接返回明文密码
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 测试环境：直接比较明文
        return rawPassword != null && rawPassword.equals(encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return false;
    }
}