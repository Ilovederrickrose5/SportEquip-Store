package com.sportsequipment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户注册请求数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    private String phone;

    private String address;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
}
    