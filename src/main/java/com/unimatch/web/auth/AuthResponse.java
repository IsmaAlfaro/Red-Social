package com.unimatch.web.auth;

import com.unimatch.web.profiles.ProfileResponse;

public class AuthResponse {

    private final ProfileResponse profile;

    public AuthResponse(ProfileResponse profile) {
        this.profile = profile;
    }

    public ProfileResponse getProfile() {
        return profile;
    }
}
