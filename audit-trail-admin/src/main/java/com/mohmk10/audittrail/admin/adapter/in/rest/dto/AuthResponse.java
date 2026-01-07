package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

public record AuthResponse(
        UserResponse user,
        String token,
        long expiresIn
) {
    public static AuthResponse of(UserResponse user, String token, long expiresIn) {
        return new AuthResponse(user, token, expiresIn);
    }
}
