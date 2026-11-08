package com.unimatch.web;

import com.unimatch.repository.StudentProfileRepository;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final StudentProfileRepository profileRepository;

    public ProfileController(StudentProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public List<ProfileResponse> list() {
        return profileRepository.findAll().stream()
                .map(ProfileResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ProfileResponse findById(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(ProfileResponse::from)
                .orElseThrow(ProfileNotFoundException::new);
    }

    @GetMapping("/{id}/matches")
    public List<MatchResponse> matches(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(profile -> profileRepository.findAll().stream()
                        .filter(candidate -> !candidate.getId().equals(profile.getId()))
                        .map(candidate -> MatchResponse.from(profile, candidate))
                        .sorted(Comparator.comparingInt(MatchResponse::getCompatibility).reversed())
                        .collect(Collectors.toList()))
                .orElseThrow(ProfileNotFoundException::new);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class ProfileNotFoundException extends RuntimeException {
    }
}
