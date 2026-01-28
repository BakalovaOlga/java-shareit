package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    private Long id;
    private Long itemId;
    private Long booker;
    private LocalDateTime start;
    private LocalDateTime end;
}