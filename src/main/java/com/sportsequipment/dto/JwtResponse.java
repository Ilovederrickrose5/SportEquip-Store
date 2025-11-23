package com.sportsequipment.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT响应数据传输对象
 * @author System
 */
@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatar;

    public JwtResponse(String accessToken, Long id, String username, String email, String role, String avatar) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
    }
}
    