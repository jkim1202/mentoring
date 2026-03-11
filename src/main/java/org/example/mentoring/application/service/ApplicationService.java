package org.example.mentoring.application.service;

import jakarta.transaction.Transactional;
import org.example.mentoring.application.dto.ApplicationCreateRequestDto;
import org.example.mentoring.application.dto.ApplicationCreateResponseDto;
import org.example.mentoring.application.dto.ApplicationStatusResponseDto;
import org.example.mentoring.application.entity.Application;
import org.example.mentoring.application.entity.ApplicationStatus;
import org.example.mentoring.application.repository.ApplicationRepository;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.listing.repository.ListingRepository;
import org.example.mentoring.listing.repository.SlotRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final SlotRepository slotRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository,
                                         UserRepository userRepository,
                                         ListingRepository listingRepository,
                                         SlotRepository slotRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.slotRepository = slotRepository;
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
        if (applicationRepository.existsByMenteeIdAndSlotId(mentee.getId(), req.slotId()))
            throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS);

        Application application = Application.builder().mentee(mentee).listing(listing).slot(slot).message(req.message()).build();

        applicationRepository.save(application);
        return ApplicationCreateResponseDto.from(application);
    }

    @Transactional
    public ApplicationStatusResponseDto changeApplicationStatus(Long applicationId, MentoringUserDetails userDetails, ApplicationStatus applicationStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow( () -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow( () -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(!application.getListing().getMentor().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.APPLICATION_NOT_BELONG_TO_MENTOR);

        application.changeStatus(applicationStatus);

        applicationRepository.save(application);

        return new ApplicationStatusResponseDto(applicationId, applicationStatus);
    }
}
