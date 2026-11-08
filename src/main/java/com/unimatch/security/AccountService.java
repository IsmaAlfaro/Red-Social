package com.unimatch.security;

import com.unimatch.domain.StudentProfile;
import com.unimatch.domain.Interest;
import com.unimatch.domain.UserAccount;
import com.unimatch.repository.InterestRepository;
import com.unimatch.repository.UserAccountRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {

    private final UserAccountRepository accountRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserAccountRepository accountRepository, InterestRepository interestRepository,
                          PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.interestRepository = interestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
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
        UserAccount account = new UserAccount(email, passwordEncoder.encode(request.getPassword()), profile);
        accountRepository.save(account);
        return new AuthResponse(ProfileResponse.from(account.getProfile()));
    }

    @Transactional(readOnly = true)
    public AuthResponse responseFor(String email) {
        UserAccount account = accountFor(email);
        return new AuthResponse(ProfileResponse.from(account.getProfile()));
    }

    @Transactional(readOnly = true)
    public StudentProfile currentProfile(String email) {
        return accountFor(email).getProfile();
    }

    @Transactional
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
        return new AuthResponse(ProfileResponse.from(profile));
    }

    private UserAccount accountFor(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
