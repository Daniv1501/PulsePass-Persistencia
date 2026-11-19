package com.pulsepass.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final Duration ttl;
    public JwtService(@Value("${pulsepass.security.jwt.secret-base64}") String secret,
                      @Value("${pulsepass.security.jwt.issuer}") String issuer,
                      @Value("${pulsepass.security.jwt.access-token-ttl}") Duration ttl) {
        if (secret == null || secret.isBlank()) throw new IllegalArgumentException("Configura PULSEPASS_JWT_SECRET_BASE64");
        byte[] bytes;
        try { bytes = Decoders.BASE64.decode(secret); }
        catch (RuntimeException ex) { throw new IllegalArgumentException("La clave JWT debe ser Base64 válido"); }
        if (bytes.length < 32) throw new IllegalArgumentException("La clave JWT requiere al menos 32 bytes");
        if (issuer == null || issuer.isBlank() || ttl == null || ttl.isNegative() || ttl.isZero() || ttl.getSeconds() < 1)
            throw new IllegalArgumentException("Emisor o duración JWT inválidos");
        this.key = Keys.hmacShaKeyFor(bytes);
        this.issuer = issuer;
        this.ttl = ttl;
    }
    public String issue(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder().subject(userId.toString()).issuer(issuer).issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl))).id(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256).compact();
    }
    public Long validate(String token) {
        var signed = Jwts.parser().verifyWith(key).requireIssuer(issuer)
                .sig().clear().add(Jwts.SIG.HS256).and().build().parseSignedClaims(token);
        Claims c = signed.getPayload();
        Instant now = Instant.now();
        if (!"HS256".equals(signed.getHeader().getAlgorithm()) || c.getSubject() == null || c.getId() == null
                || c.getId().isBlank() || c.getIssuedAt() == null || c.getExpiration() == null
                || c.getIssuedAt().toInstant().isAfter(now.plusSeconds(30))
                || !c.getExpiration().toInstant().isAfter(now)
                || !c.getExpiration().after(c.getIssuedAt())) throw new MalformedJwtException("JWT inválido");
        try {
            Long id = Long.valueOf(c.getSubject());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) { throw new MalformedJwtException("JWT inválido"); }
    }
    public long ttlSeconds() { return ttl.getSeconds(); }
}
