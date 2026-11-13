package com.unimatch.domain;

public class ProfileRejection {
    private Long id;
    private StudentProfile sourceProfile;
    private StudentProfile targetProfile;

    protected ProfileRejection() {
    }

    public ProfileRejection(StudentProfile sourceProfile, StudentProfile targetProfile) {
        this.sourceProfile = sourceProfile;
        this.targetProfile = targetProfile;
    }

    public ProfileRejection(Long id, StudentProfile sourceProfile, StudentProfile targetProfile) {
        this(sourceProfile, targetProfile);
        this.id = id;
    }

    public StudentProfile getSourceProfile() { return sourceProfile; }
    public StudentProfile getTargetProfile() { return targetProfile; }
}
