package com.unimatch.web.messaging;

import com.unimatch.domain.ChatMessage;
import com.unimatch.domain.StudentProfile;
import com.unimatch.repository.ChatMessageRepository;
import com.unimatch.repository.ProfileLikeRepository;
import com.unimatch.repository.StudentProfileRepository;
import com.unimatch.security.AccountService;
import com.unimatch.audit.AuditService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final AccountService accountService;
    private final StudentProfileRepository profileRepository;
    private final ProfileLikeRepository likeRepository;
    private final ChatMessageRepository messageRepository;
    private final AuditService auditService;

    public MessageController(AccountService accountService, StudentProfileRepository profileRepository,
                              ProfileLikeRepository likeRepository, ChatMessageRepository messageRepository, AuditService auditService) {
        this.accountService = accountService;
        this.profileRepository = profileRepository;
        this.likeRepository = likeRepository;
        this.messageRepository = messageRepository;
        this.auditService = auditService;
    }

    /** Devuelve la conversacion con un match tras comprobar que la relacion sigue siendo mutua. */
    @GetMapping("/{profileId}")
    public List<MessageResponse> conversation(@PathVariable Long profileId, Authentication authentication) {
        StudentProfile source = matchFor(profileId, authentication);
        return messageRepository.findConversation(source.getId(), profileId).stream()
                .map(MessageResponse::new)
                .collect(Collectors.toList());
    }

    /** Envia un mensaje a un match y devuelve su representacion persistida. */
    @PostMapping("/{profileId}")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@PathVariable Long profileId, Authentication authentication,
                                @Valid @RequestBody MessageRequest request) {
        StudentProfile source = matchFor(profileId, authentication);
        StudentProfile target = profileRepository.getById(profileId);
        ChatMessage message = messageRepository.save(new ChatMessage(source, target, request.getContent().trim()));
        auditService.record("MESSAGE_SENT", authentication.getName(), "recipient-profile=" + profileId);
        return new MessageResponse(message);
    }

    /** Verifica que el destinatario existe y es un match mutuo del usuario autenticado. */
    private StudentProfile matchFor(Long targetId, Authentication authentication) {
        StudentProfile source = accountService.currentProfile(authentication.getName());
        if (!profileRepository.existsById(targetId)
                || !likeRepository.existsBySourceProfileIdAndTargetProfileId(source.getId(), targetId)
                || !likeRepository.existsBySourceProfileIdAndTargetProfileId(targetId, source.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes escribir a tus matches");
        }
        return source;
    }
}
