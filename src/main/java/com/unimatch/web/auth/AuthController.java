package com.unimatch.web.auth;

import com.unimatch.security.AccountService;
import com.unimatch.web.profiles.ProfileUpdateRequest;
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

    /** Devuelve el token CSRF asociado a la solicitud actual. */
    @GetMapping("/csrf")
    public CsrfToken csrf(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }

    /** Registra una cuenta, inicia su sesion y devuelve el perfil creado. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegistrationRequest request,
                                 HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        AuthResponse response = accountService.register(request);
        authenticateAndSave(request.getEmail(), request.getPassword(), servletRequest, servletResponse);
        auditService.record("REGISTER", request.getEmail(), "account-created");
        return response;
    }

    /** Autentica las credenciales, persiste el contexto de sesion y devuelve el perfil. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        Authentication authentication = authenticateAndSave(
                request.getEmail(), request.getPassword(), servletRequest, servletResponse
        );
        auditService.record("LOGIN", authentication.getName(), "session-created");
        return accountService.responseFor(authentication.getName());
    }

    /** Devuelve el perfil de la cuenta autenticada. */
    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        return accountService.responseFor(authentication.getName());
    }

    /** Actualiza el perfil autenticado y deja constancia de la operacion en auditoria. */
    @PutMapping("/me")
    public AuthResponse updateProfile(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
        auditService.record("PROFILE_UPDATE", authentication.getName(), "profile-updated");
        return accountService.updateProfile(authentication.getName(), request);
    }

    /** Autentica las credenciales y guarda el contexto resultante en la sesion HTTP. */
    private Authentication authenticateAndSave(String email, String password,
                                               HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
        return authentication;
    }

    /** Cierra la sesion actual, limpia su contexto de seguridad y la registra en auditoria. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        auditService.record("LOGOUT", "session", "session-closed");
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
    }

    /** Convierte los fallos de autenticacion en una respuesta HTTP no autorizada. */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void invalidCredentials() {
    }
}
