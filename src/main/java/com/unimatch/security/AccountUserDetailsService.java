package com.unimatch.security;

import com.unimatch.domain.UserAccount;
import com.unimatch.repository.UserAccountRepository;
import java.util.Collections;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final UserAccountRepository accountRepository;

    public AccountUserDetailsService(UserAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /** Busca la cuenta normalizando el correo y la adapta para la autenticacion de Spring Security. */
    @Override
    public UserDetails loadUserByUsername(String email) {
        UserAccount account = accountRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Cuenta no encontrada"));
        return new User(account.getEmail(), account.getPasswordHash(), Collections.emptyList());
    }
}
