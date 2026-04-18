package org.example.mentoring.listing.service;

import org.example.mentoring.listing.dto.MyListingSearchRequestDto;
import org.example.mentoring.listing.dto.MyListingSummaryResponseDto;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.ListingStatus;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.repository.ListingRepository;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @InjectMocks
    private ListingService listingService;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("내 등록글 조회 성공")
    void get_my_listings_success() {
        User mentor = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .build();

        Listing listing = Listing.builder()
                .id(10L)
                .mentor(mentor)
                .title("Spring 멘토링")
                .topic("Spring")
                .price(50000)
                .placeType(PlaceType.ONLINE)
                .description("설명")
                .status(ListingStatus.ACTIVE)
                .avgRating(new BigDecimal("4.80"))
                .reviewCount(12)
                .createdAt(LocalDateTime.of(2026, 4, 18, 10, 0))
                .build();

        MyListingSearchRequestDto requestDto = new MyListingSearchRequestDto(0, 10, "LATEST", null);

        given(listingRepository.findByMentorId(1L, PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))))
                .willReturn(new PageImpl<>(List.of(listing), PageRequest.of(0, 10), 1));

        var result = listingService.getMyListings(1L, requestDto);

        assertThat(result.getContent()).hasSize(1);
        MyListingSummaryResponseDto item = result.getContent().get(0);
        assertThat(item.id()).isEqualTo(10L);
        assertThat(item.status()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(item.reviewCount()).isEqualTo(12);
    }

    @Test
    @DisplayName("내 등록글 상태 필터 조회 성공")
    void get_my_listings_with_status_filter_success() {
        MyListingSearchRequestDto requestDto = new MyListingSearchRequestDto(0, 10, "LATEST", ListingStatus.INACTIVE);

        given(listingRepository.findByMentorIdAndStatus(any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        var result = listingService.getMyListings(1L, requestDto);

        assertThat(result.getContent()).isEmpty();
    }
}
