package org.example.mentoring.mentor.profile.service;

import org.example.mentoring.exception.BusinessException;
import org.example.mentoring.exception.ErrorCode;
import org.example.mentoring.mentor.profile.dto.MentorProfileCreateRequestDto;
import org.example.mentoring.mentor.profile.dto.MentorProfileResponseDto;
import org.example.mentoring.mentor.profile.entity.MentorProfile;
import org.example.mentoring.mentor.profile.repository.MentorProfileRepository;
import org.example.mentoring.user.entity.User;
import org.example.mentoring.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MentorProfileService {
    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;

    public MentorProfileService(MentorProfileRepository mentorProfileRepository, UserRepository userRepository) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MentorProfileResponseDto createProfile(Long loginUserId, MentorProfileCreateRequestDto requestDto) {
        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (mentorProfileRepository.existsById(loginUserId)) {
            throw new BusinessException(ErrorCode.MENTOR_PROFILE_ALREADY_EXISTS);
        }

        MentorProfile mentorProfile = MentorProfile.builder()
                .user(user)
                .bio(requestDto.bio())
                .careerYears(requestDto.careerYears())
                .major(requestDto.major())
                .currentCompany(requestDto.currentCompany())
                .specsJson(requestDto.specsJson())
                .baseLocation(requestDto.baseLocation())
                .build();

        return MentorProfileResponseDto.from(mentorProfileRepository.save(mentorProfile));
    }

    @Transactional(readOnly = true)
    public MentorProfileResponseDto getProfile(Long mentorUserId) {
        MentorProfile mentorProfile = mentorProfileRepository.findDetailByUserId(mentorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

        return MentorProfileResponseDto.from(mentorProfile);
    }
}
