package com.unimatch.web.discovery;

import com.unimatch.domain.ProfileRejection;
import com.unimatch.domain.StudentProfile;
import com.unimatch.repository.ProfileRejectionRepository;
import com.unimatch.repository.StudentProfileRepository;
import com.unimatch.security.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rejections")
public class RejectionController {

    private final AccountService accountService;
    private final StudentProfileRepository profileRepository;
    private final ProfileRejectionRepository rejectionRepository;

    public RejectionController(AccountService accountService, StudentProfileRepository profileRepository,
                               ProfileRejectionRepository rejectionRepository) {
        this.accountService = accountService;
        this.profileRepository = profileRepository;
        this.rejectionRepository = rejectionRepository;
    }

    /** Registra el descarte de otro perfil, ignorando perfiles inexistentes o el propio. */
    @PostMapping("/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable Long profileId, Authentication authentication) {
        StudentProfile source = accountService.currentProfile(authentication.getName());
        profileRepository.findById(profileId).ifPresent(target -> {
            if (!source.getId().equals(target.getId())
                    && !rejectionRepository.existsBySourceProfileIdAndTargetProfileId(source.getId(), target.getId())) {
                rejectionRepository.save(new ProfileRejection(source, target));
            }
        });
    }
}
