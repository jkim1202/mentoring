package org.example.mentoring.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.reservation.dto.ReservationMessageCreateRequestDto;
import org.example.mentoring.reservation.dto.ReservationMessageCreateResponseDto;
import org.example.mentoring.reservation.service.ReservationMessageService;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReservationMessageController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ReservationMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationMessageService reservationMessageService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("예약 메시지 생성 성공")
    void create_message_success() throws Exception {
        ReservationMessageCreateRequestDto requestDto = new ReservationMessageCreateRequestDto("안녕하세요.");
        ReservationMessageCreateResponseDto responseDto = new ReservationMessageCreateResponseDto(1L, 10L, 2L, "안녕하세요.");

        given(reservationMessageService.createMessage(any(), any(), any())).willReturn(responseDto);

        mockMvc.perform(post("/api/reservations/{reservationId}/messages", 10L)
                        .with(authentication(authOf(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageId").value(1L))
                .andExpect(jsonPath("$.reservationId").value(10L))
                .andExpect(jsonPath("$.senderUserId").value(2L))
                .andExpect(jsonPath("$.content").value("안녕하세요."));
    }

    @Test
    @DisplayName("예약 당사자가 아니면 메시지 생성 실패")
    void create_message_fail_when_not_participant() throws Exception {
        ReservationMessageCreateRequestDto requestDto = new ReservationMessageCreateRequestDto("안녕하세요.");

        given(reservationMessageService.createMessage(any(), any(), any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_FORBIDDEN));

        mockMvc.perform(post("/api/reservations/{reservationId}/messages", 10L)
                        .with(authentication(authOf(3L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_007"));
    }

    @Test
    @DisplayName("메시지 전송 불가 상태면 생성 실패")
    void create_message_fail_when_not_writable() throws Exception {
        ReservationMessageCreateRequestDto requestDto = new ReservationMessageCreateRequestDto("안녕하세요.");

        given(reservationMessageService.createMessage(any(), any(), any()))
                .willThrow(new BusinessException(ErrorCode.RESERVATION_MESSAGE_NOT_WRITABLE));

        mockMvc.perform(post("/api/reservations/{reservationId}/messages", 10L)
                        .with(authentication(authOf(2L)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RESERVATION_006"));
    }

    private UsernamePasswordAuthenticationToken authOf(Long userId) {
        MentoringUserDetails userDetails = new MentoringUserDetails(
                userId,
                "user@test.com",
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
