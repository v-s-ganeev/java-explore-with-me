package ru.practicum.participationRequest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.participationRequest.dto.ParticipationRequestDto;
import ru.practicum.participationRequest.mapper.ParticipationRequestMapper;
import ru.practicum.participationRequest.model.ParticipationRequest;
import ru.practicum.participationRequest.model.ParticipationRequestStatus;
import ru.practicum.participationRequest.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User user = checkUser(userId);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено."));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Регистрация на собитие недоступна.");
        }
        if (event.getInitiator().getId() == userId) {
            throw new ConflictException("Инициатору события не нужно подавать запрос на участие в нем");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= event.getConfirmedRequests()) {
            throw new ConflictException("Места на событии закончились");
        }
        if (participationRequestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("Вы уже зарегистрированы на этом событии");
        }
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .build();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(ParticipationRequestStatus.CONFIRMED);
            participationRequest = participationRequestRepository.save(participationRequest);
            event.setConfirmedRequests(participationRequestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED));
            eventRepository.save(event);
            return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(participationRequest));
        } else {
            participationRequest.setStatus(ParticipationRequestStatus.PENDING);
        }
        return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long participationRequestId) {
        checkUser(userId);
        ParticipationRequest participationRequest = participationRequestRepository.findById(participationRequestId)
                .orElseThrow(() -> new NotFoundException("Запрос на участие с id = " + participationRequestId + " не найден."));
        participationRequest.setStatus(ParticipationRequestStatus.CANCELED);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Override
    public List<ParticipationRequestDto> getAllByUserId(Long userId) {
        checkUser(userId);
        return participationRequestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }
}
