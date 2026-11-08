package com.unimatch.web;

public class AuthResponse {

    private final ProfileResponse profile;

    public AuthResponse(ProfileResponse profile) {
        this.profile = profile;
    }

    public ProfileResponse getProfile() {
        return profile;
    }
}
