package com.pulsepass.service;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.domain.User;
import com.pulsepass.domain.Venue;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.mapper.EventMapperImpl;
import com.pulsepass.mapper.TicketMapperImpl;
import com.pulsepass.mapper.UserMapperImpl;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.impl.EventServiceImpl;
import com.pulsepass.service.impl.TicketServiceImpl;
import com.pulsepass.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Escenario principal de aceptación (PRD secciones 47 y 48, AC-001 a AC-011).
 *
 * Sigue siendo un unit test: sin Spring, sin PostgreSQL, sin Testcontainers. Los repositories son
 * mocks de Mockito respaldados por colecciones en memoria, de modo que los servicios REALES
 * (EventServiceImpl, UserServiceImpl, TicketServiceImpl) con sus mappers MapStruct reales
 * pueden ejecutar el escenario completo de punta a punta.
 */
class PulsePassAcceptanceScenarioTest {

    private static final String EVENT_CODE = "CMF-2026";

    private final Map<String, Venue> venues = new LinkedHashMap<>();
    private final Map<String, Event> events = new LinkedHashMap<>();
    private final Map<Long, Artist> artists = new LinkedHashMap<>();
    private final Map<String, User> users = new HashMap<>(); // clave: email en minúsculas
    private final List<Ticket> tickets = new ArrayList<>();

    private EventServiceImpl eventService;
    private UserServiceImpl userService;
    private TicketServiceImpl ticketService;

    private LocalDateTime eventDate;

    @BeforeEach
    void setUp() {
        eventDate = LocalDateTime.now().plusMonths(6);

        // --- Venue: VEN-SMR-01, capacidad 3, activo ---
        venues.put("VEN-SMR-01",
                new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 3));

        // --- Artistas: Solar Beat, Neon Waves, Caribbean Sound ---
        addArtist(1L, "Solar Beat", "Electronic");
        addArtist(2L, "Neon Waves", "Synthwave");
        addArtist(3L, "Caribbean Sound", "Caribbean");

        VenueRepository venueRepository = mock(VenueRepository.class);
        when(venueRepository.findByCode(anyString())).thenAnswer(inv -> {
            String code = inv.getArgument(0);
            return Optional.ofNullable(venues.get(code));
        });

        ArtistRepository artistRepository = mock(ArtistRepository.class);
        when(artistRepository.findById(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return Optional.ofNullable(artists.get(id));
        });

