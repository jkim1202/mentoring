package org.example.mentoring.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.mentoring.application.type.ApplicationFilter;
import org.example.mentoring.application.type.ApplicationSort;
import org.example.mentoring.application.type.ApplicationView;

public record ApplicationSearchRequestDto(
        @Min(0) Integer page,
        @Min(1) @Max(10) Integer size,
        ApplicationView view,
        ApplicationSort sort,
        ApplicationFilter filter
) {
}
