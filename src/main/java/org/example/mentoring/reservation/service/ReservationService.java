package org.example.mentoring.reservation.service;

import lombok.extern.slf4j.Slf4j;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.service.SlotService;
import org.example.mentoring.reservation.dto.ReservationDetailResponseDto;
import org.example.mentoring.reservation.dto.ReservationSearchRequestDto;
import org.example.mentoring.reservation.dto.ReservationSummaryResponseDto;
import org.example.mentoring.reservation.entity.Reservation;
import org.example.mentoring.reservation.entity.ReservationStatus;
import org.example.mentoring.reservation.repository.ReservationRepository;
import org.example.mentoring.reservation.type.ReservationFilter;
import org.example.mentoring.reservation.type.ReservationSort;
import org.example.mentoring.reservation.type.ReservationView;
import org.example.mentoring.security.MentoringUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ReservationService {
    private static final long MENTEE_CANCEL_DEADLINE_HOURS = 24L;

    private final ReservationRepository reservationRepository;
    private final SlotService slotService;

    public ReservationService(ReservationRepository reservationRepository, SlotService slotService) {
        this.reservationRepository = reservationRepository;
        this.slotService = slotService;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createReservation(Application application) {
        Slot slot = slotService.findSlotByIdForUpdate(application.getSlot().getId());

        Listing listing = application.getListing();

        validateNoActiveReservation(slot.getId());
        slotService.bookSlot(slot);

        reservationRepository.save(Reservation.builder()
                .mentee(application.getMentee())
                .mentor(listing.getMentor())
                .application(application)
                .slot(slot)
                .listing(listing)
                .startAt(slot.getStartAt())
                .endAt(slot.getEndAt())
                .build());
    }

    @Transactional
    public ReservationSummaryResponseDto cancelReservation(Long reservationId, MentoringUserDetails userDetails) {
        Reservation reservation = findReservationById(reservationId);

        validateCancelAuthorityAndDeadline(reservation, userDetails);

        reservation.changeStatus(ReservationStatus.CANCELED);
        slotService.releaseSlot(reservation.getSlot());

        reservationRepository.save(reservation);

        return ReservationSummaryResponseDto.from(reservation, userDetails.getId(), reservation.getSlot().getStatus());
    }

    @Transactional
    public ReservationSummaryResponseDto markPaid(Long reservationId, MentoringUserDetails userDetails) {
        Reservation reservation = findReservationById(reservationId);

        if (!isMentee(reservation, userDetails))
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);

        validatePaymentExpiry(reservation);
        validateReservationNotStarted(reservation);

        reservation.markPaid();

        reservationRepository.save(reservation);

        return ReservationSummaryResponseDto.from(reservation, userDetails.getId(), reservation.getSlot().getStatus());
    }

    @Transactional
    public ReservationSummaryResponseDto confirmPaid(Long reservationId, MentoringUserDetails userDetails) {
        Reservation reservation = findReservationById(reservationId);

        if (!isMentor(reservation, userDetails))
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);

        validateReservationNotStarted(reservation);

        reservation.confirmPaid();

        reservation.changeStatus(ReservationStatus.CONFIRMED);

        reservationRepository.save(reservation);

        return ReservationSummaryResponseDto.from(reservation, userDetails.getId(), reservation.getSlot().getStatus());
    }

    @Transactional
    public ReservationSummaryResponseDto completeReservation(Long reservationId, MentoringUserDetails userDetails) {
        Reservation reservation = findReservationById(reservationId);

        if (!isMentor(reservation, userDetails))
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);

        reservation.changeStatus(ReservationStatus.COMPLETED);

        reservationRepository.save(reservation);

        return ReservationSummaryResponseDto.from(reservation, userDetails.getId(), reservation.getSlot().getStatus());
    }

    @Transactional(readOnly = true)
    public ReservationDetailResponseDto getReservation(Long reservationId, MentoringUserDetails userDetails) {
        Reservation reservation = reservationRepository.findDetailById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        validateParticipantAuthority(reservation, userDetails);

        return ReservationDetailResponseDto.from(reservation, userDetails.getId());
    }

    @Transactional(readOnly = true)
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

    /**
     * Return : 만료된 Reservation들이 점유했던 Slot들
     */
    public List<Slot> expirePendingReservationsAndReturnSlots(){
        final LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations =
                reservationRepository.findPendingReservationsToExpire(now, now.minusHours(1));

        log.info("Expired reservation count:{} ", reservations.size());

        for(Reservation reservation : reservations){
            reservation.changeStatus(ReservationStatus.CANCELED);
        }

        reservationRepository.saveAll(reservations);

        // 예약된 Slot Id List 반환
        return reservations
                .stream()
                .map(Reservation::getSlot)
                .toList();
    }

    private void validateCancelAuthorityAndDeadline(Reservation reservation, MentoringUserDetails userDetails) {
        validateParticipantAuthority(reservation, userDetails);

        if (reservation.getStatus() == ReservationStatus.PENDING_PAYMENT || isMentor(reservation, userDetails)) {
            return;
        }

        LocalDateTime menteeCancelDeadline = reservation.getStartAt().minusHours(MENTEE_CANCEL_DEADLINE_HOURS);
        if (!LocalDateTime.now().isBefore(menteeCancelDeadline)) {
            throw new BusinessException(ErrorCode.RESERVATION_CANCEL_DEADLINE_EXCEEDED);
        }
    }

    private void validateParticipantAuthority(Reservation reservation, MentoringUserDetails userDetails) {
        if (!isMentor(reservation, userDetails) && !isMentee(reservation, userDetails)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
    }

    private boolean isMentor(Reservation reservation, MentoringUserDetails userDetails) {
        return reservation.getMentor().getId().equals(userDetails.getId());
    }

    private boolean isMentee(Reservation reservation, MentoringUserDetails userDetails) {
        return reservation.getMentee().getId().equals(userDetails.getId());
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateNoActiveReservation(Long slotId) {
        if (reservationRepository.existsBySlotIdAndStatusIn(
                slotId,
                List.of(ReservationStatus.PENDING_PAYMENT, ReservationStatus.CONFIRMED)
        )) {
            throw new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED);
        }
    }
    private void validatePaymentExpiry(Reservation reservation){
        LocalDateTime createdAt = reservation.getCreatedAt();
        if(reservation.getStatus().equals(ReservationStatus.PENDING_PAYMENT) && !LocalDateTime.now().isBefore(createdAt.plusHours(1)))
            throw new BusinessException(ErrorCode.RESERVATION_PAYMENT_EXPIRED);
    }
    private void validateReservationNotStarted(Reservation reservation){
        LocalDateTime startAt = reservation.getStartAt();
        if(reservation.getStatus().equals(ReservationStatus.PENDING_PAYMENT) && !LocalDateTime.now().isBefore(startAt))
            throw new BusinessException(ErrorCode.RESERVATION_START_AT_EXPIRED);
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
