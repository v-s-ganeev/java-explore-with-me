package ru.practicum.user.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(UserDto userDto);

    void delete(Long userId);

    List<UserDto> getUsers(List<Long> userIds, PageRequest pageRequest);
}
