package org.example.mentoring.reservation.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.reservation.dto.ReservationMessageCreateRequestDto;
import org.example.mentoring.reservation.dto.ReservationMessageCreateResponseDto;
import org.example.mentoring.reservation.dto.ReservationMessageSearchRequestDto;
import org.example.mentoring.reservation.dto.ReservationMessageSearchResponseDto;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationMessage;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationMessageRepository;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReservationMessageServiceTest {

    @InjectMocks
    private ReservationMessageService reservationMessageService;

    @Mock
    private ReservationMessageRepository reservationMessageRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<ReservationMessage> reservationMessageCaptor;
    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    @DisplayName("예약 메시지 생성 성공")
    void create_message_success() {
        User mentor = createUser(1L, "mentor@test.com", "멘토");
        User mentee = createUser(2L, "mentee@test.com", "멘티");
        Reservation reservation = createReservation(10L, mentor, mentee, ReservationStatus.PENDING_PAYMENT);
        ReservationMessageCreateRequestDto requestDto = new ReservationMessageCreateRequestDto("안녕하세요.");
        MentoringUserDetails userDetails = createUserDetails(mentee);

        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(reservationMessageRepository.save(any(ReservationMessage.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ReservationMessageCreateResponseDto response = reservationMessageService.createMessage(10L, requestDto, userDetails);

        then(reservationMessageRepository).should().save(reservationMessageCaptor.capture());
        ReservationMessage savedMessage = reservationMessageCaptor.getValue();

        assertThat(savedMessage.getReservation()).isEqualTo(reservation);
        assertThat(savedMessage.getSender()).isEqualTo(mentee);
        assertThat(savedMessage.getContent()).isEqualTo("안녕하세요.");
        assertThat(response.reservationId()).isEqualTo(10L);
        assertThat(response.senderUserId()).isEqualTo(2L);
        assertThat(response.content()).isEqualTo("안녕하세요.");
    }

    @Test
    @DisplayName("예약 당사자가 아니면 메시지 생성 실패")
    void create_message_fails_when_not_participant() {
        User mentor = createUser(1L, "mentor@test.com", "멘토");
        User mentee = createUser(2L, "mentee@test.com", "멘티");
        User outsider = createUser(3L, "outsider@test.com", "외부인");
        Reservation reservation = createReservation(10L, mentor, mentee, ReservationStatus.CONFIRMED);
        ReservationMessageCreateRequestDto requestDto = new ReservationMessageCreateRequestDto("안녕하세요.");
        MentoringUserDetails userDetails = createUserDetails(outsider);

        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(3L)).willReturn(Optional.of(outsider));

        assertThatThrownBy(() -> reservationMessageService.createMessage(10L, requestDto, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN));

        then(reservationMessageRepository).should(never()).save(any(ReservationMessage.class));
    }

    @Test
    @DisplayName("메시지 전송 불가 상태면 생성 실패")
    void create_message_fails_when_reservation_not_writable() {
        User mentor = createUser(1L, "mentor@test.com", "멘토");
        User mentee = createUser(2L, "mentee@test.com", "멘티");
        Reservation reservation = createReservation(10L, mentor, mentee, ReservationStatus.COMPLETED);
        ReservationMessageCreateRequestDto requestDto = new ReservationMessageCreateRequestDto("안녕하세요.");
        MentoringUserDetails userDetails = createUserDetails(mentee);

        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));

        assertThatThrownBy(() -> reservationMessageService.createMessage(10L, requestDto, userDetails))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.RESERVATION_MESSAGE_NOT_WRITABLE));

        then(reservationMessageRepository).should(never()).save(any(ReservationMessage.class));
    }

    @Test
    @DisplayName("예약 메시지 목록 조회 성공")
    void get_messages_success() {
        User mentor = createUser(1L, "mentor@test.com", "멘토");
        User mentee = createUser(2L, "mentee@test.com", "멘티");
        Reservation reservation = createReservation(10L, mentor, mentee, ReservationStatus.COMPLETED);
        MentoringUserDetails userDetails = createUserDetails(mentee);
        ReservationMessage firstMessage = ReservationMessage.builder()
                .id(100L)
                .reservation(reservation)
                .sender(mentor)
                .content("첫 메시지")
                .createdAt(LocalDateTime.of(2026, 4, 12, 10, 0))
                .build();
        ReservationMessage secondMessage = ReservationMessage.builder()
                .id(101L)
                .reservation(reservation)
                .sender(mentee)
                .content("두 번째 메시지")
                .createdAt(LocalDateTime.of(2026, 4, 12, 10, 1))
                .build();

        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(reservationMessageRepository.findByReservationId(eq(10L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(firstMessage, secondMessage)));

        Page<ReservationMessageSearchResponseDto> response = reservationMessageService.getMessages(
                10L,
                userDetails,
                new ReservationMessageSearchRequestDto(10, 0)
        );

        then(reservationMessageRepository).should().findByReservationId(eq(10L), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).messageId()).isEqualTo(100L);
        assertThat(response.getContent().get(0).senderUserId()).isEqualTo(1L);
        assertThat(response.getContent().get(0).content()).isEqualTo("첫 메시지");
        assertThat(response.getContent().get(1).messageId()).isEqualTo(101L);
        assertThat(response.getContent().get(1).senderUserId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("예약 메시지 목록 조회는 기본 페이지 조건을 사용한다")
    void get_messages_uses_default_page_request() {
        User mentor = createUser(1L, "mentor@test.com", "멘토");
        User mentee = createUser(2L, "mentee@test.com", "멘티");
        Reservation reservation = createReservation(10L, mentor, mentee, ReservationStatus.COMPLETED);
        MentoringUserDetails userDetails = createUserDetails(mentee);

        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(reservationMessageRepository.findByReservationId(eq(10L), any(Pageable.class)))
                .willReturn(Page.empty());

        reservationMessageService.getMessages(10L, userDetails, new ReservationMessageSearchRequestDto(null, null));

        then(reservationMessageRepository).should().findByReservationId(eq(10L), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("예약 당사자가 아니면 메시지 목록 조회 실패")
    void get_messages_fails_when_not_participant() {
        User mentor = createUser(1L, "mentor@test.com", "멘토");
        User mentee = createUser(2L, "mentee@test.com", "멘티");
        User outsider = createUser(3L, "outsider@test.com", "외부인");
        Reservation reservation = createReservation(10L, mentor, mentee, ReservationStatus.CANCELED);
        MentoringUserDetails userDetails = createUserDetails(outsider);

        given(reservationRepository.findById(10L)).willReturn(Optional.of(reservation));
        given(userRepository.findById(3L)).willReturn(Optional.of(outsider));

        assertThatThrownBy(() -> reservationMessageService.getMessages(
                10L,
                userDetails,
                new ReservationMessageSearchRequestDto(10, 0)
        ))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.AUTH_FORBIDDEN));
    }

    private User createUser(Long id, String email, String nickname) {
        return User.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .passwordHash("pw")
                .status(UserStatus.ACTIVE)
                .build();
    }

    private MentoringUserDetails createUserDetails(User user) {
        return new MentoringUserDetails(
                user.getId(),
                user.getEmail(),
                "pw",
                UserStatus.ACTIVE,
                List.of()
        );
    }

    private Reservation createReservation(Long id, User mentor, User mentee, ReservationStatus status) {
        Listing listing = Listing.builder()
                .id(100L)
                .mentor(mentor)
                .title("Spring 멘토링")
                .topic("백엔드")
                .price(30000)
                .placeType(PlaceType.ONLINE)
                .description("설명")
                .build();

        LocalDateTime startAt = LocalDateTime.of(2026, 4, 12, 10, 0);
        return Reservation.builder()
                .id(id)
                .listing(listing)
                .mentor(mentor)
                .mentee(mentee)
                .startAt(startAt)
                .endAt(startAt.plusHours(1))
                .status(status)
                .build();
    }
}
