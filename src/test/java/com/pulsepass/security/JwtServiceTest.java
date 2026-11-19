package com.pulsepass.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private final byte[] bytes="test-only-64-byte-key-never-use-in-runtime-012345678901234567890123".getBytes(StandardCharsets.UTF_8);
    private final String secret=Base64.getEncoder().encodeToString(bytes);
    private final JwtService service=new JwtService(secret,"pulsepass-api",Duration.ofMinutes(15));
    @Test void roundTrip() { assertThat(service.validate(service.issue(12L))).isEqualTo(12L); }
    @Test void absentSecretFailsStartup() { assertThatThrownBy(()->new JwtService("","issuer",Duration.ofMinutes(15))).isInstanceOf(IllegalArgumentException.class); }
    @Test void weakSecretFailsStartup() { assertThatThrownBy(()->new JwtService(Base64.getEncoder().encodeToString(new byte[12]),"issuer",Duration.ofMinutes(15))).isInstanceOf(IllegalArgumentException.class); }
    @ParameterizedTest @ValueSource(strings={"exp","iat","sub","jti"})
    void requiredClaims(String missing) {
        var builder=Jwts.builder().issuer("pulsepass-api");
        if (!missing.equals("exp")) builder.expiration(Date.from(Instant.now().plusSeconds(900)));
        if (!missing.equals("iat")) builder.issuedAt(new Date());
        if (!missing.equals("sub")) builder.subject("1");
        if (!missing.equals("jti")) builder.id("test-id");
        String token=builder.signWith(Keys.hmacShaKeyFor(bytes),Jwts.SIG.HS256).compact();
        assertThatThrownBy(()->service.validate(token)).isInstanceOf(JwtException.class);
    }
    @Test void expired() { rejects("pulsepass-api",Instant.now().minusSeconds(60),Instant.now().minusSeconds(1),Jwts.SIG.HS256); }
    @Test void futureIssuedAt() { rejects("pulsepass-api",Instant.now().plusSeconds(100),Instant.now().plusSeconds(900),Jwts.SIG.HS256); }
    @Test void wrongIssuer() { rejects("another-app",Instant.now(),Instant.now().plusSeconds(900),Jwts.SIG.HS256); }
    @Test void wrongAlgorithm() { rejects("pulsepass-api",Instant.now(),Instant.now().plusSeconds(900),Jwts.SIG.HS512); }
    @Test void badSignature() {
        var other=Jwts.SIG.HS256.key().build();
        String token=Jwts.builder().subject("1").issuer("pulsepass-api").issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(900))).id("x").signWith(other,Jwts.SIG.HS256).compact();
        assertThatThrownBy(()->service.validate(token)).isInstanceOf(JwtException.class);
    }
    private void rejects(String issuer,Instant issued,Instant expiration,io.jsonwebtoken.security.MacAlgorithm alg) {
        String token=Jwts.builder().subject("1").issuer(issuer).issuedAt(Date.from(issued)).expiration(Date.from(expiration))
                .id("test-id").signWith(Keys.hmacShaKeyFor(bytes),alg).compact();
        assertThatThrownBy(()->service.validate(token)).isInstanceOf(JwtException.class);
    }
}
