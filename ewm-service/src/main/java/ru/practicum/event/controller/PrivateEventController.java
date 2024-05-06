package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.participationRequest.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participationRequest.dto.EventRequestStatusUpdateResult;
import ru.practicum.participationRequest.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@Valid @RequestBody NewEventDto newEventDto, @PathVariable Long userId) {
        return eventService.create(userId, newEventDto);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByInitiator(@Valid @RequestBody UpdateEventDto updateEventDto,
                                               @PathVariable Long userId,
                                               @PathVariable Long eventId) {
        return eventService.updateByInitiator(userId, updateEventDto, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequestsForEventByInitiator(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return eventService.updateStatusRequestsForEventByInitiator(eventRequestStatusUpdateRequest, eventId, userId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByInitiator(@PathVariable Long userId,
                                            @PathVariable Long eventId) {
        return eventService.getByInitiator(userId, eventId);
    }

    @GetMapping
    public List<EventFullDto> getAllEventsByInitiator(@PathVariable Long userId,
                                                      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                      @RequestParam(defaultValue = "10") @Positive Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return eventService.getAllByInitiator(userId, pageRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> get(@PathVariable Long userId,
                                             @PathVariable Long eventId) {
        return eventService.getRequestByEventIdAndInitiator(eventId, userId);
    }

}
