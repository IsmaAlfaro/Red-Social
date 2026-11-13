package com.unimatch.web.profiles;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class ProfileUpdateRequest {

    @NotBlank private String name;
    @NotBlank private String degree;
    @Size(max = 280) private String bio;
    @NotEmpty private List<Long> interestIds;
    @Size(max = 2800000) private String profileImage;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public List<Long> getInterestIds() { return interestIds; }
    public void setInterestIds(List<Long> interestIds) { this.interestIds = interestIds; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
