package org.example.mentoring.mentoringapplication.application;

import lombok.extern.slf4j.Slf4j;
import org.example.mentoring.mentoringapplication.presentation.dto.ApplicationCreateRequestDto;
import org.example.mentoring.mentoringapplication.presentation.dto.ApplicationCreateResponseDto;
import org.example.mentoring.mentoringapplication.presentation.dto.ApplicationDetailResponseDto;
import org.example.mentoring.mentoringapplication.presentation.dto.ApplicationSearchRequestDto;
import org.example.mentoring.mentoringapplication.presentation.dto.ApplicationStatusResponseDto;
import org.example.mentoring.mentoringapplication.presentation.dto.ApplicationSummaryResponseDto;
import org.example.mentoring.mentoringapplication.domain.MentoringApplication;
import org.example.mentoring.mentoringapplication.domain.ApplicationStatus;
import org.example.mentoring.mentoringapplication.infrastructure.repository.MentoringApplicationRepository;
import org.example.mentoring.mentoringapplication.domain.ApplicationFilter;
import org.example.mentoring.mentoringapplication.domain.ApplicationSort;
import org.example.mentoring.mentoringapplication.domain.ApplicationView;
import org.example.mentoring.global.exception.BusinessException;
import org.example.mentoring.global.exception.ErrorCode;
import org.example.mentoring.listing.domain.Listing;
import org.example.mentoring.slot.domain.Slot;
import org.example.mentoring.listing.infrastructure.repository.ListingRepository;
import org.example.mentoring.slot.application.SlotService;
import org.example.mentoring.reservation.application.ReservationService;
import org.example.mentoring.global.security.MentoringUserDetails;
import org.example.mentoring.user.domain.User;
import org.example.mentoring.user.infrastructure.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MentoringApplicationService {
    private final MentoringApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final SlotService slotService;
    private final ReservationService reservationService;

    public MentoringApplicationService(MentoringApplicationRepository applicationRepository,
                                       UserRepository userRepository,
                                       ListingRepository listingRepository,
                                       SlotService slotService,
                                       ReservationService reservationService
    ) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.slotService = slotService;
        this.reservationService = reservationService;
    }

    @Transactional
    public ApplicationCreateResponseDto createApplication(ApplicationCreateRequestDto req, MentoringUserDetails userDetails) {
        User mentee = findUserById(userDetails.getId());
        Listing listing = findListingById(req.listingId());
        Slot slot = slotService.findSlotByIdForUpdate(req.slotId());

        slotService.validateSlotBelongsToListing(slot, listing.getId());
        slotService.validateSlotAvailableForApplication(slot);
        validateNoAppliedApplication(mentee.getId(), req.slotId());

        MentoringApplication application = MentoringApplication.builder()
                .mentee(mentee)
                .listing(listing)
                .slot(slot)
                .message(req.message())
                .build();

        try {
            applicationRepository.saveAndFlush(application);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
        }

        return ApplicationCreateResponseDto.from(application);
    }

    @Transactional
    public ApplicationStatusResponseDto updateApplicationStatus(Long applicationId, MentoringUserDetails userDetails, ApplicationStatus applicationStatus) {
        MentoringApplication application = findApplicationByIdForUpdate(applicationId);

        validateUserExists(userDetails.getId());

        switch (applicationStatus) {
            case ACCEPTED -> acceptApplication(application, userDetails);
            case REJECTED -> rejectApplication(application, userDetails);
            case CANCELED -> cancelApplication(application, userDetails);
            default -> application.changeStatus(applicationStatus);
        }

        return new ApplicationStatusResponseDto(applicationId, applicationStatus);
    }

    @Transactional(readOnly = true)
    public ApplicationDetailResponseDto getApplication(Long applicationId, MentoringUserDetails userDetails) {
        MentoringApplication application = applicationRepository.findDetailById(applicationId)
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

        Page<MentoringApplication> applications;
        applications = switch (view){
            case MENTOR ->  applicationRepository.searchByMentorId(filter, pageable, loginUserId);
            case MENTEE ->  applicationRepository.searchByMenteeId(filter, pageable, loginUserId);
        };
        return applications.map(res -> ApplicationSummaryResponseDto.from(res, loginUserId));
    }

    public void cancelAppliedApplicationsByExpiredSlots(List<Slot> expiredSlots){
        List<Long> slotIds = expiredSlots
                .stream()
                .map(Slot::getId)
                .toList();

        List<MentoringApplication> applications = applicationRepository.findAppliedApplicationsBySlotIds(slotIds);
        for (MentoringApplication application : applications) {
            application.changeStatus(ApplicationStatus.CANCELED);
        }

        log.info("Canceled applications by slot expiry count={}", applications.size());
    }

    private Sort toSort(ApplicationSort sort) {
        return switch (sort) {
            case OLDEST -> Sort.by(
                    Sort.Order.asc("createdAt")
            );
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Listing findListingById(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));
    }

    private MentoringApplication findApplicationByIdForUpdate(Long applicationId) {
        return applicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));
    }

    private void validateNoAppliedApplication(Long menteeId, Long slotId) {
        if (applicationRepository.existsByMenteeIdAndSlotIdAndStatus(menteeId, slotId, ApplicationStatus.APPLIED))
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId))
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    private void validateMentorAuthority(MentoringApplication application, MentoringUserDetails userDetails) {
        if (!application.getListing().getMentor().getId().equals(userDetails.getId()))
            throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_MENTOR);
    }
    private void validateMenteeAuthority(MentoringApplication application, MentoringUserDetails userDetails) {
        if (!application.getMentee().getId().equals(userDetails.getId()))
            throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_MENTEE);
    }

    private void acceptApplication(MentoringApplication application, MentoringUserDetails userDetails) {
        validateMentorAuthority(application, userDetails);
        validateApplicationSlotNotStarted(application.getSlot());
        application.changeStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);
        reservationService.createReservation(application);
    }

    private void rejectApplication(MentoringApplication application, MentoringUserDetails userDetails) {
        validateMentorAuthority(application, userDetails);
        application.changeStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);
    }


    private void cancelApplication(MentoringApplication application, MentoringUserDetails userDetails) {
        validateMenteeAuthority(application, userDetails);
        application.changeStatus(ApplicationStatus.CANCELED);
        applicationRepository.save(application);
    }

    private void validateApplicationSlotNotStarted(Slot slot) {
        slotService.validateSlotAcceptable(slot);
    }
}
