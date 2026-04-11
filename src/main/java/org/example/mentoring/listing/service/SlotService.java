package org.example.mentoring.listing.service;

import org.example.mentoring.application.repository.ApplicationRepository;
import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.listing.entity.Slot;
import org.example.mentoring.listing.entity.SlotStatus;
import org.example.mentoring.listing.repository.SlotRepository;
import org.springframework.stereotype.Service;

@Service
public class SlotService {
    private final SlotRepository slotRepository;

    public SlotService(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public Slot findSlotById(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));
    }

    public Slot findSlotByIdForUpdate(Long slotId) {
        return slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLOT_NOT_FOUND));
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
}
