package com.pulsepass.security;

import com.pulsepass.domain.*;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.repository.*;
import com.pulsepass.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SecurityIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("pulsepass_security_test").withUsername("pulsepass").withPassword("pulsepass");
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired UserCredentialRepository credentials;
    @Autowired VenueRepository venues;
    @Autowired PasswordEncoder encoder;
    @Autowired AuthService auth;
    @Autowired PlatformTransactionManager transactions;
    static final String PASSWORD="SoloParaPruebas2026!";

    private String register(String username) throws Exception {
        String body=mapper.writeValueAsString(new RegisterUserRequest(username,username+"@pulsepass.test","Prueba","Persona",
                null,"Santa Marta",LocalDate.of(1990,1,1),PASSWORD));
        mvc.perform(post("/api/users").contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist()).andExpect(jsonPath("$.accessToken").doesNotExist());
        return login(username,PASSWORD);
    }
    private String login(String identifier,String password) throws Exception {
        var result=mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(mapper.writeValueAsString(Map.of("identifier",identifier,"password",password))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().doesNotExist("Set-Cookie")).andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asString();
    }
    private String code(org.springframework.test.web.servlet.MvcResult result,String field) throws Exception {
        return mapper.readTree(result.getResponse().getContentAsString()).get(field).asString();
    }
    @Test void registerPersistsHashAndUserRoleAndSupportsEmailLogin() throws Exception {
        String token=register("hash-user");
        Long id=users.findByUsername("hash-user").orElseThrow().getId();
        String hash=jdbc.queryForObject("select password_hash from user_credentials where user_id=?",String.class,id);
        assertThat(hash).startsWith("$2").isNotEqualTo(PASSWORD);
        assertThat(encoder.matches(PASSWORD,hash)).isTrue();
        assertThat(jdbc.queryForList("select role from user_roles where user_id=?",String.class,id)).containsExactly("USER");
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(id)).andExpect(jsonPath("$.roles[0]").value("USER"));
        login("HASH-USER@PULSEPASS.TEST",PASSWORD);
    }
    @Test void fullHttpBusinessAndOwnershipFlow() throws Exception {
        String a=register("flow-andrea"); String b=register("flow-carlos");
        String admin=register("flow-admin"); String staff=register("flow-staff");
        jdbc.update("update user_roles set role='ADMIN' where user_id=(select id from users where username='flow-admin')");
        jdbc.update("update user_roles set role='STAFF' where user_id=(select id from users where username='flow-staff')");
        venues.save(new Venue("HTTP-VEN","Sede HTTP","Santa Marta","Cra 1",20));
        String body="""
            {"eventCode":"HTTP-EVT","name":"Festival HTTP","description":"Integración real","category":"MUSIC",
             "eventDate":"2099-12-12T20:00:00","minimumAge":0,"venueCode":"HTTP-VEN"}
            """;
        mvc.perform(post("/api/events").header("Authorization","Bearer "+admin).contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("DRAFT"));
        mvc.perform(get("/api/events/HTTP-EVT")).andExpect(status().isNotFound());
        Long artistId=jdbc.queryForObject("select id from artists where stage_name='Solar Beat'",Long.class);
        mvc.perform(post("/api/events/HTTP-EVT/artists/"+artistId).header("Authorization","Bearer "+admin)).andExpect(status().isOk());
        mvc.perform(patch("/api/events/HTTP-EVT/publish").header("Authorization","Bearer "+a)).andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("select status from events where event_code='HTTP-EVT'",String.class)).isEqualTo("DRAFT");
        mvc.perform(patch("/api/events/HTTP-EVT/publish").header("Authorization","Bearer "+admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
        String purchase="{\"userEmail\":\"flow-andrea@pulsepass.test\",\"eventCode\":\"HTTP-EVT\",\"type\":\"VIP\"}";
        String cancelable=code(mvc.perform(post("/api/tickets").header("Authorization","Bearer "+a)
                .contentType("application/json").content(purchase)).andExpect(status().isCreated()).andReturn(),"ticketCode");
        String usable=code(mvc.perform(post("/api/tickets").header("Authorization","Bearer "+a)
                .contentType("application/json").content(purchase)).andExpect(status().isCreated()).andReturn(),"ticketCode");
        mvc.perform(post("/api/tickets").header("Authorization","Bearer "+b).contentType("application/json").content(purchase))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("select count(*) from tickets where event_id=(select id from events where event_code='HTTP-EVT')",Long.class)).isEqualTo(2);
        mvc.perform(get("/api/tickets/"+cancelable).header("Authorization","Bearer "+b)).andExpect(status().isForbidden());
        mvc.perform(patch("/api/tickets/"+cancelable+"/cancel").header("Authorization","Bearer "+b)).andExpect(status().isForbidden());
        mvc.perform(get("/api/tickets/"+cancelable).header("Authorization","Bearer "+a)).andExpect(jsonPath("$.status").value("PAID"));
        mvc.perform(patch("/api/tickets/"+cancelable+"/cancel").header("Authorization","Bearer "+a)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(patch("/api/tickets/"+cancelable+"/cancel").header("Authorization","Bearer "+a)).andExpect(status().isConflict());
        mvc.perform(patch("/api/tickets/"+usable+"/use").header("Authorization","Bearer "+staff)).andExpect(status().isOk());
        mvc.perform(patch("/api/tickets/"+usable+"/use").header("Authorization","Bearer "+staff)).andExpect(status().isConflict());
        mvc.perform(get("/api/tickets/"+usable).header("Authorization","Bearer "+a)).andExpect(jsonPath("$.status").value("USED"));
        mvc.perform(get("/api/events/HTTP-EVT/tickets/paid").header("Authorization","Bearer "+staff)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }
    @Test void disabledUserLosesTokenAccessImmediately() throws Exception {
        String token=register("disabled-after-login");
        jdbc.update("update users set active=false where username='disabled-after-login'");
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json").content(mapper.writeValueAsString(Map.of(
                "identifier","disabled-after-login","password",PASSWORD)))).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }
    @Test void atomicRegistrationRollsBackUserCredentialAndRoles() {
        var tx=new TransactionTemplate(transactions);
        assertThatThrownBy(()->tx.execute(status->{
            auth.register(new RegisterUserRequest("rollback-user","rollback-user@pulsepass.test","Prueba","Rollback",null,null,
                    LocalDate.of(1990,1,1),PASSWORD));
            throw new IllegalStateException("Simulated failure before commit");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(users.findByUsername("rollback-user")).isEmpty();
        assertThat(jdbc.queryForObject("select count(*) from user_credentials c join users u on u.id=c.user_id where u.username='rollback-user'",Long.class)).isZero();
    }
    @Test void registrationCannotEscalateRoles() throws Exception {
        String body="""
            {"username":"injected-role","email":"injected-role@pulsepass.test","firstName":"Prueba","lastName":"Usuario",
             "birthDate":"1990-01-01","password":"SoloParaPruebas2026!","role":"ADMIN","roles":["ADMIN"]}
            """;
        mvc.perform(post("/api/users").contentType("application/json").content(body)).andExpect(status().isCreated());
        String token=login("injected-role",PASSWORD);
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }
    @Test void legacyUserDoesNotAcquireDefaultCredentials() {
        users.save(new User("legacy-only","legacy-only@pulsepass.test"));
        assertThat(credentials.findByUserUsername("legacy-only")).isEmpty();
        assertThat(jdbc.queryForList("select version from flyway_schema_history",String.class)).contains("4");
    }
}
