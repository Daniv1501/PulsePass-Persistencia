package com.pulsepass.security;

import com.pulsepass.config.E2eFixtureSeeder;
import com.pulsepass.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={"PULSEPASS_E2E_ADMIN_PASSWORD=AdminSoloPruebas2026!",
        "PULSEPASS_E2E_STAFF_PASSWORD=StaffSoloPruebas2026!", "PULSEPASS_E2E_INACTIVE_PASSWORD=InactivoPruebas2026!"})
@ActiveProfiles("e2e")
@Testcontainers
@Transactional
class E2eFixturesIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer postgres=new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("pulsepass_e2e").withUsername("pulsepass").withPassword("pulsepass");
    @DynamicPropertySource static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",postgres::getJdbcUrl);
    }
    @Autowired UserCredentialRepository credentials;
    @Autowired UserRepository users;
    @Autowired VenueRepository venues;
    @Autowired EventRepository events;
    @Autowired PasswordEncoder encoder;
    @Autowired E2eFixtureSeeder seeder;
    @Autowired JdbcTemplate jdbc;
    @Test void provisionsDedicatedFixturesWithoutPlaintextPasswords() {
        var admin=credentials.findByUserUsername("e2e-admin").orElseThrow();
        assertThat(admin.getRoles()).containsExactly(Role.ADMIN);
        assertThat(encoder.matches("AdminSoloPruebas2026!",admin.getPasswordHash())).isTrue();
        assertThat(users.findByUsername("e2e-inactive").orElseThrow().isActive()).isFalse();
        assertThat(venues.findByCode("VEN-SMR-01").orElseThrow().getCapacity()).isEqualTo(3);
        assertThat(events.findByEventCode("E2E-DRAFT").orElseThrow().getStatus().name()).isEqualTo("DRAFT");
    }
    @Test void restartingDoesNotResetRolesOrDuplicateFixtures() {
        long initial=users.count();
        jdbc.update("update user_roles set role='USER' where user_id=(select id from users where username='e2e-staff')");
        seeder.run(new DefaultApplicationArguments());
        assertThat(users.count()).isEqualTo(initial);
        assertThat(jdbc.queryForList("select role from user_roles where user_id=(select id from users where username='e2e-staff')",String.class))
                .containsExactly("USER");
    }
}
