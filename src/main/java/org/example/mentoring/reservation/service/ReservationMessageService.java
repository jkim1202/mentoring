package org.example.mentoring.reservation.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.reservation.dto.ReservationMessageCreateRequestDto;
import org.example.mentoring.reservation.dto.ReservationMessageCreateResponseDto;
import org.example.mentoring.reservation.dto.ReservationMessageResponseDto;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationMessage;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationMessageRepository;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationMessageService {
    private final ReservationMessageRepository reservationMessageRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    @Autowired
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
    public List<ReservationMessageResponseDto> getMessages(Long reservationId, MentoringUserDetails userDetails) {
        Reservation reservation = findReservation(reservationId);
        User user = findUser(userDetails.getId());

        validateParticipantAuthority(reservation, user);

        return reservationMessageRepository.findByReservationIdOrderByCreatedAtAsc(reservationId).stream()
                .map(ReservationMessageResponseDto::from)
                .toList();
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
