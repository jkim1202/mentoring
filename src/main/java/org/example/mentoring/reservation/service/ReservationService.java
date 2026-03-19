package org.example.mentoring.reservation.service;

import org.example.mentoring.application.entity.Application;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.repository.SlotRepository;
import org.example.mentoring.reservation.dto.ReservationSearchRequestDto;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.reservation.type.ReservationFilter;
import org.example.mentoring.reservation.type.ReservationSort;
import org.example.mentoring.reservation.type.ReservationView;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final SlotRepository slotRepository;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, SlotRepository slotRepository) {
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createReservation(Application application) {
        //
        Slot slot = slotRepository.findByIdForUpdate(application.getSlot().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

        Listing listing = application.getListing();

        // 현재 active인 slot이 이미 존재하는지 확인
        if (reservationRepository.existsBySlotIdAndStatusIn(slot.getId(),
                List.of(ReservationStatus.PENDING_PAYMENT, ReservationStatus.CONFIRMED)
        )) {
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_EXISTS);
        }

        // slot 상태 변경 -> BOOKED
        slot.book();

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

        if (reservationStatus == ReservationStatus.CANCELED)
            reservation.getSlot().reopen();
        reservation.changeStatus(reservationStatus);

        reservationRepository.save(reservation);

        return ReservationSummaryResponseDto.from(reservation, userDetails.getId(), reservation.getSlot().getStatus());
    }

    public Page<ReservationSummaryResponseDto> getReservations(ReservationSearchRequestDto req, MentoringUserDetails userDetails) {
        Long  loginUserId = userDetails.getId();

        int page =  req.page() == null ? 0 : req.page();
        int size = req.size() == null ? 10 : req.size();
        ReservationView view = req.view() == null ? ReservationView.MENTEE : req.view();
        ReservationSort sort = req.sort() == null ? ReservationSort.LATEST : req.sort();
        ReservationFilter filter = req.filter() == null ? ReservationFilter.UPCOMING : req.filter();

        Pageable pageable = PageRequest.of(page, size, toSort(sort));

        return switch (view){
            case MENTOR -> reservationRepository.searchByMentorId(filter, pageable, loginUserId)
                    .map(res -> ReservationSummaryResponseDto.from(res, userDetails.getId(), res.getSlot().getStatus()));
            case MENTEE -> reservationRepository.searchByMenteeId(filter, pageable, loginUserId)
                    .map(res -> ReservationSummaryResponseDto.from(res, userDetails.getId(), res.getSlot().getStatus()));
        };
    }
    private Sort toSort(ReservationSort sort) {
        return switch (sort) {
            case SOONEST -> Sort.by(
                    Sort.Order.asc("startAt")
            );
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

}
