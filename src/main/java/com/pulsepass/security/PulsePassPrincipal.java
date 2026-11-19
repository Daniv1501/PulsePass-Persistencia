package com.pulsepass.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Set;

public record PulsePassPrincipal(Long userId, String username, String email, String passwordHash,
                                  boolean active, Set<Role> roles) implements UserDetails {
    public PulsePassPrincipal { roles = Set.copyOf(roles); }
    public static PulsePassPrincipal from(UserCredential credential) {
        var user = credential.getUser();
        return new PulsePassPrincipal(user.getId(), user.getUsername(), user.getEmail(),
                credential.getPasswordHash(), user.isActive(), credential.getRoles());
    }
    @Override public String getUsername() { return username; }
    @Override public String getPassword() { return passwordHash; }
    @Override public boolean isEnabled() { return active; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.name())).toList();
    }
    @Override public String toString() { return "PulsePassPrincipal[userId=" + userId + "]"; }
}
