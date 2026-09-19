package com.pulsepass;

import com.pulsepass.domain.*;
import com.pulsepass.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                .withDatabaseName("pulsepass_test")
                .withUsername("pulsepass")
                .withPassword("pulsepass");

    @Autowired private VenueRepository venueRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    // ---------- QT-001: Flyway aplica V1, V2 y V3 ----------
    @Test
    void flywayAppliesAllMigrations() {
        List<String> versions = jdbcTemplate.queryForList(
                "select version from flyway_schema_history order by installed_rank",
                String.class);

        assertThat(versions).contains("1", "2", "3");

        // el catálogo de V2 debe estar presente
        assertThat(artistRepository.findByStageName("Solar Beat")).isPresent();
    }

    // ---------- QT-003: Venue 1:N Event ----------
    @Test
    void venueHasManyEvents() {
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 5000);
        venueRepository.save(venue);

        Event event1 = new Event("CMF-2026", "Caribbean Music Fest 2026", "Festival de música",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 5, 20, 0), 0);
        Event event2 = new Event("CMF-2026-B", "Caribbean Music Fest - Día 2", "Segundo día",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.of(2026, 12, 6, 20, 0), 0);

        venue.addEvent(event1);
        venue.addEvent(event2);
        eventRepository.save(event1);
        eventRepository.save(event2);

        List<Event> events = eventRepository.findByVenueCode("VEN-SMR-01");

        assertThat(events).hasSize(2);
        assertThat(events).allMatch(e -> e.getVenue().getCode().equals("VEN-SMR-01"));
    }

    // ---------- QT-004: User 1:1 UserProfile ----------
    @Test
    void userHasOneUserProfile() {
        User user = new User("andrea", "andrea@example.com");
        UserProfile profile = new UserProfile("Andrea", "Gomez", "3001234567", "Santa Marta",
                LocalDate.of(1998, 5, 10));

        user.assignProfile(profile);
        userRepository.save(user);

        User persisted = userRepository.findByUsername("andrea").orElseThrow();

        assertThat(persisted.getProfile()).isNotNull();
        assertThat(persisted.getProfile().getFirstName()).isEqualTo("Andrea");
    }

    // ---------- QT-005: Event N:M Artist ----------
    @Test
    void eventHasManyArtists() {
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 5000);
        venueRepository.save(venue);

        Event event = new Event("CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 5, 20, 0), 0);
        venue.addEvent(event);

        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageName("Neon Waves").orElseThrow();
        Artist caribbeanSound = artistRepository.findByStageName("Caribbean Sound").orElseThrow();

        event.addArtist(solarBeat);
        event.addArtist(neonWaves);
        event.addArtist(caribbeanSound);
        eventRepository.save(event);

        Event persisted = eventRepository.findByEventCode("CMF-2026").orElseThrow();
        assertThat(persisted.getArtists()).hasSize(3);
    }

    // ---------- QT-006: Ticket -> User y Ticket -> Event ----------
    @Test
    void ticketReferencesUserAndEvent() {
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 5000);
        venueRepository.save(venue);

        Event event = new Event("CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 5, 20, 0), 0);
        venue.addEvent(event);
        eventRepository.save(event);

        User user = new User("andrea", "andrea@example.com");
        userRepository.save(user);

        Ticket ticket = new Ticket("TCK-0001", TicketType.VIP, new BigDecimal("250000.00"),
                TicketStatus.PAID, LocalDateTime.now(), user, event);
        ticketRepository.save(ticket);

        Ticket persisted = ticketRepository.findAll().get(0);
        assertThat(persisted.getUser().getUsername()).isEqualTo("andrea");
        assertThat(persisted.getEvent().getEventCode()).isEqualTo("CMF-2026");
    }

    // ---------- QT-007: Query Methods simples y con navegación ----------
    @Test
    void queryMethodsWork() {
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 5000);
        venueRepository.save(venue);

        Event published = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 5, 20, 0), 0);
        Event draft = new Event("CMF-2027", "Caribbean Music Fest 2027", "Festival futuro",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.of(2027, 12, 5, 20, 0), 0);
        venue.addEvent(published);
        venue.addEvent(draft);
        eventRepository.save(published);
        eventRepository.save(draft);

        // findByEventCode (Query Method simple)
        assertThat(eventRepository.findByEventCode("CMF-2026")).isPresent();

        // findByStatusOrderByEventDateAsc (Query Method simple)
        List<Event> publishedEvents = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);
        assertThat(publishedEvents).extracting(Event::getEventCode).containsExactly("CMF-2026");

        // findByUserEmailAndStatus (Query Method navegando relación)
        User user = new User("carlos", "carlos@example.com");
        userRepository.save(user);
        ticketRepository.save(new Ticket("TCK-1001", TicketType.GENERAL, new BigDecimal("120000.00"),
                TicketStatus.PAID, LocalDateTime.now(), user, published));

        List<Ticket> paidTickets = ticketRepository.findByUserEmailAndStatus("carlos@example.com", TicketStatus.PAID);
        assertThat(paidTickets).hasSize(1);
    }

    // ---------- QT-008: JPQL con JOIN y COUNT ----------
    @Test
    void jpqlWithJoinAndCountWork() {
        Venue venue = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 5000);
        venueRepository.save(venue);

        Event event = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.of(2026, 12, 5, 20, 0), 0);
        venue.addEvent(event);

        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        event.addArtist(solarBeat);
        eventRepository.save(event);

        // JOIN: eventos por artista, sin duplicados
        List<Event> events = eventRepository.findByArtistStageName("Solar Beat");
        assertThat(events).extracting(Event::getEventCode).containsExactly("CMF-2026");

        // COUNT: tickets PAID de un evento
        User andrea = new User("andrea", "andrea@example.com");
        User carlos = new User("carlos", "carlos@example.com");
        User laura = new User("laura", "laura@example.com");
        userRepository.save(andrea);
        userRepository.save(carlos);
        userRepository.save(laura);

        ticketRepository.save(new Ticket("TCK-2001", TicketType.VIP, new BigDecimal("250000.00"),
                TicketStatus.PAID, LocalDateTime.now(), andrea, event));
        ticketRepository.save(new Ticket("TCK-2002", TicketType.GENERAL, new BigDecimal("120000.00"),
                TicketStatus.PAID, LocalDateTime.now(), carlos, event));
        ticketRepository.save(new Ticket("TCK-2003", TicketType.GENERAL, new BigDecimal("120000.00"),
                TicketStatus.RESERVED, LocalDateTime.now(), laura, event));

        long paidCount = ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID);
        assertThat(paidCount).isEqualTo(2);
    }

    // ---------- QT-009: restricción UNIQUE con saveAndFlush ----------
    @Test
    void savingDuplicateVenueCodeViolatesUniqueConstraint() {
        Venue venue1 = new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 5000);
        venueRepository.saveAndFlush(venue1);

        Venue venue2 = new Venue("VEN-SMR-01", "Otro Venue", "Barranquilla", "Cll 10 #5-6", 3000);

        assertThatThrownBy(() -> venueRepository.saveAndFlush(venue2))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}