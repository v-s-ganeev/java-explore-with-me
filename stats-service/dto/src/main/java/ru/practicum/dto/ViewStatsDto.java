package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDto {
    @NotNull
    @NotBlank
    private String app;
    @NotNull
    @NotBlank
    private String uri;
    @NotNull
    @PositiveOrZero
    private Long hits;
}
