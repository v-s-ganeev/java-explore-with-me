package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable Long eventId,
                                     HttpServletRequest request) {
        return eventService.getById(eventId, request);
    }

    @GetMapping
    public List<EventShortDto> getAllEventsByFilterForPublic(@RequestParam(required = false) String text,
                                                             @RequestParam(required = false) List<Long> categories,
                                                             @RequestParam(required = false) Boolean paid,
                                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                             @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                             @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                             @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                             @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                             @RequestParam(defaultValue = "10") @Positive Integer size,
                                                             HttpServletRequest httpServletRequest) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return eventService.getAllByFilterForPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageRequest, httpServletRequest);
    }
}
