package ru.practicum.category.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(CategoryDto categoryDto);

    CategoryDto update(CategoryDto categoryDto, Long categoryId);

    void delete(Long categoryId);

    CategoryDto getById(Long categoryId);

    List<CategoryDto> getAllCategory(PageRequest pageRequest);
}