        EventRepository eventRepository = mock(EventRepository.class);
        when(eventRepository.existsByEventCode(anyString())).thenAnswer(inv -> {
            String code = inv.getArgument(0);
            return events.containsKey(code);
        });
        when(eventRepository.findByEventCode(anyString())).thenAnswer(inv -> {
            String code = inv.getArgument(0);
            return Optional.ofNullable(events.get(code));
        });
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event event = inv.getArgument(0);
            events.put(event.getEventCode(), event);
            return event;
        });

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.existsByUsername(anyString())).thenAnswer(inv -> {
            String username = inv.getArgument(0);
            return users.values().stream().anyMatch(u -> u.getUsername().equals(username));
        });
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenAnswer(inv -> {
            String email = inv.getArgument(0);
            return users.containsKey(email.toLowerCase());
        });
        when(userRepository.findByEmailIgnoreCase(anyString())).thenAnswer(inv -> {
            String email = inv.getArgument(0);
            return Optional.ofNullable(users.get(email.toLowerCase()));
        });
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            users.put(user.getEmail().toLowerCase(), user);
            return user;
        });

        TicketRepository ticketRepository = mock(TicketRepository.class);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket ticket = inv.getArgument(0);
            if (!tickets.contains(ticket)) {
                tickets.add(ticket);
            }
            return ticket;
        });
        when(ticketRepository.findByTicketCode(anyString())).thenAnswer(inv -> {
            String code = inv.getArgument(0);
            return tickets.stream().filter(t -> t.getTicketCode().equals(code)).findFirst();
        });
        when(ticketRepository.countByEventEventCodeAndStatus(anyString(), any(TicketStatus.class)))
                .thenAnswer(inv -> {
                    String code = inv.getArgument(0);
                    TicketStatus status = inv.getArgument(1);
                    return tickets.stream()
                            .filter(t -> t.getEvent().getEventCode().equals(code))
                            .filter(t -> t.getStatus() == status)
                            .count();
                });

        // Fase 4: el escenario de negocio se ejecuta como personal autorizado (STAFF/ADMIN);
        // la seguridad por rol/propiedad se prueba en las suites de seguridad.
        AuthorizationService authorizationService = mock(AuthorizationService.class);
        when(authorizationService.isStaffOrAdmin()).thenReturn(true);

        eventService = new EventServiceImpl(eventRepository, venueRepository, artistRepository,
                new EventMapperImpl(), authorizationService);
        userService = new UserServiceImpl(userRepository, new UserMapperImpl());
        ticketService = new TicketServiceImpl(userRepository, eventRepository, ticketRepository,
                new TicketMapperImpl());

        registerUsers();
    }

    private void addArtist(long id, String stageName, String genre) {
        Artist artist = new Artist(stageName, "Colombia", genre);
        ReflectionTestUtils.setField(artist, "id", id);
        artists.put(id, artist);
    }

    private void registerUsers() {
        LocalDate eventDay = eventDate.toLocalDate();
        register("andrea", "andrea@email.com", "Andrea", eventDay.minusYears(25));   // 25 años al evento
        register("carlos", "carlos@email.com", "Carlos", eventDay.minusYears(21));   // 21 años
        register("laura", "laura@email.com", "Laura", eventDay.minusYears(17));      // 17 años
        register("miguel", "miguel@email.com", "Miguel", eventDay.minusYears(30));   // 30 años
        register("daniel", "daniel@email.com", "Daniel", eventDay.minusYears(28));   // tercer adulto
        users.get("miguel@email.com").setActive(false);                               // Miguel inactivo
    }

    private void register(String username, String email, String firstName, LocalDate birthDate) {
        userService.register(new RegisterUserRequest(
                username, email, firstName, "Test", "3000000000", "Santa Marta", birthDate,
                "UnaFraseSeguraLarga2026!"));
    }

    private EventResponse createEvent() {
        return eventService.create(new CreateEventRequest(
                EVENT_CODE, "Caribbean Music Fest 2026", "Festival de música del Caribe",
                EventCategory.MUSIC, eventDate, 18, "VEN-SMR-01"));
    }

    private void createPublishAndAddArtists() {
        createEvent();
        eventService.addArtist(EVENT_CODE, 1L);
        eventService.addArtist(EVENT_CODE, 2L);
        eventService.addArtist(EVENT_CODE, 3L);
        eventService.publish(EVENT_CODE);
    }

    private TicketResponse buy(String email, TicketType type) {
        return ticketService.purchase(new PurchaseTicketRequest(email, EVENT_CODE, type));
    }

    // ---------- AC-001 ----------
    @Test
    void ac001_eventCanBeCreatedAndStartsAsDraft() {
        EventResponse response = createEvent();

        assertThat(response.eventCode()).isEqualTo(EVENT_CODE);
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
        assertThat(response.minimumAge()).isEqualTo(18);
        assertThat(response.venueCode()).isEqualTo("VEN-SMR-01");
        assertThat(response.venueName()).isEqualTo("Marina Convention Center");
    }

    // ---------- AC-002 ----------
    @Test
    void ac002_eventCanBePublishedWhenRulesAreMet() {
        createEvent();

        EventResponse response = eventService.publish(EVENT_CODE);

        assertThat(response.status()).isEqualTo(EventStatus.PUBLISHED);
    }

    // ---------- AC-003 ----------
    @Test
    void ac003_threeArtistsCanBeAssociated() {
        createEvent();

        eventService.addArtist(EVENT_CODE, 1L);
        eventService.addArtist(EVENT_CODE, 2L);
        EventResponse response = eventService.addArtist(EVENT_CODE, 3L);

        assertThat(response.artists()).containsExactly("Caribbean Sound", "Neon Waves", "Solar Beat");
    }

    // ---------- AC-004 (flujo mental de la sección 54: Andrea compra VIP) ----------
    @Test
    void ac004_andreaCanBuyVipTicket() {
        createPublishAndAddArtists();

        TicketResponse ticket = buy("andrea@email.com", TicketType.VIP);

        assertThat(ticket.status()).isEqualTo(TicketStatus.PAID);
        assertThat(ticket.type()).isEqualTo(TicketType.VIP);
        assertThat(ticket.price()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(ticket.userEmail()).isEqualTo("andrea@email.com");
        assertThat(ticket.eventCode()).isEqualTo(EVENT_CODE);
        assertThat(ticket.eventName()).isEqualTo("Caribbean Music Fest 2026");
    }

    // ---------- AC-005 ----------
    @Test
    void ac005_carlosCanBuy() {
        createPublishAndAddArtists();

        TicketResponse ticket = buy("carlos@email.com", TicketType.GENERAL);

        assertThat(ticket.status()).isEqualTo(TicketStatus.PAID);
        assertThat(ticket.userEmail()).isEqualTo("carlos@email.com");
    }

    // ---------- AC-006 ----------
    @Test
    void ac006_lauraCannotBuyBecauseOfMinimumAge() {
        createPublishAndAddArtists();

        assertThatThrownBy(() -> buy("laura@email.com", TicketType.GENERAL))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("minimum age");

        assertThat(tickets).isEmpty();
    }

    // ---------- AC-007 ----------
    @Test
    void ac007_miguelCannotBuyBecauseHeIsInactive() {
        createPublishAndAddArtists();

        assertThatThrownBy(() -> buy("miguel@email.com", TicketType.GENERAL))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not active");

        assertThat(tickets).isEmpty();
    }

    // ---------- AC-008 ----------
    @Test
    void ac008_thirdAdultBuysLastTicketAndEventBecomesSoldOut() {
        createPublishAndAddArtists();
        buy("andrea@email.com", TicketType.VIP);
        buy("carlos@email.com", TicketType.GENERAL);
        assertThat(events.get(EVENT_CODE).getStatus()).isEqualTo(EventStatus.PUBLISHED);

        TicketResponse last = buy("daniel@email.com", TicketType.STUDENT);

        assertThat(last.status()).isEqualTo(TicketStatus.PAID);
        assertThat(last.price()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(tickets).hasSize(3);
        assertThat(events.get(EVENT_CODE).getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        assertThat(eventService.findByCode(EVENT_CODE).status()).isEqualTo(EventStatus.SOLD_OUT);
    }

    // ---------- AC-009 ----------
    @Test
    void ac009_fourthPurchaseIsRejected() {
        createPublishAndAddArtists();
        buy("andrea@email.com", TicketType.VIP);
        buy("carlos@email.com", TicketType.GENERAL);
        buy("daniel@email.com", TicketType.GENERAL);

        assertThatThrownBy(() -> buy("andrea@email.com", TicketType.GENERAL))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(tickets).hasSize(3);
    }

    // ---------- AC-010 ----------
    @Test
    void ac010_paidTicketCanBecomeUsed() {
        createPublishAndAddArtists();
        TicketResponse paid = buy("andrea@email.com", TicketType.VIP);

        TicketResponse used = ticketService.markAsUsed(paid.ticketCode());

        assertThat(used.status()).isEqualTo(TicketStatus.USED);
    }

    // ---------- AC-011 ----------
    @Test
    void ac011_usedTicketCannotBeCancelled() {
        createPublishAndAddArtists();
        TicketResponse paid = buy("andrea@email.com", TicketType.VIP);
        ticketService.markAsUsed(paid.ticketCode());

        assertThatThrownBy(() -> ticketService.cancel(paid.ticketCode()))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(ticketService.findByCode(paid.ticketCode()).status()).isEqualTo(TicketStatus.USED);
    }

    // ---------- Reglas transversales del escenario ----------
    @Test
    void cancelledTicketNeverCanBeUsed() {
        createPublishAndAddArtists();
        TicketResponse paid = buy("carlos@email.com", TicketType.GENERAL);
        ticketService.cancel(paid.ticketCode());

        assertThatThrownBy(() -> ticketService.markAsUsed(paid.ticketCode()))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(ticketService.findByCode(paid.ticketCode()).status()).isEqualTo(TicketStatus.CANCELLED);
    }

    @Test
    void cancelledTicketFreesCapacityForTheCountOfPaidTickets() {
        createPublishAndAddArtists();
        TicketResponse first = buy("andrea@email.com", TicketType.GENERAL);
        buy("carlos@email.com", TicketType.GENERAL);
        ticketService.cancel(first.ticketCode());

        // Sólo hay 1 PAID, el evento sigue PUBLISHED y aún cabe gente: Daniel puede comprar
        TicketResponse next = buy("daniel@email.com", TicketType.GENERAL);

        assertThat(next.status()).isEqualTo(TicketStatus.PAID);
        assertThat(events.get(EVENT_CODE).getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void purchasedTicketCanBeFoundByCode() {
        createPublishAndAddArtists();
        buy("andrea@email.com", TicketType.VIP);

        // Las consultas por usuario y por evento se prueban en TicketServiceImplTest
        assertThat(tickets).hasSize(1);
        assertThat(ticketService.findByCode(tickets.get(0).getTicketCode()).userEmail())
                .isEqualTo("andrea@email.com");
    }
}
