package ru.practicum.event.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.model.EventState;
import ru.practicum.participationRequest.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participationRequest.dto.EventRequestStatusUpdateResult;
import ru.practicum.participationRequest.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto updateByInitiator(Long userId, UpdateEventDto updateEventDto, Long eventId);

    EventFullDto updateByAdmin(UpdateEventDto updateEventDto, Long eventId);

    EventFullDto getByInitiator(Long userId, Long eventId);

    EventFullDto getById(Long eventId, HttpServletRequest httpServletRequest);

    List<EventFullDto> getAllByInitiator(Long userId, PageRequest pageRequest);

    List<EventFullDto> getAllByFilterForAdmin(List<Long> userIds, List<Long> categoryIds, List<EventState> states,
                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest);

    List<EventShortDto> getAllByFilterForPublic(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                                                    LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, PageRequest pageRequest,
                                                    HttpServletRequest httpServletRequest);

    List<ParticipationRequestDto> getRequestByEventIdAndInitiator(Long eventId, Long userId);

    EventRequestStatusUpdateResult updateStatusRequestsForEventByInitiator(EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest,
                                                                           Long eventId, Long userId);

}
