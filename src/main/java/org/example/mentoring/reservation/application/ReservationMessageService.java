package org.example.mentoring.reservation.application;

import org.example.mentoring.global.exception.BusinessException;
import org.example.mentoring.global.exception.ErrorCode;
import org.example.mentoring.reservation.presentation.dto.ReservationMessageCreateRequestDto;
import org.example.mentoring.reservation.presentation.dto.ReservationMessageCreateResponseDto;
import org.example.mentoring.reservation.presentation.dto.ReservationMessageSearchRequestDto;
import org.example.mentoring.reservation.presentation.dto.ReservationMessageSearchResponseDto;
import org.example.mentoring.reservation.domain.Reservation;
import org.example.mentoring.reservation.domain.ReservationMessage;
import org.example.mentoring.reservation.domain.ReservationStatus;
import org.example.mentoring.reservation.infrastructure.repository.ReservationMessageRepository;
import org.example.mentoring.reservation.infrastructure.repository.ReservationRepository;
import org.example.mentoring.global.security.MentoringUserDetails;
import org.example.mentoring.user.domain.User;
import org.example.mentoring.user.infrastructure.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationMessageService {
    private final ReservationMessageRepository reservationMessageRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public ReservationMessageService(ReservationMessageRepository reservationMessageRepository, ReservationRepository reservationRepository, UserRepository userRepository) {
        this.reservationMessageRepository = reservationMessageRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationMessageCreateResponseDto createMessage(Long reservationId, ReservationMessageCreateRequestDto req, MentoringUserDetails userDetails) {
        Reservation reservation = findReservation(reservationId);
        User sender = findUser(userDetails.getId());

        validateParticipantAuthority(reservation, sender);
        validateWritableStatus(reservation);

        ReservationMessage reservationMessage = ReservationMessage
                .builder()
                .reservation(reservation)
                .sender(sender)
                .content(req.content())
                .build();

        reservationMessageRepository.save(reservationMessage);

        return ReservationMessageCreateResponseDto.from(reservationMessage);
    }

    @Transactional(readOnly = true)
    public Page<ReservationMessageSearchResponseDto> getMessages(Long reservationId, MentoringUserDetails userDetails, ReservationMessageSearchRequestDto req) {
        Reservation reservation = findReservation(reservationId);
        User user = findUser(userDetails.getId());

        validateParticipantAuthority(reservation, user);

        int page = req.page() == null ? 0 : req.page();
        int size = req.size() == null ? 20 : req.size();

        Sort sort = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        return reservationMessageRepository.findByReservationId(reservationId, pageable)
                .map(ReservationMessageSearchResponseDto::from);
    }

    private void validateParticipantAuthority(Reservation reservation, User user) {
        if(!reservation.getMentee().getId().equals(user.getId()) && !reservation.getMentor().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
    }

    private void validateWritableStatus(Reservation reservation) {
        if(!reservation.getStatus().equals(ReservationStatus.PENDING_PAYMENT) && !reservation.getStatus().equals(ReservationStatus.CONFIRMED))
            throw new BusinessException(ErrorCode.RESERVATION_MESSAGE_NOT_WRITABLE);
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
