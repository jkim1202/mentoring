package org.example.mentoring.like.controller;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.like.dto.LikeDetailsDto;
import org.example.mentoring.like.dto.LikeSummaryResponseDto;
import org.example.mentoring.like.service.LikeService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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
        controllers = LikeController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LikeService likeService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("찜 토글 성공")
    void toggle_like_success() throws Exception {
        given(likeService.toggleLike(any(), any()))
                .willReturn(new LikeDetailsDto(10L, 1L, true));

        mockMvc.perform(post("/api/listings/{listingId}/like", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10L))
                .andExpect(jsonPath("$.listingId").value(1L))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    @DisplayName("본인 게시글 찜 토글 실패")
    void toggle_like_fail_when_own_listing() throws Exception {
        given(likeService.toggleLike(any(), any()))
                .willThrow(new BusinessException(ErrorCode.LIKE_SELF_NOT_ALLOWED));

        mockMvc.perform(post("/api/listings/{listingId}/like", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("LIKE_001"));
    }

    @Test
    @DisplayName("내 찜 목록 조회 성공")
    void get_my_likes_success() throws Exception {
        List<LikeSummaryResponseDto> items = List.of(
                new LikeSummaryResponseDto(
                        1L,
                        "Spring 멘토링",
                        "Spring",
                        50000,
                        new BigDecimal("4.80"),
                        12,
                        LocalDateTime.of(2026, 4, 27, 13, 0),
                        true
                )
        );

        given(likeService.getMyLikes(any(), any()))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/users/me/likes")
                        .param("page", "0")
                        .param("size", "10")
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].listingId").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Spring 멘토링"))
                .andExpect(jsonPath("$.content[0].liked").value(true));
    }

    @Test
    @DisplayName("내 찜 목록 조회 실패 - 잘못된 페이지 크기")
    void get_my_likes_fail_when_invalid_size() throws Exception {
        mockMvc.perform(get("/api/users/me/likes")
                        .param("size", "0")
                        .with(authentication(authOf(10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        MentoringUserDetails principal = new MentoringUserDetails(
                userId,
                "test@example.com",
                "encoded-password",
                UserStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
