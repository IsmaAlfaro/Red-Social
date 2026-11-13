package com.unimatch.web.discovery;

import com.unimatch.domain.Interest;
import com.unimatch.domain.StudentProfile;
import com.unimatch.web.profiles.ProfileResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MatchResponse {

    private final ProfileResponse profile;
    private final int compatibility;
    private final List<String> sharedInterests;

    /** Calcula intereses compartidos y compatibilidad porcentual entre un perfil y un candidato. */
    private MatchResponse(StudentProfile source, StudentProfile candidate) {
        Set<Long> sourceInterestIds = source.getInterests().stream()
                .map(Interest::getId)
                .collect(Collectors.toSet());
        Set<Long> allInterestIds = new HashSet<Long>(sourceInterestIds);
        candidate.getInterests().forEach(interest -> allInterestIds.add(interest.getId()));

        this.sharedInterests = candidate.getInterests().stream()
                .filter(interest -> sourceInterestIds.contains(interest.getId()))
                .map(Interest::getName)
                .collect(Collectors.toList());
        this.compatibility = allInterestIds.isEmpty()
                ? 0
                : (int) Math.round((sharedInterests.size() * 100.0) / allInterestIds.size());
        this.profile = ProfileResponse.from(candidate);
    }

    /** Crea la respuesta de compatibilidad para el par de perfiles indicado. */
    public static MatchResponse from(StudentProfile source, StudentProfile candidate) {
        return new MatchResponse(source, candidate);
    }

    public ProfileResponse getProfile() {
        return profile;
    }

    public int getCompatibility() {
        return compatibility;
    }

    public List<String> getSharedInterests() {
        return sharedInterests;
    }
}
