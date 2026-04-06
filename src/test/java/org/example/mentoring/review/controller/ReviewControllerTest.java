package org.example.mentoring.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.review.dto.ReviewCreateRequestDto;
import org.example.mentoring.review.dto.ReviewCreateResponseDto;
import org.example.mentoring.review.dto.ReviewDetailResponseDto;
import org.example.mentoring.review.dto.ReviewSummaryResponseDto;
import org.example.mentoring.review.service.ReviewService;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReviewController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("리뷰 생성 성공")
    void create_review_success() throws Exception {
        ReviewCreateRequestDto requestDto = new ReviewCreateRequestDto(1L, 5, "도움이 많이 됐습니다.");
        ReviewCreateResponseDto responseDto = new ReviewCreateResponseDto(
                10L,
                5,
                "도움이 많이 됐습니다.",
                LocalDateTime.of(2026, 4, 6, 12, 0)
        );

        given(reviewService.createReview(any(), any())).willReturn(responseDto);

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(authOf(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(10L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("도움이 많이 됐습니다."));
    }

    @Test
    @DisplayName("완료되지 않은 예약에는 리뷰 생성 실패")
    void create_review_fail_when_reservation_not_completed() throws Exception {
        ReviewCreateRequestDto requestDto = new ReviewCreateRequestDto(1L, 5, "도움이 많이 됐습니다.");

        given(reviewService.createReview(any(), any()))
                .willThrow(new BusinessException(ErrorCode.RESERVATION_NOT_COMPLETED));

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(authOf(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RESERVATION_005"));
    }

    @Test
    @DisplayName("중복 리뷰 생성 실패")
    void create_review_fail_when_review_already_exists() throws Exception {
        ReviewCreateRequestDto requestDto = new ReviewCreateRequestDto(1L, 5, "도움이 많이 됐습니다.");

        given(reviewService.createReview(any(), any()))
                .willThrow(new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS));

        mockMvc.perform(post("/api/reviews")
                        .with(authentication(authOf(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVIEW_001"));
    }

    @Test
    @DisplayName("리뷰 상세 조회 성공")
    void get_review_success() throws Exception {
        ReviewDetailResponseDto responseDto = new ReviewDetailResponseDto(
                10L,
                1L,
                100L,
                "Spring 멘토링",
                2L,
                "멘티닉네임",
                5,
                "도움이 많이 됐습니다.",
                LocalDateTime.of(2026, 4, 6, 12, 0)
        );

        given(reviewService.getReview(10L)).willReturn(responseDto);

        mockMvc.perform(get("/api/reviews/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(10L))
                .andExpect(jsonPath("$.listingTitle").value("Spring 멘토링"))
                .andExpect(jsonPath("$.reviewerNickname").value("멘티닉네임"));
    }

    @Test
    @DisplayName("없는 리뷰 상세 조회 실패")
    void get_review_fail_when_not_found() throws Exception {
        given(reviewService.getReview(999L))
                .willThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        mockMvc.perform(get("/api/reviews/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REVIEW_002"));
    }

    @Test
    @DisplayName("리뷰 목록 조회 성공")
    void get_reviews_success() throws Exception {
        List<ReviewSummaryResponseDto> items = List.of(
                new ReviewSummaryResponseDto(10L, 2L, "멘티1", 5, "첫 번째 리뷰", LocalDateTime.of(2026, 4, 6, 12, 0)),
                new ReviewSummaryResponseDto(11L, 3L, "멘티2", 4, "두 번째 리뷰", LocalDateTime.of(2026, 4, 5, 12, 0))
        );

        given(reviewService.getReviews(any()))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, 10), items.size()));

        mockMvc.perform(get("/api/reviews")
                        .param("listingId", "100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reviewId").value(10L))
                .andExpect(jsonPath("$.content[0].reviewerNickname").value("멘티1"))
                .andExpect(jsonPath("$.content[1].reviewId").value(11L));
    }

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        MentoringUserDetails userDetails = new MentoringUserDetails(
                userId,
                "mentee@test.com",
                "pw",
                UserStatus.ACTIVE,
                List.of()
        );

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
