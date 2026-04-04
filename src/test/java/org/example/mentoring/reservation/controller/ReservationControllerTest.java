package org.example.mentoring.reservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.reservation.dto.ReservationDetailResponseDto;
import org.example.mentoring.reservation.dto.ReservationSearchRequestDto;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.service.ReservationService;
import org.example.mentoring.reservation.type.ReservationFilter;
import org.example.mentoring.reservation.type.ReservationSort;
import org.example.mentoring.reservation.type.ReservationView;
import org.example.mentoring.security.JwtAuthenticationFilter;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("예약 입금 표시 성공")
    void mark_paid_success() throws Exception {
        ReservationSummaryResponseDto res = new ReservationSummaryResponseDto(
                1L,
                ReservationStatus.PENDING_PAYMENT,
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                1L,
                "게시글 1 제목",
                1L,
                "멘토 닉네임 1"
                , SlotStatus.BOOKED
        );

        given(reservationService.markPaid(any(), any())).willReturn(res);

        mockMvc.perform(patch("/api/reservations/{id}/mark-paid", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.reservationStatus").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.listingTitle").value("게시글 1 제목"))
                .andExpect(jsonPath("$.slotStatus").value("BOOKED"));
    }

    @Test
    @DisplayName("예약 입금 확인 성공")
    void confirm_paid_success() throws Exception {
        ReservationSummaryResponseDto res = new ReservationSummaryResponseDto(
                1L,
                ReservationStatus.CONFIRMED,
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                1L,
                "게시글 1 제목",
                1L,
                "멘티 닉네임 1",
                SlotStatus.BOOKED
        );

        given(reservationService.confirmPaid(any(), any())).willReturn(res);

        mockMvc.perform(patch("/api/reservations/{id}/confirm-paid", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.listingTitle").value("게시글 1 제목"))
                .andExpect(jsonPath("$.slotStatus").value("BOOKED"));
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancel_reservation_success() throws Exception {
        ReservationSummaryResponseDto res = new ReservationSummaryResponseDto(
                1L,
                ReservationStatus.CANCELED,
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                1L,
                "게시글 1 제목",
                1L,
                "멘티 닉네임 1",
                SlotStatus.OPEN
        );

        given(reservationService.cancelReservation(any(), any())).willReturn(res);

        mockMvc.perform(patch("/api/reservations/{id}/cancel", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.reservationStatus").value("CANCELED"))
                .andExpect(jsonPath("$.slotStatus").value("OPEN"));
    }

    @Test
    @DisplayName("예약 취소 실패(멘티 취소 가능 시간 초과)")
    void cancel_reservation_deadline_fail() throws Exception {
        given(reservationService.cancelReservation(any(), any()))
                .willThrow(new BusinessException(ErrorCode.RESERVATION_CANCEL_DEADLINE_EXCEEDED));

        mockMvc.perform(patch("/api/reservations/{id}/cancel", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RESERVATION_004"));
    }

    @Test
    @DisplayName("예약 완료 실패(권한 없음)")
    void complete_reservation_auth_fail() throws Exception {
        given(reservationService.completeReservation(any(), any()))
                .willThrow(new BusinessException(ErrorCode.AUTH_FORBIDDEN));

        mockMvc.perform(patch("/api/reservations/{id}/complete", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_007"));
    }

    @Test
    @DisplayName("예약 완료 실패(잘못된 상태 전이)")
    void complete_reservation_invalid_fail() throws Exception {
        given(reservationService.completeReservation(any(), any()))
                .willThrow(new BusinessException(ErrorCode.RESERVATION_INVALID_STATUS_TRANSITION));

        mockMvc.perform(patch("/api/reservations/{id}/complete", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RESERVATION_002"));
    }

    @Test
    @DisplayName("예약 상세 조회 성공")
    void get_reservation_success() throws Exception {
        ReservationDetailResponseDto res = new ReservationDetailResponseDto(
                1L,
                ReservationStatus.CONFIRMED,
                LocalDateTime.of(2026, 3, 15, 10, 0),
                LocalDateTime.of(2026, 3, 15, 11, 0),
                10L,
                "Spring 멘토링",
                "Spring Boot",
                50000,
                PlaceType.OFFLINE,
                "강남역",
                2L,
                "멘티 1",
                SlotStatus.BOOKED
        );

        given(reservationService.getReservation(any(), any())).willReturn(res);

        mockMvc.perform(get("/api/reservations/{id}", 1L)
                        .with(authentication(authOf(10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(1))
                .andExpect(jsonPath("$.reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.listingTitle").value("Spring 멘토링"))
                .andExpect(jsonPath("$.listingTopic").value("Spring Boot"))
                .andExpect(jsonPath("$.listingPrice").value(50000))
                .andExpect(jsonPath("$.placeType").value("OFFLINE"))
                .andExpect(jsonPath("$.placeDesc").value("강남역"))
                .andExpect(jsonPath("$.partnerUserId").value(2))
                .andExpect(jsonPath("$.partnerNickname").value("멘티 1"))
                .andExpect(jsonPath("$.slotStatus").value("BOOKED"));
    }

    @Test
    @DisplayName("예약 전체 조회 성공")
    void get_reservations_success() throws Exception {
        List<ReservationSummaryResponseDto> items = List.of(
                new ReservationSummaryResponseDto(1L,
                        ReservationStatus.CONFIRMED,
                        LocalDateTime.of(2026, 3, 15, 10, 0),
                        LocalDateTime.of(2026, 3, 15, 11, 0),
                        10L,
                        "Spring 멘토링",
                        1L,
                        "멘티 1",
                        SlotStatus.BOOKED
                ),
                new ReservationSummaryResponseDto(2L,
                        ReservationStatus.CONFIRMED,
                        LocalDateTime.of(2026, 3, 15, 12, 0),
                        LocalDateTime.of(2026, 3, 15, 13, 0),
                        20L,
                        "Java 멘토링",
                        2L,
                        "멘티 2",
                        SlotStatus.BOOKED
                ));
        given(reservationService.getReservations(any(), any()))
                .willReturn(new PageImpl<>(items, PageRequest.of(0, items.size()), items.size()));

        mockMvc.perform(get("/api/reservations")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "SOONEST")
                        .param("view", "MENTOR")
                        .param("filter", "UPCOMING")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].reservationId").value(1))
                .andExpect(jsonPath("$.content[0].reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.content[0].listingTitle").value("Spring 멘토링"))
                .andExpect(jsonPath("$.content[0].partnerNickname").value("멘티 1"))
                .andExpect(jsonPath("$.content[1].reservationId").value(2))
                .andExpect(jsonPath("$.content[1].reservationStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.content[1].listingTitle").value("Java 멘토링"))
                .andExpect(jsonPath("$.content[1].partnerNickname").value("멘티 2"));


        ArgumentCaptor<ReservationSearchRequestDto> requestCaptor =
                ArgumentCaptor.forClass(ReservationSearchRequestDto.class);

        then(reservationService).should().getReservations(requestCaptor.capture(), any());

        ReservationSearchRequestDto captured = requestCaptor.getValue();
        assertThat(captured.page()).isEqualTo(0);
        assertThat(captured.size()).isEqualTo(10);
        assertThat(captured.view()).isEqualTo(ReservationView.MENTOR);
        assertThat(captured.sort()).isEqualTo(ReservationSort.SOONEST);
        assertThat(captured.filter()).isEqualTo(ReservationFilter.UPCOMING);

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
