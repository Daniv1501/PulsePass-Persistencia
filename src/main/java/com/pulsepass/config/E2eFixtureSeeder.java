package com.pulsepass.config;

import com.pulsepass.domain.*;
import com.pulsepass.repository.*;
import com.pulsepass.security.*;
import com.pulsepass.validation.BcryptPasswordValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/** Exclusivo de una base E2E identificada; no modifica usuarios ni contraseñas existentes. */
@Component
@Profile("e2e")
public class E2eFixtureSeeder implements ApplicationRunner {
    private final VenueRepository venues;
    private final EventRepository events;
    private final ArtistRepository artists;
    private final UserRepository users;
    private final UserCredentialRepository credentials;
    private final PasswordEncoder encoder;
    private final String databaseUrl;
    private final String adminPassword;
    private final String staffPassword;
    private final String inactivePassword;

    public E2eFixtureSeeder(VenueRepository venues, EventRepository events, ArtistRepository artists,
                           UserRepository users, UserCredentialRepository credentials, PasswordEncoder encoder,
                           @Value("${spring.datasource.url}") String databaseUrl,
                           @Value("${PULSEPASS_E2E_ADMIN_PASSWORD}") String adminPassword,
                           @Value("${PULSEPASS_E2E_STAFF_PASSWORD}") String staffPassword,
                           @Value("${PULSEPASS_E2E_INACTIVE_PASSWORD}") String inactivePassword) {
        this.venues=venues; this.events=events; this.artists=artists; this.users=users;
        this.credentials=credentials; this.encoder=encoder; this.databaseUrl=databaseUrl;
        this.adminPassword=adminPassword; this.staffPassword=staffPassword; this.inactivePassword=inactivePassword;
    }
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!databaseUrl.matches("^jdbc:postgresql://[^/]+/pulsepass_e2e(?:\\?.*)?$"))
            throw new IllegalStateException("El perfil e2e requiere exclusivamente la base pulsepass_e2e");
        validatePassword(adminPassword); validatePassword(staffPassword); validatePassword(inactivePassword);
        account("e2e-admin", adminPassword, Role.ADMIN, true);
        account("e2e-staff", staffPassword, Role.STAFF, true);
        account("e2e-inactive", inactivePassword, Role.USER, false);
        Venue main = venue("VEN-SMR-01", "Marina Convention Center", 3, true);
        Venue ample = venue("VEN-E2E-LARGE", "Auditorio de pruebas", 100, true);
        venue("VEN-E2E-INACTIVE", "Sede inactiva", 20, false);
        sample("CMF-2026", "Caribbean Music Fest 2026", main, EventStatus.PUBLISHED, 18);
        sample("E2E-PUBLISHED", "PulsePass · Experiencias", ample, EventStatus.PUBLISHED, 0);
        sample("E2E-DRAFT", "Borrador de pruebas", ample, EventStatus.DRAFT, 0);
        sample("E2E-SOLD-OUT", "Evento agotado", main, EventStatus.SOLD_OUT, 0);
    }
    private void validatePassword(String password) {
        if (!new BcryptPasswordValidator().isValid(password, null))
            throw new IllegalArgumentException("Cada contraseña E2E requiere 12 caracteres y máximo 72 bytes UTF-8");
    }
    private void account(String username, String password, Role role, boolean active) {
        if (users.findByUsername(username).isPresent()) return;
        User user = new User(username, username+"@pulsepass.test");
        user.setActive(active);
        user.assignProfile(new UserProfile("Fixture", username, null, "Santa Marta", LocalDate.of(1990,1,1)));
        users.save(user);
        credentials.save(new UserCredential(user, encoder.encode(password), Set.of(role)));
    }
    private Venue venue(String code, String name, int capacity, boolean active) {
        return venues.findByCode(code).orElseGet(() -> {
            var v = new Venue(code, name, "Santa Marta", "Carrera 1 # 2-3", capacity);
            v.setActive(active);
            return venues.save(v);
        });
    }
    private void sample(String code, String name, Venue venue, EventStatus status, int age) {
        if (events.existsByEventCode(code)) return;
        var event = new Event(code, name, "Escenario académico PulsePass", EventCategory.MUSIC, status,
                LocalDateTime.now().plusMonths(6), age);
        venue.addEvent(event);
        artists.findByStageName("Solar Beat").ifPresent(event::addArtist);
        events.save(event);
    }
}
