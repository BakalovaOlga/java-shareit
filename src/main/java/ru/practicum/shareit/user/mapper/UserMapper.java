package ru.practicum.shareit.user.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserMapper {

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toEntity(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getName(),
                userDto.getEmail()
        );
    }

    public static void updateUserFromDto(UserDto dto, User user) {
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
    }
}
