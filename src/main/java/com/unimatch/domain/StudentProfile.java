package com.unimatch.domain;

import java.util.LinkedHashSet;
import java.util.Set;
public class StudentProfile {
    private Long id;
    private String name;
    private String universityEmail;
    private String degree;
    private String bio;
    private String profileImage;
    private Set<Interest> interests = new LinkedHashSet<Interest>();

    protected StudentProfile() {
    }

    public StudentProfile(String name, String universityEmail, String degree, String bio) {
        this.name = name;
        this.universityEmail = universityEmail;
        this.degree = degree;
        this.bio = bio;
    }

    public StudentProfile(Long id, String name, String universityEmail, String degree, String bio) {
        this(name, universityEmail, degree, bio);
        this.id = id;
    }

    public void addInterest(Interest interest) {
        interests.add(interest);
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void update(String name, String degree, String bio, Set<Interest> interests) {
        this.name = name;
        this.degree = degree;
        this.bio = bio;
        this.interests.clear();
        this.interests.addAll(interests);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUniversityEmail() {
        return universityEmail;
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

    public Set<Interest> getInterests() {
        return interests;
    }
}
