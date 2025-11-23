package com.sportsequipment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户更新请求数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class UserUpdateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3到20个字符之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 20, message = "手机号不能超过20个字符")
    private String phone;

    @Size(max = 255, message = "地址不能超过255个字符")
    private String address;

    // 可选的新密码字段
    private String password;
    
    // 头像字段
    private String avatar;
}