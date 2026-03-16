package com.korebit.rigel.dto.request;

import com.korebit.rigel.dto.TicketDetailDto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record TicketAddRequest(
        String description,
        LocalDate dateAndTime,
        List<TicketDetailDto> products,
        String currentConsumerEmail
) implements Serializable {
}
