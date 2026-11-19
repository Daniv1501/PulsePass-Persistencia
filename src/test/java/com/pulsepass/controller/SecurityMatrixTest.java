package com.pulsepass.controller;

import com.pulsepass.domain.EventStatus;
import com.pulsepass.security.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.HttpMethod.valueOf;

@WebMvcTest({VenueController.class,ArtistController.class,EventController.class,UserController.class,TicketController.class,AuthController.class})
class SecurityMatrixTest extends ControllerTestSupport {
    @ParameterizedTest
    @CsvSource({"POST,/api/events","PATCH,/api/events/CMF-2026/publish","POST,/api/events/CMF-2026/artists/1",
            "POST,/api/tickets","GET,/api/tickets/TKT-A","GET,/api/tickets/by-user?email=andrea@pulsepass.test",
            "PATCH,/api/tickets/TKT-A/cancel","PATCH,/api/tickets/TKT-A/use","GET,/api/events/CMF-2026/tickets/paid",
            "GET,/api/users/by-email?email=andrea@pulsepass.test","GET,/api/users/by-username?username=andrea","GET,/api/auth/me"})
    void anonymousDenied(String method,String path) throws Exception {
        mvc.perform(request(valueOf(method),path)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.details").isMap()).andExpect(content().contentTypeCompatibleWith("application/json"));
        verifyNoInteractions(events,tickets,users);
    }
    @ParameterizedTest
    @CsvSource({"POST,/api/events","PATCH,/api/events/CMF-2026/publish","POST,/api/events/CMF-2026/artists/1",
            "PATCH,/api/tickets/TKT-A/use","GET,/api/events/CMF-2026/tickets/paid"})
    void userCannotOperatePrivilegedRoutes(String method,String path) throws Exception {
        mvc.perform(request(valueOf(method),path).with(actor(Role.USER))).andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(events,tickets);
    }
    @ParameterizedTest
    @CsvSource({"GET,/api/tickets/TKT-B","PATCH,/api/tickets/TKT-B/cancel",
            "GET,/api/tickets/by-user?email=carlos@pulsepass.test","GET,/api/users/by-email?email=carlos@pulsepass.test",
            "GET,/api/users/by-username?username=carlos"})
    void foreignResourcesForbidden(String method,String path) throws Exception {
        mvc.perform(request(valueOf(method),path).with(actor(Role.USER))).andExpect(status().isForbidden());
        verifyNoInteractions(tickets,users);
    }
    @Test void cannotBuyForAnotherUser() throws Exception {
        mvc.perform(post("/api/tickets").with(actor(Role.USER)).contentType("application/json")
                .content(TICKET_JSON.replace("andrea@pulsepass.test","carlos@pulsepass.test"))).andExpect(status().isForbidden());
        verifyNoInteractions(tickets);
    }
    @Test void malformedBearerEvenOnPublicRoute() throws Exception {
        mvc.perform(get("/api/venues/active").header("Authorization","Bearer invalid"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(venues);
    }
    @Test void validJwtLoadsCurrentIdentity() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+jwt.issue(1L)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("andrea"));
        verify(credentials).findByUserId(1L);
    }
    @Test void inactiveUserLosesTokenAccess() throws Exception {
        andrea.setActive(false);
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+jwt.issue(1L))).andExpect(status().isUnauthorized());
    }
    @Test void revokedRolesDoNotSurviveInToken() throws Exception {
        String token=jwt.issue(1L);
        when(credentials.findByUserId(1L)).thenReturn(Optional.of(new UserCredential(andrea,HASH,Set.of(Role.USER))));
        mvc.perform(post("/api/events").header("Authorization","Bearer "+token).contentType("application/json").content(EVENT_JSON))
                .andExpect(status().isForbidden());
        verifyNoInteractions(events);
    }
    @Test void queryTokenDoesNotAuthenticate() throws Exception {
        mvc.perform(get("/api/auth/me").param("access_token",jwt.issue(1L))).andExpect(status().isUnauthorized());
    }
    @Test void expiredSignedJwtReturns401BeforeController() throws Exception {
        var key=io.jsonwebtoken.security.Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(
                "cHVsc2VwYXNzLXRlc3Qtb25seS1ub3QtcHJvZHVjdGlvbi1rZXk="));
        String token=io.jsonwebtoken.Jwts.builder().subject("1").issuer("pulsepass-api").id("expired-test")
                .issuedAt(Date.from(java.time.Instant.now().minusSeconds(120)))
                .expiration(Date.from(java.time.Instant.now().minusSeconds(60)))
                .signWith(key,io.jsonwebtoken.Jwts.SIG.HS256).compact();
        mvc.perform(get("/api/tickets/TKT-A").header("Authorization","Bearer "+token))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(tickets);
    }
    @Test void tamperedSignatureReturns401BeforeController() throws Exception {
        String token=jwt.issue(1L);
        int index=token.lastIndexOf('.')+1;
        String altered=token.substring(0,index)+(token.charAt(index)=='A'?'B':'A')+token.substring(index+1);
        mvc.perform(get("/api/tickets/TKT-A").header("Authorization","Bearer "+altered))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(tickets);
    }
    @Test void publicArtistFilterHidesDrafts() throws Exception {
        when(events.findByArtist("Solar Beat")).thenReturn(List.of(summary(EventStatus.DRAFT),summary(EventStatus.PUBLISHED)));
        mvc.perform(get("/api/events/by-artist").param("stageName","Solar Beat"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].status").value("PUBLISHED"));
        mvc.perform(get("/api/events/by-artist").param("stageName","Solar Beat").with(actor(Role.ADMIN)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }
    @Test void unknownFailureIsSafe() throws Exception {
        when(venues.findActiveVenues()).thenThrow(new IllegalStateException("password SQL secret"));
        mvc.perform(get("/api/venues/active")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocurrió un error inesperado"));
    }
}
