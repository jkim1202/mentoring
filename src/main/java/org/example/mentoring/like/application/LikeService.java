package org.example.mentoring.like.application;

import org.example.mentoring.global.exception.BusinessException;
import org.example.mentoring.global.exception.ErrorCode;
import org.example.mentoring.like.presentation.dto.LikeDetailsDto;
import org.example.mentoring.like.presentation.dto.LikeSearchRequestDto;
import org.example.mentoring.like.presentation.dto.LikeSummaryResponseDto;
import org.example.mentoring.like.domain.Like;
import org.example.mentoring.like.infrastructure.repository.LikeRepository;
import org.example.mentoring.listing.domain.Listing;
import org.example.mentoring.listing.infrastructure.repository.ListingRepository;
import org.example.mentoring.global.security.MentoringUserDetails;
import org.example.mentoring.user.domain.User;
import org.example.mentoring.user.infrastructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository, UserRepository userRepository, ListingRepository listingRepository) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public LikeDetailsDto toggleLike(Long listingId, MentoringUserDetails userDetails){
        User loginUser = findUser(userDetails);
        Listing listing = findListing(listingId);

        validateNotOwnListing(loginUser, listing);
        Optional<Like> like = likeRepository.findByUserIdAndListingId(loginUser.getId(), listing.getId());

        if(like.isPresent()){
            likeRepository.delete(like.get());
            return LikeDetailsDto.from(like.get(), false);
        }
        else  {
            return LikeDetailsDto.from(likeRepository.save(Like.create(loginUser, listing)), true);
        }
    }

    @Transactional(readOnly = true)
    public Page<LikeSummaryResponseDto> getMyLikes(LikeSearchRequestDto requestDto, MentoringUserDetails userDetails) {
        User loginUser = findUser(userDetails);

        int page = requestDto.page() == null ? 0 : requestDto.page();
        int size = requestDto.size() == null ? 10 : requestDto.size();
        Pageable pageable = PageRequest.of(page, size);

        return likeRepository.findByUserIdOrderByCreatedAtDesc(loginUser.getId(), pageable)
                .map(LikeSummaryResponseDto::from);
    }

    private User findUser(MentoringUserDetails userDetails){
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
    private Listing findListing(Long listingId){
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LISTING_NOT_FOUND));
    }
    private void validateNotOwnListing(User user, Listing listing){
        if(listing.getMentor().getId().equals(user.getId())){
            throw new BusinessException(ErrorCode.LIKE_SELF_NOT_ALLOWED);
        }
    }

}
