package org.example.mentoring.user.controller;

import org.example.mentoring.listing.dto.MyListingSummaryResponseDto;
import org.example.mentoring.listing.entity.ListingStatus;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.service.MyPageService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MyPageController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MyPageService myPageService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("내 등록글 조회 성공")
    void get_my_listings_success() throws Exception {
        List<MyListingSummaryResponseDto> items = List.of(
                new MyListingSummaryResponseDto(
                        1L,
                        "Spring 멘토링",
                        "Spring",
                        50000,
                        ListingStatus.ACTIVE,
                        new BigDecimal("4.80"),
                        12,
                        LocalDateTime.of(2026, 4, 18, 10, 0)
                )
        );

        given(myPageService.getMyListings(any(), any()))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/users/me/listings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "LATEST")
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring 멘토링"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].reviewCount").value(12));
    }

    @Test
    @DisplayName("내 등록글 조회 실패 - 잘못된 페이지 크기")
    void get_my_listings_fail_when_invalid_size() throws Exception {
        mockMvc.perform(get("/api/users/me/listings")
                        .param("page", "0")
                        .param("size", "0")
                        .with(authentication(authOf(10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_001"));
    }

    @Test
    @DisplayName("내 등록글 조회 실패 - 잘못된 정렬 값")
    void get_my_listings_fail_when_invalid_sort() throws Exception {
        mockMvc.perform(get("/api/users/me/listings")
                        .param("sort", "WRONG")
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
