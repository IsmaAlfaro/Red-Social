package com.unimatch.security;

import com.unimatch.domain.StudentProfile;
import com.unimatch.domain.Interest;
import com.unimatch.domain.UserAccount;
import com.unimatch.repository.InterestRepository;
import com.unimatch.repository.UserAccountRepository;
import com.unimatch.repository.StudentProfileRepository;
import com.unimatch.web.auth.AuthResponse;
import com.unimatch.web.profiles.ProfileResponse;
import com.unimatch.web.auth.RegistrationRequest;
import com.unimatch.web.profiles.ProfileUpdateRequest;
import java.util.Locale;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {

    private final UserAccountRepository accountRepository;
    private final InterestRepository interestRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountRepository accountRepository, InterestRepository interestRepository, StudentProfileRepository profileRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.interestRepository = interestRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Registra una cuenta con perfil e intereses validados, rechazando correos duplicados. */
    public AuthResponse register(RegistrationRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (accountRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya tiene una cuenta");
        }

        StudentProfile profile = new StudentProfile(
                request.getName().trim(), email, request.getDegree().trim(), request.getBio()
        );
        profile.setProfileImage(request.getProfileImage());
        Set<Long> interestIds = new HashSet<Long>(request.getInterestIds());
        List<Interest> interests = interestRepository.findAllById(interestIds);
        if (interests.size() != interestIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay intereses no validos");
        }
        interests.forEach(profile::addInterest);
        UserAccount account = accountRepository.save(new UserAccount(email, passwordEncoder.encode(request.getPassword()), profile));
        return new AuthResponse(ProfileResponse.from(account.getProfile()));
    }

    /** Construye la respuesta de autenticacion para la cuenta identificada por el correo. */
    public AuthResponse responseFor(String email) {
        UserAccount account = accountFor(email);
        return new AuthResponse(ProfileResponse.from(account.getProfile()));
    }

    /** Obtiene el perfil asociado a la cuenta autenticada por su correo. */
    public StudentProfile currentProfile(String email) {
        return accountFor(email).getProfile();
    }

    /** Actualiza los datos editables e intereses del perfil tras validar sus identificadores. */
    public AuthResponse updateProfile(String email, ProfileUpdateRequest request) {
        StudentProfile profile = accountFor(email).getProfile();
        Set<Long> ids = new HashSet<Long>(request.getInterestIds());
        List<Interest> interests = interestRepository.findAllById(ids);
        if (interests.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay intereses no validos");
        }
        profile.update(request.getName().trim(), request.getDegree().trim(), request.getBio(), new HashSet<Interest>(interests));
        if (request.getProfileImage() != null) {
            profile.setProfileImage(request.getProfileImage());
        }
        profileRepository.save(profile);
        return new AuthResponse(ProfileResponse.from(profile));
    }

    /** Busca una cuenta por correo o responde como no autenticado si no existe. */
    private UserAccount accountFor(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
