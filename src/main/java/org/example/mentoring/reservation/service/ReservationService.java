package org.example.mentoring.reservation.service;

import jakarta.transaction.Transactional;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class    ReservationService {
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void createReservation(Application application) {
        Slot slot = application.getSlot();
        Listing listing = application.getListing();

        Reservation reservation = Reservation.builder()
                .mentee(application.getMentee())
                .mentor(listing.getMentor())
                .application(application)
                .slot(slot)
                .listing(listing)
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .build();

        reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationSummaryResponseDto updateReservationStatus(Long reservationId, ReservationStatus reservationStatus, MentoringUserDetails userDetails) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        //권한 확인
        Long loginUserId = userDetails.getId();
        if (!reservation.getMentor().getId().equals(loginUserId)
                && !reservation.getMentee().getId().equals(loginUserId))
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);

        reservation.changeStatus(reservationStatus);

        reservationRepository.save(reservation);

        return ReservationSummaryResponseDto.from(reservation, userDetails.getId());
    }
}
