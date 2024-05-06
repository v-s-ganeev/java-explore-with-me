package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.user.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private CategoryDto category;
    private UserShortDto initiator;
    private Boolean paid;
    private Long confirmedRequests;
    private Long views;
}
