package com.pulsepass.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly=true)
public class CustomUserDetailsService implements UserDetailsService {
    private final UserCredentialRepository credentials;
    public CustomUserDetailsService(UserCredentialRepository credentials) { this.credentials = credentials; }
    @Override
    public PulsePassPrincipal loadUserByUsername(String identifier) {
        return credentials.findByUserUsername(identifier)
                .or(() -> credentials.findByUserEmailIgnoreCase(identifier))
                .map(PulsePassPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
    public PulsePassPrincipal loadById(Long userId) {
        return credentials.findByUserId(userId).map(PulsePassPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
    }
}
