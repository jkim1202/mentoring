package org.example.mentoring.slot.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.repository.ListingRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.slot.dto.SlotCreateRequestDto;
import org.example.mentoring.slot.dto.SlotResponseDto;
import org.example.mentoring.slot.dto.SlotUpdateRequestDto;
import org.example.mentoring.slot.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.slot.repository.SlotRepository;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SlotService {
    private final SlotRepository slotRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public SlotService(SlotRepository slotRepository, ListingRepository listingRepository, UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    public Slot findSlotById(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));
    }

    public Slot findSlotByIdForUpdate(Long slotId) {
        return slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));
    }

    @Transactional
    public SlotResponseDto createSlot(Long listingId, SlotCreateRequestDto request, MentoringUserDetails loginUser) {
        Listing listing =  listingRepository.findById(listingId).orElseThrow(
                () -> new BusinessException(ErrorCode.LISTING_NOT_FOUND)
        );
        User user = userRepository.findById(loginUser.getId()).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );

        validateUserOwnsListing(listing, user);
        validateSlotCreation(request);
        Slot slot = Slot.builder()
                .listing(listing)
                .startAt(request.startAt())
                .endAt(request.endAt())
                .status(SlotStatus.OPEN)
                .build();

        Slot savedSlot = slotRepository.save(slot);

        return SlotResponseDto.from(savedSlot);
    }

    @Transactional(readOnly = true)
    public Page<SlotResponseDto> getSlots(Long listingId, LocalDateTime from, LocalDateTime to, Integer page, Integer size) {
        if (!listingRepository.existsById(listingId)) {
            throw new BusinessException(ErrorCode.LISTING_NOT_FOUND);
        }

        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 20 : size;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "startAt"));

        return slotRepository.searchByListingId(listingId, from, to, pageable)
                .map(SlotResponseDto::from);
    }

    @Transactional
    public SlotResponseDto updateSlot(Long slotId, SlotUpdateRequestDto requestDto, MentoringUserDetails loginUser) {
        Slot slot = findSlotById(slotId);
        User user = userRepository.findById(loginUser.getId()).orElseThrow(
                () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );

        validateUserOwnsListing(slot.getListing(), user);
        validateSlotEditable(slot);
        validateSlotUpdate(requestDto);

        slot.changeSchedule(requestDto.startAt(), requestDto.endAt());

        return SlotResponseDto.from(slot);
    }

    public void validateSlotBelongsToListing(Slot slot, Long listingId) {
        if (!slot.getListing().getId().equals(listingId)) {
            throw new BusinessException(ErrorCode.SLOT_NOT_BELONG_TO_LISTING);
        }
    }

    public void validateSlotAvailableForApplication(Slot slot) {
        expireIfStarted(slot);

        if (slot.getStatus() == SlotStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.SLOT_EXPIRED);
        }

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED);
        }
    }

    public void validateSlotAcceptable(Slot slot) {
        expireIfStarted(slot);

        if (slot.getStatus() == SlotStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.APPLICATION_ACCEPT_EXPIRED);
        }
    }

    public void bookSlot(Slot slot) {
        expireIfStarted(slot);

        if (slot.getStatus() == SlotStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.SLOT_EXPIRED);
        }

        slot.book();
    }

    public void releaseSlot(Slot slot) {
        if (slot.isStarted()) {
            expireIfStarted(slot);
            return;
        }
        slot.reopen();
    }

    public void expireIfStarted(Slot slot) {
        if (slot.getStatus() != SlotStatus.EXPIRED && slot.isStarted()) {
            slot.expire();
        }
    }

    private void validateUserOwnsListing(Listing listing, User user) {
        if(!listing.getMentor().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
    }
    private void validateSlotCreation(SlotCreateRequestDto requestDto) {
        if(!requestDto.startAt().isBefore(requestDto.endAt())) {
            throw new BusinessException(ErrorCode.SLOT_INVALID_TIME_RANGE);
        }

        if(requestDto.startAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.SLOT_START_AT_IN_PAST);
        }
    }

    private void validateSlotUpdate(SlotUpdateRequestDto requestDto) {
        if (!requestDto.startAt().isBefore(requestDto.endAt())) {
            throw new BusinessException(ErrorCode.SLOT_INVALID_TIME_RANGE);
        }

        if (requestDto.startAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.SLOT_START_AT_IN_PAST);
        }
    }

    private void validateSlotEditable(Slot slot) {
        expireIfStarted(slot);

        if (slot.getStatus() == SlotStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.SLOT_EXPIRED);
        }

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED);
        }
    }
}
