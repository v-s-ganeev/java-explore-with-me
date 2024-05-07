package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        if (newCompilationDto.getEvents() == null || newCompilationDto.getEvents().isEmpty()) {
            compilation.setEvents(Collections.emptySet());
        } else {
            compilation.setEvents(eventRepository.findByIdIn(newCompilationDto.getEvents()));
        }
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto update(Long compilationId, UpdateCompilationDto updateCompilationDto) {
        Compilation updatedCompilation = checkCompilation(compilationId);
        if (updateCompilationDto.getTitle() != null) {
            updatedCompilation.setTitle(updateCompilationDto.getTitle());
        }
        if (updateCompilationDto.getPinned() != null) {
            updatedCompilation.setPinned(updateCompilationDto.getPinned());
        }
        if (updateCompilationDto.getEvents() == null || updateCompilationDto.getEvents().isEmpty()) {
            updateCompilationDto.setEvents(Collections.emptySet());
        } else {
            updatedCompilation.setEvents(eventRepository.findByIdIn(updateCompilationDto.getEvents()));
        }
        return CompilationMapper.toCompilationDto(compilationRepository.save(updatedCompilation));
    }

    @Override
    @Transactional
    public void delete(Long compilationId) {
        checkCompilation(compilationId);
        compilationRepository.deleteById(compilationId);
    }

    @Override
    public CompilationDto getById(Long compilationId) {
        return CompilationMapper.toCompilationDto(checkCompilation(compilationId));
    }

    @Override
    public List<CompilationDto> getAllPinnedCompilations(Boolean pinned, PageRequest pageRequest) {
        return compilationRepository.findAllByPinned(pinned, pageRequest).stream()
                .map(CompilationMapper::toCompilationDto)
                .collect(Collectors.toList());
    }

    private Compilation checkCompilation(Long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() -> new NotFoundException("Подборка с id = " + compilationId + " не найдена."));
    }
}
