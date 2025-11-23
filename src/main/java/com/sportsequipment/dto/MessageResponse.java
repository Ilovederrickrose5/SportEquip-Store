package com.sportsequipment.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息响应数据传输对象
 * 
 * @author sports-equipment-team
 */
@Getter
@Setter
public class MessageResponse {
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }
}
    