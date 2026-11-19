package com.pulsepass.security;

import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;

@Service("authorizationService")
@Transactional(readOnly=true)
public class AuthorizationService {
    private final UserRepository users;
    private final TicketRepository tickets;
    public AuthorizationService(UserRepository users, TicketRepository tickets) { this.users=users; this.tickets=tickets; }
    public boolean hasRole(Role... roles) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && Arrays.stream(roles).anyMatch(role -> auth.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_"+role.name())));
    }
    public PulsePassPrincipal current() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof PulsePassPrincipal principal) return principal;
        throw new AuthenticationCredentialsNotFoundException("Se requiere autenticación válida");
    }
    public boolean canReadUserEmail(String email) {
        return hasRole(Role.ADMIN) || ownsEmail(email);
    }
    public boolean canReadUserUsername(String username) {
        if (hasRole(Role.ADMIN)) return true;
        var user = users.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return current().userId().equals(user.getId());
    }
    public boolean ownsEmail(String email) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return current().userId().equals(user.getId());
    }
    public boolean canReadTicket(String ticketCode) {
        return hasRole(Role.STAFF, Role.ADMIN) || ownsTicket(ticketCode);
    }
    public boolean canCancelTicket(String ticketCode) {
        return hasRole(Role.ADMIN) || (hasRole(Role.USER) && ownsTicket(ticketCode));
    }
    private boolean ownsTicket(String ticketCode) {
        var ticket = tickets.findByTicketCode(ticketCode).orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado"));
        return current().userId().equals(ticket.getUser().getId());
    }
}
