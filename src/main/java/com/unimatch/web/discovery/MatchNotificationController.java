package com.unimatch.web.discovery;

import com.unimatch.domain.MatchNotification;
import com.unimatch.repository.MatchNotificationRepository;
import com.unimatch.security.AccountService;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/match-notifications")
public class MatchNotificationController {
    private final AccountService accountService;
    private final MatchNotificationRepository notificationRepository;
    public MatchNotificationController(AccountService accountService, MatchNotificationRepository notificationRepository) { this.accountService = accountService; this.notificationRepository = notificationRepository; }
    /** Entrega el siguiente aviso de match pendiente y lo marca como visto. */
    @GetMapping
    public Map<String, String> next(Authentication authentication) {
        return notificationRepository.findFirstByOwnerIdAndSeenFalseOrderByIdAsc(accountService.currentProfile(authentication.getName()).getId())
                .map(notification -> { notification.markSeen(); notificationRepository.save(notification); return Collections.singletonMap("name", notification.getMatch().getName()); })
                .orElse(Collections.emptyMap());
    }
}
