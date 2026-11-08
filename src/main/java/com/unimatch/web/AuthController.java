package com.unimatch.web;

import com.unimatch.security.AccountService;
import com.unimatch.audit.AuditService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AuditService auditService;

    public AuthController(AccountService accountService, AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository, AuditService auditService) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.auditService = auditService;
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegistrationRequest request,
                                 HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        AuthResponse response = accountService.register(request);
        authenticateAndSave(request.getEmail(), request.getPassword(), servletRequest, servletResponse);
        auditService.record("REGISTER", request.getEmail(), "account-created");
        return response;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        Authentication authentication = authenticateAndSave(
                request.getEmail(), request.getPassword(), servletRequest, servletResponse
        );
        auditService.record("LOGIN", authentication.getName(), "session-created");
        return accountService.responseFor(authentication.getName());
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        return accountService.responseFor(authentication.getName());
    }

    @PutMapping("/me")
    public AuthResponse updateProfile(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
        auditService.record("PROFILE_UPDATE", authentication.getName(), "profile-updated");
        return accountService.updateProfile(authentication.getName(), request);
    }

    private Authentication authenticateAndSave(String email, String password,
                                               HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
        return authentication;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        auditService.record("LOGOUT", "session", "session-closed");
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void invalidCredentials() {
    }
}
