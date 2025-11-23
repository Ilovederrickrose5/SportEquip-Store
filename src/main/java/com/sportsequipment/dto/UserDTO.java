package com.sportsequipment.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String avatar;
}
    