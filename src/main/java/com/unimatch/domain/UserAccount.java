package com.unimatch.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @OneToOne(cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private StudentProfile profile;

    protected UserAccount() {
    }

    public UserAccount(String email, String passwordHash, StudentProfile profile) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.profile = profile;
    }

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
