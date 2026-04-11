package org.example.mentoring.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.mentoring.application.type.ApplicationFilter;
import org.example.mentoring.application.type.ApplicationSort;
import org.example.mentoring.application.type.ApplicationView;

@Schema(description = "신청 목록 조회 요청")
public record ApplicationSearchRequestDto(
        @Schema(description = "페이지 번호", example = "0")
        @Min(0) Integer page,
        @Schema(description = "페이지 크기", example = "10")
        @Min(1) @Max(10) Integer size,
        @Schema(description = "조회 관점", example = "MENTEE")
        ApplicationView view,
        @Schema(description = "정렬 기준", example = "LATEST")
        ApplicationSort sort,
        @Schema(description = "상태 필터", example = "PENDING")
        ApplicationFilter filter
) {
}
