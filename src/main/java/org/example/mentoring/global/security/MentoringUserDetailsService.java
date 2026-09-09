package org.example.mentoring.global.security;

import org.example.mentoring.user.domain.User;
import org.example.mentoring.global.exception.BusinessException;
import org.example.mentoring.global.exception.ErrorCode;
import org.example.mentoring.user.infrastructure.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MentoringUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public MentoringUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        List<GrantedAuthority> grantedAuthorities =
                user.getRoles().stream().map(MentoringGrantedAuthority::new).collect(Collectors.toList());

        return new MentoringUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus(),
                grantedAuthorities
        );
    }
}
