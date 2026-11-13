package com.unimatch.web.discovery;

import com.unimatch.domain.ProfileLike;
import com.unimatch.domain.HiddenContact;
import com.unimatch.domain.MatchNotification;
import com.unimatch.domain.StudentProfile;
import com.unimatch.repository.ProfileLikeRepository;
import com.unimatch.repository.HiddenContactRepository;
import com.unimatch.repository.ChatMessageRepository;
import com.unimatch.repository.MatchNotificationRepository;
import com.unimatch.repository.StudentProfileRepository;
import com.unimatch.security.AccountService;
import com.unimatch.web.profiles.ProfileResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/likes")
public class LikeController {

    private final AccountService accountService;
    private final StudentProfileRepository profileRepository;
    private final ProfileLikeRepository likeRepository;
    private final HiddenContactRepository hiddenContactRepository;
    private final ChatMessageRepository messageRepository;
    private final MatchNotificationRepository notificationRepository;

    public LikeController(AccountService accountService, StudentProfileRepository profileRepository,
                          ProfileLikeRepository likeRepository, HiddenContactRepository hiddenContactRepository,
                          ChatMessageRepository messageRepository, MatchNotificationRepository notificationRepository) {
        this.accountService = accountService;
        this.profileRepository = profileRepository;
        this.likeRepository = likeRepository;
        this.hiddenContactRepository = hiddenContactRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
    }

    /** Registra un interes por otro perfil y genera avisos si se convierte en match. */
    @PostMapping("/{profileId}")
    @ResponseStatus(HttpStatus.CREATED)
    public LikeResponse like(@PathVariable Long profileId, Authentication authentication) {
        StudentProfile source = accountService.currentProfile(authentication.getName());
        StudentProfile target = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (source.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes indicar interes por tu perfil");
        }
        if (!likeRepository.existsBySourceProfileIdAndTargetProfileId(source.getId(), target.getId())) {
            likeRepository.save(new ProfileLike(source, target));
        }
        boolean mutual = likeRepository.existsBySourceProfileIdAndTargetProfileId(target.getId(), source.getId());
        if (mutual) {
            notifyMatch(source, target);
            notifyMatch(target, source);
        }
        return new LikeResponse(mutual);
    }

    /** Crea un aviso para el propietario solo si todavia no se le notifico ese match. */
    private void notifyMatch(StudentProfile owner, StudentProfile match) {
        if (!notificationRepository.existsByOwnerIdAndMatchId(owner.getId(), match.getId())) {
            notificationRepository.save(new MatchNotification(owner, match));
        }
    }

    /** Devuelve los perfiles con interes mutuo del usuario autenticado. */
    @GetMapping("/matches")
    public List<ProfileResponse> matches(Authentication authentication) {
        StudentProfile source = accountService.currentProfile(authentication.getName());
        return likeRepository.findBySourceProfileId(source.getId()).stream()
                .map(ProfileLike::getTargetProfile)
                .filter(target -> likeRepository.existsBySourceProfileIdAndTargetProfileId(target.getId(), source.getId()))
                .map(ProfileResponse::from)
                .collect(Collectors.toList());
    }

    /** Oculta un match, elimina sus mensajes y retira los intereses mutuos. */
    @org.springframework.web.bind.annotation.DeleteMapping("/matches/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideMatch(@PathVariable Long profileId, Authentication authentication) {
        StudentProfile source = accountService.currentProfile(authentication.getName());
        profileRepository.findById(profileId).ifPresent(target -> {
            hiddenContactRepository.save(new HiddenContact(source, target));
            messageRepository.deleteConversation(source.getId(), target.getId());
            likeRepository.deleteBySourceProfileIdAndTargetProfileId(source.getId(), target.getId());
            likeRepository.deleteBySourceProfileIdAndTargetProfileId(target.getId(), source.getId());
        });
    }
}
