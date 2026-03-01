package org.example.mentoring.auth.dto;

import org.example.mentoring.user.entity.UserStatus;

public record RegisterResponseDto(String email, UserStatus userStatus) {}
