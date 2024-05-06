package ru.practicum.compilation.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.event.dto.EventShortDto;

import java.util.Set;

@Data
@Builder
public class CompilationDto {
    private Long id;
    private String title;
    private Boolean pinned;
    private Set<EventShortDto> events;
}
