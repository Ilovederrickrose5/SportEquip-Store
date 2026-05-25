package com.sportsequipment.entity;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 地址实体类
 * @author sports-equipment-team
 */
@Getter
@Setter
public class Address {
    private Long id;

    private Long userId;

    @NotBlank(message = "Recipient name is required")
    @Size(max = 50, message = "Recipient name cannot exceed 50 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    private boolean isDefault = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private User user;

    public Address() {
    }

    public boolean isDefault() {
        return isDefault;
    }
}