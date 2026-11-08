package com.unimatch.web;

import com.unimatch.domain.Interest;
import com.unimatch.domain.InterestType;
import com.unimatch.domain.StudentProfile;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileResponse {

    private final Long id;
    private final String name;
    private final String degree;
    private final String bio;
    private final String profileImage;
    private final List<InterestResponse> interests;

    private ProfileResponse(StudentProfile profile) {
        this.id = profile.getId();
        this.name = profile.getName();
        this.degree = profile.getDegree();
        this.bio = profile.getBio();
        this.profileImage = profile.getProfileImage();
        this.interests = profile.getInterests().stream()
                .map(InterestResponse::new)
                .collect(Collectors.toList());
    }

    public static ProfileResponse from(StudentProfile profile) {
        return new ProfileResponse(profile);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDegree() {
        return degree;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public List<InterestResponse> getInterests() {
        return interests;
    }

    public static class InterestResponse {

        private final Long id;
        private final String name;
        private final InterestType type;

        private InterestResponse(Interest interest) {
            this.id = interest.getId();
            this.name = interest.getName();
            this.type = interest.getType();
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public InterestType getType() {
            return type;
        }
    }
}
