package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsAndBookingDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring", uses = CommentMapper.class)
public interface ItemWithBookingsMapper {
    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "ownerId", expression = "java(item.getOwner() != null ? item.getOwner().getId() : null)")
    @Mapping(target = "requestId", expression = "java(item.getRequest() != null ? item.getRequest().getId() : null)")
    ItemWithCommentsAndBookingDto toDto(
            Item item,
            List<CommentDto> comments,
            BookingShortDto lastBooking,
            BookingShortDto nextBooking
    );
}
