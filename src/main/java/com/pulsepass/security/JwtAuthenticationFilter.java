package com.pulsepass.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final CustomUserDetailsService users;
    private final SecurityErrorHandler errors;
    public JwtAuthenticationFilter(JwtService jwt, CustomUserDetailsService users, SecurityErrorHandler errors) {
        this.jwt = jwt; this.users = users; this.errors = errors;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.regionMatches(true, 0, "Bearer ", 0, 7) || header.substring(7).isBlank())
                    throw new BadCredentialsException("Bearer inválido");
                var principal = users.loadById(jwt.validate(header.substring(7)));
                if (!principal.isEnabled()) throw new BadCredentialsException("Credenciales inválidas");
                var auth = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                errors.commence(request, response, new BadCredentialsException("Bearer inválido"));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
