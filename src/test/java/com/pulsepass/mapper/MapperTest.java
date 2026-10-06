package com.pulsepass.mapper;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.domain.Venue;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.dto.response.VenueResponse;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica (sin Spring) las implementaciones que MapStruct genera en compilación.
 * Cubre NFR-004 y las secciones 33 y 34 del PRD.
 */
class MapperTest {

    private final LocalDateTime eventDate = LocalDateTime.now().plusMonths(1);

    private Venue venue() {
        return new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 3);
    }

    private Event event(Venue venue) {
        Event event = new Event("CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, eventDate, 18);
        venue.addEvent(event);
        return event;
    }

    @Test
    void venueMapperMapsAllFields() {
        VenueResponse response = new VenueMapperImpl().toResponse(venue());

        assertThat(response.code()).isEqualTo("VEN-SMR-01");
        assertThat(response.name()).isEqualTo("Marina Convention Center");
        assertThat(response.city()).isEqualTo("Santa Marta");
        assertThat(response.address()).isEqualTo("Cra 1 #2-3");
        assertThat(response.capacity()).isEqualTo(3);
        assertThat(response.active()).isTrue();
    }

    @Test
    void artistMapperMapsAllFields() {
        ArtistResponse response = new ArtistMapperImpl()
                .toResponse(new Artist("Solar Beat", "Colombia", "Electronic"));

        assertThat(response.stageName()).isEqualTo("Solar Beat");
        assertThat(response.country()).isEqualTo("Colombia");
        assertThat(response.genre()).isEqualTo("Electronic");
        assertThat(response.active()).isTrue();
    }

    @Test
    void eventMapperFlattensVenueAndSortsArtistNames() {
        Event event = event(venue());
        event.addArtist(new Artist("Solar Beat", "Colombia", "Electronic"));
        event.addArtist(new Artist("Neon Waves", "Colombia", "Synthwave"));
        event.addArtist(new Artist("Caribbean Sound", "Colombia", "Caribbean"));

        EventResponse response = new EventMapperImpl().toResponse(event);

        assertThat(response.eventCode()).isEqualTo("CMF-2026");
        assertThat(response.status()).isEqualTo(EventStatus.DRAFT);
        assertThat(response.category()).isEqualTo(EventCategory.MUSIC);
        assertThat(response.minimumAge()).isEqualTo(18);
        assertThat(response.eventDate()).isEqualTo(eventDate);
        assertThat(response.venueCode()).isEqualTo("VEN-SMR-01");
        assertThat(response.venueName()).isEqualTo("Marina Convention Center");
        assertThat(response.artists()).containsExactly("Caribbean Sound", "Neon Waves", "Solar Beat");
    }

    @Test
    void eventMapperProducesSummary() {
        EventSummaryResponse summary = new EventMapperImpl().toSummary(event(venue()));

        assertThat(summary.eventCode()).isEqualTo("CMF-2026");
        assertThat(summary.name()).isEqualTo("Caribbean Music Fest 2026");
        assertThat(summary.venueName()).isEqualTo("Marina Convention Center");
        assertThat(summary.status()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void userMapperFlattensProfile() {
        User user = new User("andrea", "andrea@email.com");
        user.assignProfile(new UserProfile("Andrea", "Gomez", "300", "Santa Marta", LocalDate.of(2000, 1, 1)));

        UserResponse response = new UserMapperImpl().toResponse(user);

        assertThat(response.username()).isEqualTo("andrea");
        assertThat(response.email()).isEqualTo("andrea@email.com");
        assertThat(response.active()).isTrue();
        assertThat(response.firstName()).isEqualTo("Andrea");
        assertThat(response.lastName()).isEqualTo("Gomez");
        assertThat(response.phone()).isEqualTo("300");
        assertThat(response.city()).isEqualTo("Santa Marta");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    // Sección 34: ticket.user.email -> userEmail, ticket.event.eventCode -> eventCode, ticket.event.name -> eventName
    @Test
    void ticketMapperFlattensUserAndEvent() {
        User user = new User("andrea", "andrea@email.com");
        Event event = event(venue());
        LocalDateTime purchase = LocalDateTime.now();
        Ticket ticket = new Ticket("TCK-ABC", TicketType.VIP, new BigDecimal("200.00"),
                TicketStatus.PAID, purchase, user, event);

        TicketResponse response = new TicketMapperImpl().toResponse(ticket);

        assertThat(response.ticketCode()).isEqualTo("TCK-ABC");
        assertThat(response.type()).isEqualTo(TicketType.VIP);
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(response.status()).isEqualTo(TicketStatus.PAID);
        assertThat(response.purchaseDate()).isEqualTo(purchase);
        assertThat(response.userEmail()).isEqualTo("andrea@email.com");
        assertThat(response.eventCode()).isEqualTo("CMF-2026");
        assertThat(response.eventName()).isEqualTo("Caribbean Music Fest 2026");
    }
}
