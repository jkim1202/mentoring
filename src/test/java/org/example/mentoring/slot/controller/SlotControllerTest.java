package org.example.mentoring.slot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.slot.dto.SlotResponseDto;
import org.example.mentoring.slot.dto.SlotUpdateRequestDto;
import org.example.mentoring.slot.service.SlotService;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SlotController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class SlotControllerTest {

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
    @DisplayName("슬롯 수정 성공")
    void update_slot_success() throws Exception {
        SlotUpdateRequestDto requestDto = new SlotUpdateRequestDto(
                LocalDateTime.of(2026, 4, 22, 14, 0),
                LocalDateTime.of(2026, 4, 22, 15, 0)
        );
        SlotResponseDto responseDto = new SlotResponseDto(
                100L,
                10L,
                LocalDateTime.of(2026, 4, 22, 14, 0),
                LocalDateTime.of(2026, 4, 22, 15, 0),
                org.example.mentoring.listing.entity.SlotStatus.OPEN
        );

        given(slotService.updateSlot(any(), any(), any())).willReturn(responseDto);

        mockMvc.perform(patch("/api/slots/{slotId}", 100L)
                        .with(authentication(authOf(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotId").value(100L))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("슬롯 수정 실패 - 권한 없음")
    void update_slot_forbidden() throws Exception {
        SlotUpdateRequestDto requestDto = new SlotUpdateRequestDto(
                LocalDateTime.of(2026, 4, 22, 14, 0),
                LocalDateTime.of(2026, 4, 22, 15, 0)
        );

        given(slotService.updateSlot(any(), any(), any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_FORBIDDEN));

        mockMvc.perform(patch("/api/slots/{slotId}", 100L)
                        .with(authentication(authOf(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_007"));
    }

    @Test
    @DisplayName("슬롯 수정 실패 - 이미 예약된 슬롯")
    void update_slot_fail_when_booked() throws Exception {
        SlotUpdateRequestDto requestDto = new SlotUpdateRequestDto(
                LocalDateTime.of(2026, 4, 22, 14, 0),
                LocalDateTime.of(2026, 4, 22, 15, 0)
        );

        given(slotService.updateSlot(any(), any(), any()))
                .willThrow(new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED));

        mockMvc.perform(patch("/api/slots/{slotId}", 100L)
                        .with(authentication(authOf(1L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SLOT_003"));
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
