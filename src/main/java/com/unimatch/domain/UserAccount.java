package com.unimatch.domain;

public class UserAccount {
    private Long id;
    private String email;
    private String passwordHash;
    private StudentProfile profile;

    protected UserAccount() {
    }

    public UserAccount(String email, String passwordHash, StudentProfile profile) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.profile = profile;
    }

    public UserAccount(Long id, String email, String passwordHash, StudentProfile profile) {
        this(email, passwordHash, profile);
        this.id = id;
    }

    public Long getId() { return id; }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public StudentProfile getProfile() {
        return profile;
    }
}
