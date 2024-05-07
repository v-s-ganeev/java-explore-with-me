package ru.practicum.category.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
public class CategoryDto {
    private Long id;
    @Size(max = 50)
    @NotBlank
    private String name;
}
