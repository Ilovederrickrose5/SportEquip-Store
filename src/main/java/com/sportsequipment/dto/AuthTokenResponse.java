package com.sportsequipment.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录/刷新令牌统一响应（双 token 机制）
 * 保留原 JwtResponse 字段，向下兼容旧前端 token 字段
 * @author System
 */
@Getter
@Setter
public class AuthTokenResponse {
    private String token;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long accessTokenExpiresIn;
    private Long refreshTokenExpiresIn;

    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatar;

    public static AuthTokenResponse of(String accessToken,
                                       String refreshToken,
                                       long accessTokenExpiresIn,
                                       long refreshTokenExpiresIn,
                                       Long id,
                                       String username,
                                       String email,
                                       String role,
                                       String avatar) {
        AuthTokenResponse resp = new AuthTokenResponse();
        // token 字段保留，兼容旧前端
        resp.token = accessToken;
        resp.accessToken = accessToken;
        resp.refreshToken = refreshToken;
        resp.accessTokenExpiresIn = accessTokenExpiresIn;
        resp.refreshTokenExpiresIn = refreshTokenExpiresIn;
        resp.id = id;
        resp.username = username;
        resp.email = email;
        resp.role = role;
        resp.avatar = avatar;
        return resp;
    }

    /**
     * 仅刷新 access_token 场景返回（用户信息复用之前登录获取的即可）
     */
    public static AuthTokenResponse refreshOnly(String accessToken,
                                                String refreshToken,
                                                long accessTokenExpiresIn,
                                                long refreshTokenExpiresIn) {
        AuthTokenResponse resp = new AuthTokenResponse();
        resp.token = accessToken;
        resp.accessToken = accessToken;
        resp.refreshToken = refreshToken;
        resp.accessTokenExpiresIn = accessTokenExpiresIn;
        resp.refreshTokenExpiresIn = refreshTokenExpiresIn;
        return resp;
    }
}
