package org.example.mentoring.security;

import org.example.mentoring.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

public class MentoringUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    public MentoringUserDetails(
            Long id,
            String email,
            String passwordHash,
            UserStatus status,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.authorities = authorities;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Objects.equals(status, UserStatus.SUSPENDED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Objects.equals(status, UserStatus.ACTIVE);
    }
}