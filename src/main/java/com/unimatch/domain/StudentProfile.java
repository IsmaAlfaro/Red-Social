package com.unimatch.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

@Entity
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String universityEmail;

    @Column(nullable = false)
    private String degree;

    private String bio;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "profile_interests",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private Set<Interest> interests = new LinkedHashSet<Interest>();

    protected StudentProfile() {
    }

    public StudentProfile(String name, String universityEmail, String degree, String bio) {
        this.name = name;
        this.universityEmail = universityEmail;
        this.degree = degree;
        this.bio = bio;
    }

    public void addInterest(Interest interest) {
        interests.add(interest);
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

    public Set<Interest> getInterests() {
        return interests;
    }
}
