package com.korebit.rigel.dto.request;

import com.korebit.rigel.dto.TicketDetailDto;
import com.korebit.rigel.enums.MethodPayment;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Payload to register a sale.
 *
 * <p>The seller is always the authenticated user; it is never taken from the request body.</p>
 */
public record TicketAddRequest(
        String description,
        LocalDate dateAndTime,
        List<TicketDetailDto> products,
        MethodPayment methodPayment
) implements Serializable {
}
