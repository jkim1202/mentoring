package org.example.mentoring.like.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.like.dto.LikeSearchRequestDto;
import org.example.mentoring.like.dto.LikeSummaryResponseDto;
import org.example.mentoring.like.entity.Like;
import org.example.mentoring.like.repository.LikeRepository;
import org.example.mentoring.listing.entity.Listing;
import org.example.mentoring.listing.entity.PlaceType;
import org.example.mentoring.listing.repository.ListingRepository;
import org.example.mentoring.security.MentoringUserDetails;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.entity.UserStatus;
import org.example.mentoring.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    @Test
    @DisplayName("내 찜 목록 조회 성공")
    void get_my_likes_success() {
        User mentor = createUser(1L, "mentor@test.com", "멘토");
        User loginUser = createUser(10L, "mentee@test.com", "멘티");
        Listing listing = createListing(100L, mentor);
        Like like = Like.create(loginUser, listing);
        setField(like, "id", 1000L);
        setField(like, "createdAt", LocalDateTime.of(2026, 4, 27, 13, 0));

        given(userRepository.findById(10L)).willReturn(Optional.of(loginUser));
        given(likeRepository.findByUserIdOrderByCreatedAtDesc(10L, PageRequest.of(0, 10)))
                .willReturn(new PageImpl<>(List.of(like), PageRequest.of(0, 10), 1));

        var result = likeService.getMyLikes(new LikeSearchRequestDto(0, 10), userDetailsOf(loginUser));

        assertThat(result.getContent()).hasSize(1);
        LikeSummaryResponseDto item = result.getContent().get(0);
        assertThat(item.listingId()).isEqualTo(100L);
        assertThat(item.title()).isEqualTo("Spring 멘토링");
        assertThat(item.liked()).isTrue();
    }

    @Test
    @DisplayName("본인 게시글은 찜할 수 없다")
    void toggle_like_fail_when_own_listing() {
        User loginUser = createUser(10L, "mentor@test.com", "멘토");
        Listing listing = createListing(100L, loginUser);

        given(userRepository.findById(10L)).willReturn(Optional.of(loginUser));
        given(listingRepository.findById(100L)).willReturn(Optional.of(listing));

        assertThatThrownBy(() -> likeService.toggleLike(100L, userDetailsOf(loginUser)))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.LIKE_SELF_NOT_ALLOWED));
    }

    @Test
    @DisplayName("찜이 없으면 생성한다")
    void toggle_like_create_success() {
        User mentor = createUser(1L, "mentor@test.com", "멘토");
        User loginUser = createUser(10L, "mentee@test.com", "멘티");
        Listing listing = createListing(100L, mentor);

        given(userRepository.findById(10L)).willReturn(Optional.of(loginUser));
        given(listingRepository.findById(100L)).willReturn(Optional.of(listing));
        given(likeRepository.findByUserIdAndListingId(10L, 100L)).willReturn(Optional.empty());
        given(likeRepository.save(any(Like.class))).willAnswer(invocation -> invocation.getArgument(0));

        var result = likeService.toggleLike(100L, userDetailsOf(loginUser));

        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.listingId()).isEqualTo(100L);
        assertThat(result.liked()).isTrue();
    }

    private User createUser(Long id, String email, String nickname) {
        return User.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .passwordHash("pw")
                .status(UserStatus.ACTIVE)
                .build();
    }

    private Listing createListing(Long id, User mentor) {
        return Listing.builder()
                .id(id)
                .mentor(mentor)
                .title("Spring 멘토링")
                .topic("Spring")
                .price(50000)
                .placeType(PlaceType.ONLINE)
                .description("설명")
                .avgRating(new BigDecimal("4.80"))
                .reviewCount(12)
                .build();
    }

    private MentoringUserDetails userDetailsOf(User user) {
        return new MentoringUserDetails(
                user.getId(),
                user.getEmail(),
                "pw",
                UserStatus.ACTIVE,
                List.of()
        );
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
