package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.exception.ValidationException;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public EndpointHitDto addEndpointHit(EndpointHitDto endpointHitDto) {
        return EndpointHitMapper.toEndpointHitDto(statsRepository.save(EndpointHitMapper.toEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean isUnique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Начало искомого периода не может быть после конца");
        }
        if (uris == null) {
            if (isUnique) {
                return statsRepository.getStatsByUniqueIp(start, end);
            } else {
                return statsRepository.getAllStats(start, end);
            }
        } else {
            if (isUnique) {
                return statsRepository.getStatsByUrisAndUniqueIp(start, end, uris);
            } else {
                return statsRepository.getStatsByUris(start, end, uris);
            }
        }
    }
}
