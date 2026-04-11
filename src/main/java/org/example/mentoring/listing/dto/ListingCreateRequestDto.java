package org.example.mentoring.listing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.mentoring.listing.entity.PlaceType;

@Schema(description = "등록글 생성 요청")
public record ListingCreateRequestDto(
        @Schema(description = "등록글 제목", example = "Spring 실무 멘토링")
        @NotBlank
        @Size(max = 120)
        String title,
        @Schema(description = "멘토링 주제", example = "Spring")
        @NotBlank
        @Size(max = 80)
        String topic,
        @Schema(description = "멘토링 가격", example = "60000")
        @NotNull
        @Min(0)
        Integer price,
        @Schema(description = "장소 유형", example = "ONLINE")
        @NotNull
        PlaceType placeType,
        @Schema(description = "오프라인 장소 설명", example = "강남역 스터디룸")
        @Size(max = 255)
        String placeDesc,
        @Schema(description = "멘토링 상세 설명", example = "실무 중심으로 코드 리뷰와 설계 피드백을 진행합니다.")
        @NotBlank
        @Size(max = 5000)
        String description
) {
}
