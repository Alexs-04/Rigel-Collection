package com.korebit.rigel.dto;

import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.model.enums.Role;

import java.io.Serializable;

public record ConsumerDto(
        String name,
        Role role,
        String username,
        String password,
        String email,
        String phoneNumber
) implements Serializable {
    public static ConsumerDto toRequest(Consumer consumer) {
        return new ConsumerDto(
                consumer.getName(),
                consumer.getRole(),
                consumer.getUsername(),
                consumer.getPassword(),
                consumer.getEmail(),
                consumer.getPhoneNumber()
        );
    }
}
