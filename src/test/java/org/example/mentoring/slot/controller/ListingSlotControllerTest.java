package org.example.mentoring.slot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.slot.dto.SlotCreateRequestDto;
import org.example.mentoring.slot.dto.SlotResponseDto;
import org.example.mentoring.slot.service.SlotService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ListingSlotController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ListingSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SlotService slotService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("슬롯 생성 성공")
    void create_slot_success() throws Exception {
        SlotCreateRequestDto requestDto = new SlotCreateRequestDto(
                LocalDateTime.of(2026, 4, 20, 14, 0),
                LocalDateTime.of(2026, 4, 20, 15, 0)
        );
        SlotResponseDto responseDto = new SlotResponseDto(
                100L,
                10L,
                LocalDateTime.of(2026, 4, 20, 14, 0),
                LocalDateTime.of(2026, 4, 20, 15, 0),
                org.example.mentoring.listing.entity.SlotStatus.OPEN
        );

        given(slotService.createSlot(any(), any(), any())).willReturn(responseDto);

        mockMvc.perform(post("/api/listings/{listingId}/slots", 10L)
                        .with(authentication(authOf(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slotId").value(100L))
                .andExpect(jsonPath("$.listingId").value(10L))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("슬롯 생성 실패 - 권한 없음")
    void create_slot_forbidden() throws Exception {
        SlotCreateRequestDto requestDto = new SlotCreateRequestDto(
                LocalDateTime.of(2026, 4, 20, 14, 0),
                LocalDateTime.of(2026, 4, 20, 15, 0)
        );

        given(slotService.createSlot(any(), any(), any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_FORBIDDEN));

        mockMvc.perform(post("/api/listings/{listingId}/slots", 10L)
                        .with(authentication(authOf(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_007"));
    }

    @Test
    @DisplayName("슬롯 생성 실패 - 시간 범위 오류")
    void create_slot_fail_when_invalid_time_range() throws Exception {
        SlotCreateRequestDto requestDto = new SlotCreateRequestDto(
                LocalDateTime.of(2026, 4, 20, 15, 0),
                LocalDateTime.of(2026, 4, 20, 15, 0)
        );

        given(slotService.createSlot(any(), any(), any()))
                .willThrow(new BusinessException(ErrorCode.SLOT_INVALID_TIME_RANGE));

        mockMvc.perform(post("/api/listings/{listingId}/slots", 10L)
                        .with(authentication(authOf(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SLOT_006"));
    }

    @Test
    @DisplayName("슬롯 생성 실패 - 과거 시작 시각")
    void create_slot_fail_when_start_at_in_past() throws Exception {
        SlotCreateRequestDto requestDto = new SlotCreateRequestDto(
                LocalDateTime.of(2026, 4, 20, 13, 0),
                LocalDateTime.of(2026, 4, 20, 14, 0)
        );

        given(slotService.createSlot(any(), any(), any()))
                .willThrow(new BusinessException(ErrorCode.SLOT_START_AT_IN_PAST));

        mockMvc.perform(post("/api/listings/{listingId}/slots", 10L)
                        .with(authentication(authOf(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SLOT_007"));
    }

    @Test
    @DisplayName("슬롯 목록 조회 성공")
    void get_slots_success() throws Exception {
        List<SlotResponseDto> items = List.of(
                new SlotResponseDto(
                        100L,
                        10L,
                        LocalDateTime.of(2026, 4, 20, 14, 0),
                        LocalDateTime.of(2026, 4, 20, 15, 0),
                        org.example.mentoring.listing.entity.SlotStatus.OPEN
                ),
                new SlotResponseDto(
                        101L,
                        10L,
                        LocalDateTime.of(2026, 4, 21, 14, 0),
                        LocalDateTime.of(2026, 4, 21, 15, 0),
                        org.example.mentoring.listing.entity.SlotStatus.OPEN
                )
        );

        given(slotService.getSlots(10L, null, null, 0, 20))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, 20), items.size()));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/listings/{listingId}/slots", 10L)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slotId").value(100L))
                .andExpect(jsonPath("$.content[1].slotId").value(101L));
    }

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        MentoringUserDetails userDetails = new MentoringUserDetails(
                userId,
                "mentor@test.com",
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
