package com.korebit.rigel.dto;

import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.model.enums.Role;

import java.io.Serializable;

public record ConsumerRequest(
        String name,
        Role role,
        String username,
        String password,
        String email,
        String phoneNumber
) implements Serializable {
    public static ConsumerRequest toRequest(Consumer consumer) {
        return new ConsumerRequest(
                consumer.getName(),
                consumer.getRole(),
                consumer.getUsername(),
                consumer.getPassword(),
                consumer.getEmail(),
                consumer.getPhoneNumber()
        );
    }
}
