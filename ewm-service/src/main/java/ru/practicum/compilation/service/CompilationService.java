package ru.practicum.compilation.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationDto;

import java.util.List;

public interface CompilationService {

    CompilationDto create(NewCompilationDto newCompilationDto);

    CompilationDto update(Long compilationId, UpdateCompilationDto updateCompilationDto);

    void delete(Long compilationId);

    CompilationDto getById(Long compilationId);

    List<CompilationDto> getAllPinnedCompilations(Boolean pinned, PageRequest pageRequest);
}
