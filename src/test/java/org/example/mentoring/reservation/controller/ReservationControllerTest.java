package org.example.mentoring.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.reservation.dto.ReservationStatusUpdateRequestDto;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.service.ReservationService;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
        controllers = ReservationController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
public class ReservationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("예약 상태 변경 성공")
    void update_reservation_state_success() throws Exception {
        ReservationStatusUpdateRequestDto req = new ReservationStatusUpdateRequestDto(
                ReservationStatus.CONFIRMED
        );
        ReservationSummaryResponseDto res = new ReservationSummaryResponseDto(
                1L,
                ReservationStatus.CONFIRMED,
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                1L,
                "게시글 1 제목",
                1L,
                "멘토 닉네임 1"
                , SlotStatus.BOOKED
        );

        given(reservationService.updateReservationStatus(any(), any(), any())).willReturn(res);

        mockMvc.perform(patch("/api/reservations/{id}/status", 1L)
                        .with(authentication(authOf(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.listingTitle").value("게시글 1 제목"))
                .andExpect(jsonPath("$.slotStatus").value("BOOKED"));
    }

    @Test
    @DisplayName("예약 상태 변경 실패(권한 없음)")
    void update_reservation_state_auth_fail() throws Exception {
        ReservationStatusUpdateRequestDto req = new ReservationStatusUpdateRequestDto(
                ReservationStatus.CONFIRMED
        );

        given(reservationService.updateReservationStatus(any(), any(), any())).willThrow(new BusinessException(ErrorCode.AUTH_FORBIDDEN));

        mockMvc.perform(patch("/api/reservations/{id}/status", 1L)
                        .with(authentication(authOf(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_007"));
    }

    @Test
    @DisplayName("예약 상태 변경 실패(잘못된 상태 전이)")
    void update_reservation_state_invalid_fail() throws Exception {
        ReservationStatusUpdateRequestDto req = new ReservationStatusUpdateRequestDto(
                ReservationStatus.CONFIRMED
        );

        given(reservationService.updateReservationStatus(any(), any(), any())).willThrow(new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS_TRANSITION));

        mockMvc.perform(patch("/api/reservations/{id}/status", 1L)
                        .with(authentication(authOf(10L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RESERVATION_002"));
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
