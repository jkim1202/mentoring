package org.example.mentoring.application.service;

import org.example.mentoring.application.dto.*;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.repository.ApplicationRepository;
import org.example.mentoring.application.type.ApplicationFilter;
import org.example.mentoring.application.type.ApplicationSort;
import org.example.mentoring.application.type.ApplicationView;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.listing.repository.ListingRepository;
import org.example.mentoring.listing.repository.SlotRepository;
import org.example.mentoring.reservation.service.ReservationService;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final SlotRepository slotRepository;
    private final ReservationService reservationService;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository,
                              UserRepository userRepository,
                              ListingRepository listingRepository,
                              SlotRepository slotRepository,
                              ReservationService reservationService
    ) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.slotRepository = slotRepository;
        this.reservationService = reservationService;
    }

    @Transactional
    public ApplicationCreateResponseDto createApplication(ApplicationCreateRequestDto req, MentoringUserDetails userDetails) {
        User mentee = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Listing listing = listingRepository.findById(req.listingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));

        Slot slot = slotRepository.findById(req.slotId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));

        // slot listing 매핑 확인
        if (!slot.getListing().getId().equals(listing.getId()))
            throw new BusinessException(ErrorCode.SLOT_NOT_BELONG_TO_LISTING);

        // slot 예약 상태 확인
        if (slot.getStatus() == SlotStatus.BOOKED) throw new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED);

        // 중복 신청 방지
        if (applicationRepository.existsByMenteeIdAndSlotIdAndStatus(mentee.getId(), req.slotId(), ApplicationStatus.APPLIED))
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);

        Application application = Application.builder().mentee(mentee).listing(listing).slot(slot).message(req.message()).build();

        applicationRepository.save(application);
        return ApplicationCreateResponseDto.from(application);
    }

    @Transactional
    public ApplicationStatusResponseDto updateApplicationStatus(Long applicationId, MentoringUserDetails userDetails, ApplicationStatus applicationStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        Long userId = userDetails.getId();

        if(!userRepository.existsById(userId))
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        if (!application.getListing().getMentor().getId().equals(userId))
            throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_MENTOR);

        application.changeStatus(applicationStatus);

        applicationRepository.save(application);

        // Application ACCEPTED -> Slot BOOKED, Reservation PENDING_PAYMENT
        if (applicationStatus == ApplicationStatus.ACCEPTED) {
            // Reservation 생성
            reservationService.createReservation(application);
        }

        return new ApplicationStatusResponseDto(applicationId, applicationStatus);
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponseDto getApplication(Long applicationId, MentoringUserDetails userDetails) {
        Application application = applicationRepository.findDetailById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        Long loginUserId = userDetails.getId();
        if(!application.getListing().getMentor().getId().equals(loginUserId)
                && !application.getMentee().getId().equals(loginUserId)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }

        return ApplicationDetailResponseDto.from(application, loginUserId);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationSummaryResponseDto> getApplications(ApplicationSearchRequestDto req, MentoringUserDetails userDetails) {
        Long  loginUserId = userDetails.getId();

        int page =  req.page() == null ? 0 : req.page();
        int size = req.size() == null ? 10 : req.size();
        ApplicationView view = req.view() == null ? ApplicationView.MENTEE : req.view();
        ApplicationSort sort = req.sort() == null ? ApplicationSort.LATEST : req.sort();
        ApplicationFilter filter = req.filter() == null ? ApplicationFilter.PENDING : req.filter();

        Pageable pageable = PageRequest.of(page, size, toSort(sort));

        Page<Application> applications;
        applications = switch (view){
            case MENTOR ->  applicationRepository.searchByMentorId(filter, pageable, loginUserId);
            case MENTEE ->  applicationRepository.searchByMenteeId(filter, pageable, loginUserId);
        };
        return applications.map(res -> ApplicationSummaryResponseDto.from(res, loginUserId));
    }

    private Sort toSort(ApplicationSort sort) {
        return switch (sort) {
            case OLDEST -> Sort.by(
                    Sort.Order.asc("createdAt")
            );
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
