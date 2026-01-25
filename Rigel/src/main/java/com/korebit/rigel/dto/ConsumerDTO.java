package com.korebit.rigel.dto;

import com.korebit.rigel.model.Consumer;
import com.korebit.rigel.model.enums.Role;

import java.io.Serializable;

public record ConsumerDTO(
        String name,
        Role role,
        String username,
        String password,
        String email,
        String phoneNumber
) implements Serializable {
    public static ConsumerDTO toDTO(Consumer consumer) {
        return new ConsumerDTO(
                consumer.getName(),
                consumer.getRole(),
                consumer.getUsername(),
                consumer.getPassword(),
                consumer.getEmail(),
                consumer.getPhoneNumber()
        );
    }
}
