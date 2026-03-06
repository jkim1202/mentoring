package org.example.mentoring.listing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.dto.*;
import org.example.mentoring.listing.entity.ListingStatus;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.service.ListingService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ListingController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ListingService listingService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("생성 성공")
    void create_listing_success() throws Exception {
        ListingCreateRequestDto req = new ListingCreateRequestDto(
                "새 멘토링",
                "Spring",
                60000,
                PlaceType.ONLINE,
                null,
                "실무 중심 멘토링"
        );

        ListingResponseDto res = new ListingResponseDto(
                10L,
                "새 멘토링",
                "Spring",
                60000,
                PlaceType.ONLINE,
                "실무 중심 멘토링",
                null,
                ListingStatus.ACTIVE
        );

        given(listingService.createListing(any(), any(ListingCreateRequestDto.class))).willReturn(res);

        mockMvc.perform(post("/api/listings")
                        .with(authentication(authOf(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("새 멘토링"));
    }

    @Test
    @DisplayName("목록 조회 성공")
    void get_listings_success() throws Exception {
        List<ListingSummaryResponseDto> items = List.of(
                new ListingSummaryResponseDto(1L, "Spring 멘토링", "Spring", 50000, new BigDecimal("4.80"), 12),
                new ListingSummaryResponseDto(2L, "Java 멘토링", "Java", 40000, new BigDecimal("4.60"), 8)
        );

        given(listingService.getListings(any()))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, 10), 2));

        mockMvc.perform(get("/api/listings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "LATEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring 멘토링"))
                .andExpect(jsonPath("$.content[0].reviewCount").value(12));
    }

    @Test
    @DisplayName("상세 조회 성공")
    void get_listing_success() throws Exception {
        ListingResponseDto res = new ListingResponseDto(
                1L,
                "Spring 멘토링",
                "Spring",
                50000,
                PlaceType.ONLINE,
                "실무 위주 멘토링",
                null,
                ListingStatus.ACTIVE
        );

        given(listingService.getListing(1L)).willReturn(res);

        mockMvc.perform(get("/api/listings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.placeType").value("ONLINE"));
    }

    @Test
    @DisplayName("수정 성공")
    void update_listing_success() throws Exception {
        ListingUpdateRequestDto req = new ListingUpdateRequestDto(
                "수정된 제목",
                null,
                null,
                PlaceType.ONLINE,
                null,
                "수정된 설명"
        );

        ListingResponseDto res = new ListingResponseDto(
                1L,
                "수정된 제목",
                "Spring",
                50000,
                PlaceType.ONLINE,
                "수정된 설명",
                null,
                ListingStatus.ACTIVE
        );

        given(listingService.updateListing(eq(1L), any(), any(ListingUpdateRequestDto.class))).willReturn(res);

        mockMvc.perform(patch("/api/listings/1")
                        .with(authentication(authOf(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("수정된 제목"));
    }

    @Test
    @DisplayName("수정 실패 - 권한 없음")
    void update_listing_forbidden() throws Exception {
        ListingUpdateRequestDto req = new ListingUpdateRequestDto(
                "수정된 제목",
                null,
                null,
                null,
                null,
                null
        );

        given(listingService.updateListing(eq(1L), any(), any(ListingUpdateRequestDto.class)))
                .willThrow(new BusinessException(ErrorCode.AUTH_FORBIDDEN));

        mockMvc.perform(patch("/api/listings/1")
                        .with(authentication(authOf(99L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_007"));
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

    @Test
    @DisplayName("상태 변경 성공 - ACTIVE -> INACTIVE")
    void update_listing_status_active_to_inactive_success() throws Exception {
        ListingStatusUpdateRequestDto req = new ListingStatusUpdateRequestDto(ListingStatus.INACTIVE);
        ListingResponseDto res = new ListingResponseDto(
                1L, "제목", "Spring", 50000, PlaceType.ONLINE, "설명", null, ListingStatus.INACTIVE
        );
        given(listingService.updateStatus(eq(1L), any(Long.class), any(ListingStatusUpdateRequestDto.class))).willReturn(res);

        mockMvc.perform(patch("/api/listings/1/status")
                        .with(authentication(authOf(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("상태 변경 실패 - DELETE -> ACTIVE")
    void update_listing_status_delete_to_active_fail() throws Exception {
        ListingStatusUpdateRequestDto req = new ListingStatusUpdateRequestDto(ListingStatus.ACTIVE);
        ListingResponseDto res = new ListingResponseDto(
                1L, "제목", "Spring", 50000, PlaceType.ONLINE, "설명", null, ListingStatus.DELETED
        );
        given(listingService.updateStatus(eq(1L), any(Long.class), any(ListingStatusUpdateRequestDto.class))).willThrow(new BusinessException(ErrorCode.LISTING_INVALID_STATUS_TRANSITION));

        mockMvc.perform(patch("/api/listings/1/status")
                        .with(authentication(authOf(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LISTING_004"));
    }

}
