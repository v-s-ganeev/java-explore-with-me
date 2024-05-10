package ru.practicum.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatsServiceClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.participationRequest.dto.EventRequestStatusUpdateRequest;
import ru.practicum.participationRequest.dto.EventRequestStatusUpdateResult;
import ru.practicum.participationRequest.dto.ParticipationRequestDto;
import ru.practicum.participationRequest.mapper.ParticipationRequestMapper;
import ru.practicum.participationRequest.model.ParticipationRequest;
import ru.practicum.participationRequest.model.ParticipationRequestStatus;
import ru.practicum.participationRequest.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsServiceClient statsServiceClient;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User user = checkUser(userId);
        Category category = checkCategory(newEventDto.getCategory());
        Location location = locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation()));
        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateByInitiator(Long userId, UpdateEventDto updateEventDto, Long eventId) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Обновить событие может только инициатор");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие уже опубликовано и не может быть обновлено");
        }
        return EventMapper.toEventFullDto(updateEvent(event, updateEventDto));
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(UpdateEventDto updateEventDto, Long eventId) {
        return EventMapper.toEventFullDto(updateEvent(checkEvent(eventId), updateEventDto));
    }

    @Override
    public EventFullDto getByInitiator(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " и initiatorId = " + userId + " не найдено"));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = checkEvent(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие c id = " + eventId + " еще не опубликовано");
        }
        addEndpointHit(httpServletRequest);
        event.setViews(getStats(event));
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getAllByInitiator(Long userId, PageRequest pageRequest) {
        checkUser(userId);
        return eventRepository.findAllByInitiatorId(userId, pageRequest).stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    @Override
    public List<EventFullDto> getAllByFilterForAdmin(List<Long> userIds, List<Long> categoryIds, List<EventState> states, LocalDateTime rangeStart, LocalDateTime rangeEnd, PageRequest pageRequest) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Начала периода не может быть раньше его конца");
        }
        return eventRepository.getEventsByAdmin(userIds, states, categoryIds, rangeStart, rangeEnd, pageRequest).stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getAllByFilterForPublic(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, PageRequest pageRequest, HttpServletRequest httpServletRequest) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Начала периода не может быть раньше его конца");
        }
        List<Event> events = eventRepository.getEventsByPublic(text, categoryIds, paid, rangeStart, rangeEnd, onlyAvailable, sort, pageRequest);
        addEndpointHit(httpServletRequest);
        for (Event event : events) {
            event.setViews(getStats(event));
            eventRepository.save(event);
        }
        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    public List<ParticipationRequestDto> getRequestByEventIdAndInitiator(Long eventId, Long userId) {
        checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Вы не инициатор события");
        }
        return participationRequestRepository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequestsForEventByInitiator(EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest, Long eventId, Long userId) {
        checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Вы не являетесь инициатором события с id = " + eventId);
        }
        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            return result;
        }
        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Места на событии закончились");
        }

        Long availableSeats = event.getParticipantLimit() - event.getConfirmedRequests();

        List<ParticipationRequest> requests = participationRequestRepository.findAllById(eventRequestStatusUpdateRequest.getRequestIds());

        for (ParticipationRequest participationRequest : requests) {
            if (!participationRequest.getStatus().equals(ParticipationRequestStatus.PENDING)) {
                throw new ConflictException("Запрос с id = " + participationRequest.getId() + " не находится в статусе PENDING");
            }
            if (eventRequestStatusUpdateRequest.getStatus().equals(ParticipationRequestStatus.CONFIRMED) && availableSeats > 0) {
                participationRequest.setStatus(ParticipationRequestStatus.CONFIRMED);
                result.getConfirmedRequests().add(ParticipationRequestMapper.toParticipationRequestDto(participationRequest));
                availableSeats--;
            } else {
                participationRequest.setStatus(ParticipationRequestStatus.REJECTED);
                result.getRejectedRequests().add(ParticipationRequestMapper.toParticipationRequestDto(participationRequest));
            }

        }
        event.setConfirmedRequests(participationRequestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED));
        eventRepository.save(event);
        participationRequestRepository.saveAll(requests);
        return result;
    }

    private Event updateEvent(Event event, UpdateEventDto updateEventDto) {
        if (updateEventDto.getStateAction() != null) {
            if (updateEventDto.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
                if (event.getState().equals(EventState.PUBLISHED) || event.getState().equals(EventState.CANCELED)) {
                    throw new ConflictException("Можно опубликовать только ожидающее публикации событие");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventDto.getStateAction().equals(StateAction.REJECT_EVENT) ||
                    updateEventDto.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ConflictException("Нельзя отменить опубликованное событие");
                }
                event.setState(EventState.CANCELED);
            } else if (updateEventDto.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
        }
        if (updateEventDto.getTitle() != null && !updateEventDto.getTitle().isBlank()) {
            event.setTitle(updateEventDto.getTitle());
        }
        if (updateEventDto.getAnnotation() != null && !updateEventDto.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventDto.getAnnotation());
        }
        if (updateEventDto.getDescription() != null && !updateEventDto.getDescription().isBlank()) {
            event.setDescription(updateEventDto.getDescription());
        }
        if (updateEventDto.getEventDate() != null) {
            event.setEventDate(updateEventDto.getEventDate());
        }
        if (updateEventDto.getCategory() != null) {
            event.setCategory(checkCategory(updateEventDto.getCategory()));
        }
        if (updateEventDto.getLocation() != null) {
            event.setLocation(LocationMapper.toLocation(updateEventDto.getLocation()));
            locationRepository.save(event.getLocation());
        }
        if (updateEventDto.getPaid() != null) {
            event.setPaid(updateEventDto.getPaid());
        }
        if (updateEventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventDto.getParticipantLimit());
        }
        if (updateEventDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventDto.getRequestModeration());
        }
        return eventRepository.save(event);
    }

    private void addEndpointHit(HttpServletRequest httpServletRequest) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("main-service")
                .uri(httpServletRequest.getRequestURI())
                .ip(httpServletRequest.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsServiceClient.addEndpointHit(endpointHitDto);
    }

    private Long getStats(Event event) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            ResponseEntity<Object> response = statsServiceClient.getStats(event.getPublishedOn(), LocalDateTime.now(),
                    List.of("/events/" + event.getId()), true);
            List<ViewStatsDto> result = objectMapper.convertValue(response.getBody(), new TypeReference<>() {});
            if (!result.isEmpty() && result.get(0).getHits() != null) {
                return result.get(0).getHits();
            }
        }
        return 1L;
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    private Category checkCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Категория с id = " + categoryId + " не найдена."));
    }
}
