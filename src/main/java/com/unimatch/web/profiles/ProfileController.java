package com.unimatch.web.profiles;

import com.unimatch.domain.StudentProfile;
import com.unimatch.repository.StudentProfileRepository;
import com.unimatch.repository.ProfileRejectionRepository;
import com.unimatch.repository.ProfileLikeRepository;
import com.unimatch.repository.HiddenContactRepository;
import com.unimatch.security.AccountService;
import com.unimatch.web.discovery.MatchResponse;
import org.springframework.security.core.Authentication;
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
    private final AccountService accountService;
    private final ProfileRejectionRepository rejectionRepository;
    private final ProfileLikeRepository likeRepository;
    private final HiddenContactRepository hiddenContactRepository;

    public ProfileController(StudentProfileRepository profileRepository, AccountService accountService,
                             ProfileRejectionRepository rejectionRepository, ProfileLikeRepository likeRepository,
                             HiddenContactRepository hiddenContactRepository) {
        this.profileRepository = profileRepository;
        this.accountService = accountService;
        this.rejectionRepository = rejectionRepository;
        this.likeRepository = likeRepository;
        this.hiddenContactRepository = hiddenContactRepository;
    }

    /** Lista todos los perfiles en su representacion publica. */
    @GetMapping
    public List<ProfileResponse> list() {
        return profileRepository.findAll().stream()
                .map(ProfileResponse::from)
                .collect(Collectors.toList());
    }

    /** Busca un perfil publico por identificador o responde con 404. */
    @GetMapping("/{id}")
    public ProfileResponse findById(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(ProfileResponse::from)
                .orElseThrow(ProfileNotFoundException::new);
    }

    /** Calcula las sugerencias de compatibilidad para el perfil indicado. */
    @GetMapping("/{id}/matches")
    public List<MatchResponse> matches(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(this::matchesFor)
                .orElseThrow(ProfileNotFoundException::new);
    }

    /** Calcula las sugerencias de compatibilidad para el usuario autenticado. */
    @GetMapping("/matches")
    public List<MatchResponse> myMatches(Authentication authentication) {
        return matchesFor(accountService.currentProfile(authentication.getName()));
    }

    /** Filtra perfiles descartados, ocultos o ya valorados y los ordena por afinidad. */
    private List<MatchResponse> matchesFor(StudentProfile profile) {
        return profileRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(profile.getId()))
                .filter(candidate -> !rejectionRepository.existsBySourceProfileIdAndTargetProfileId(
                        profile.getId(), candidate.getId()))
                .filter(candidate -> !hiddenContactRepository.existsByOwnerIdAndContactId(
                        profile.getId(), candidate.getId()))
                .filter(candidate -> !likeRepository.existsBySourceProfileIdAndTargetProfileId(
                        profile.getId(), candidate.getId()))
                .sorted(Comparator.comparing((StudentProfile candidate) -> likeRepository
                        .existsBySourceProfileIdAndTargetProfileId(candidate.getId(), profile.getId())).reversed())
                .map(candidate -> MatchResponse.from(profile, candidate))
                .sorted(Comparator.comparingInt(MatchResponse::getCompatibility).reversed())
                .collect(Collectors.toList());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class ProfileNotFoundException extends RuntimeException {
    }
}
