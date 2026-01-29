package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ItemMapper.class, UserMapper.class})
public interface BookingMapper {

    //полный ответ
    @Mapping(target = "status", expression = "java(booking.getStatus() != null ? booking.getStatus().name() : null)")
    BookingDto toDto(Booking booking);

    //короткий ответ
    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "itemId", source = "item.id")
    BookingShortDto toShortDto(Booking booking);

    List<BookingDto> toDtoList(List<Booking> bookings);

    List<BookingShortDto> toShortDtoList(List<Booking> bookings);

    default Booking toEntity(BookingRequestDto bookingRequestDto, Item item, User booker) {
        if (bookingRequestDto == null || item == null || booker == null) {
            return null;
        }

        return Booking.builder()
                .id(bookingRequestDto.getId())
                .item(item)
                .booker(booker)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .status(BookingStatus.WAITING)
                .build();
    }
}
