package com.pulsepass.service;

import com.pulsepass.domain.Artist;
import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Venue;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.EventMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.impl.EventServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private EventMapper mapper;

    @InjectMocks
    private EventServiceImpl service;

    private Venue activeVenue() {
        return new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 3000);
    }

    private CreateEventRequest validRequest() {
        return new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, LocalDateTime.now().plusMonths(1), 18, "VEN-SMR-01");
    }

    // ---------- TEST-EVENT-001: evento existente -> DTO ----------
    @Test
    void shouldFindEventByCode() {
        Venue venue = activeVenue();
        Event event = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusMonths(1), 18);
        venue.addEvent(event);

        EventResponse response = new EventResponse(1L, "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, event.getEventDate(), 18,
                "VEN-SMR-01", "Marina Convention Center", List.of());

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(mapper.toResponse(event)).thenReturn(response);

        EventResponse result = service.findByCode("CMF-2026");

        assertThat(result).isEqualTo(response);
    }

    // ---------- TEST-EVENT-002: evento inexistente ----------
    @Test
    void shouldThrowWhenEventNotFound() {
        when(eventRepository.findByEventCode("CMF-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("CMF-9999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------- TEST-EVENT-003: crear evento válido -> save() ----------
    @Test
    void shouldCreateEventWhenRequestIsValid() {
        Venue venue = activeVenue();
        EventResponse response = new EventResponse(1L, "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusMonths(1), 18,
                "VEN-SMR-01", "Marina Convention Center", List.of());

        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Event.class))).thenReturn(response);

        EventResponse result = service.create(validRequest());

        assertThat(result).isEqualTo(response);
        verify(eventRepository).save(any(Event.class));
    }

    // ---------- TEST-EVENT-004: venue inexistente -> no save() ----------
    @Test
    void shouldThrowWhenVenueNotFound() {
        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository, never()).save(any());
    }

    // ---------- TEST-EVENT-005: venue inactivo ----------
    @Test
    void shouldThrowWhenVenueIsInactive() {
        Venue inactiveVenue = activeVenue();
        inactiveVenue.setActive(false);

        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(inactiveVenue));

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(BusinessRuleException.class);

        verify(eventRepository, never()).save(any());
    }

    // ---------- TEST-EVENT-006: fecha pasada ----------
    @Test
    void shouldThrowWhenEventDateIsInThePast() {
        Venue venue = activeVenue();
        CreateEventRequest pastRequest = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, LocalDateTime.now().minusDays(1), 18, "VEN-SMR-01");

        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));

        assertThatThrownBy(() -> service.create(pastRequest))
                .isInstanceOf(BusinessRuleException.class);

        verify(eventRepository, never()).save(any());
    }

    // ---------- TEST-EVENT-007: publicar DRAFT válido -> PUBLISHED ----------
    @Test
    void shouldPublishValidDraftEvent() {
        Venue venue = activeVenue();
        Event event = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusMonths(1), 18);
        venue.addEvent(event);

        EventResponse response = new EventResponse(1L, "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, event.getEventDate(), 18,
                "VEN-SMR-01", "Marina Convention Center", List.of());

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(mapper.toResponse(event)).thenReturn(response);

        EventResponse result = service.publish("CMF-2026");

        assertThat(result.status()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    // ---------- TEST-EVENT-008: publicar CANCELLED -> error, no persistir ----------
    @Test
    void shouldThrowWhenPublishingCancelledEvent() {
        Venue venue = activeVenue();
        Event event = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.CANCELLED, LocalDateTime.now().plusMonths(1), 18);
        venue.addEvent(event);

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish("CMF-2026"))
                .isInstanceOf(BusinessRuleException.class);

        verify(eventRepository, never()).save(any());
    }

    // =====================================================================
    // Pruebas adicionales: reglas BR-EVENT-001..011 completas
    // =====================================================================

    private Event eventWithStatus(Venue venue, EventStatus status) {
        Event event = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, status, LocalDateTime.now().plusMonths(1), 18);
        venue.addEvent(event);
        return event;
    }

    private EventResponse responseFor(Event event) {
        return new EventResponse(1L, event.getEventCode(), event.getName(), event.getDescription(),
                event.getCategory(), event.getStatus(), event.getEventDate(), event.getMinimumAge(),
                "VEN-SMR-01", "Marina Convention Center", List.of());
    }

    // ---------- BR-EVENT-001: código único ----------
    @Test
    void shouldThrowWhenEventCodeAlreadyExists() {
        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(true);

        assertThatThrownBy(() -> service.create(validRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CMF-2026");

        verify(eventRepository, never()).save(any());
        verifyNoInteractions(venueRepository);
    }

    // ---------- BR-EVENT-005: estado inicial siempre DRAFT ----------
    @Test
    void shouldAlwaysCreateEventInDraftStatus() {
        Venue venue = activeVenue();

        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Event.class))).thenAnswer(inv -> responseFor(inv.getArgument(0)));

        EventResponse result = service.create(validRequest());

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(captor.getValue().getVenue()).isSameAs(venue);
        assertThat(captor.getValue().getEventCode()).isEqualTo("CMF-2026");
        assertThat(result.status()).isEqualTo(EventStatus.DRAFT);
    }

    // ---------- BR-EVENT-006: minimumAge >= 0 ----------
    @Test
    void shouldThrowWhenMinimumAgeIsNegative() {
        CreateEventRequest request = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, LocalDateTime.now().plusMonths(1), -1, "VEN-SMR-01");

        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(activeVenue()));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldAllowMinimumAgeZeroMeaningNoRestriction() {
        CreateEventRequest request = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, LocalDateTime.now().plusMonths(1), 0, "VEN-SMR-01");

        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(activeVenue()));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Event.class))).thenAnswer(inv -> responseFor(inv.getArgument(0)));

        EventResponse result = service.create(request);

        assertThat(result.minimumAge()).isZero();
        verify(eventRepository).save(any(Event.class));
    }

    // ---------- BR-EVENT-004: fecha nula tampoco es válida ----------
    @Test
    void shouldThrowWhenEventDateIsNull() {
        CreateEventRequest request = new CreateEventRequest(
                "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, null, 18, "VEN-SMR-01");

        when(eventRepository.existsByEventCode("CMF-2026")).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(activeVenue()));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(eventRepository, never()).save(any());
    }

    // ---------- BR-EVENT-007: solo DRAFT se publica ----------
    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"PUBLISHED", "SOLD_OUT", "CANCELLED", "FINISHED"})
    void shouldThrowWhenPublishingNonDraftEvent(EventStatus status) {
        Event event = eventWithStatus(activeVenue(), status);

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish("CMF-2026"))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(event.getStatus()).isEqualTo(status);
        verify(eventRepository, never()).save(any());
    }

    // ---------- BR-EVENT-008: la fecha debe seguir siendo futura ----------
    @Test
    void shouldThrowWhenPublishingEventWithPastDate() {
        Event event = eventWithStatus(activeVenue(), EventStatus.DRAFT);
        event.setEventDate(LocalDateTime.now().minusDays(1));

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish("CMF-2026"))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        verify(eventRepository, never()).save(any());
    }

    // ---------- BR-EVENT-009: el venue debe seguir activo ----------
    @Test
    void shouldThrowWhenPublishingEventWhoseVenueBecameInactive() {
        Venue venue = activeVenue();
        Event event = eventWithStatus(venue, EventStatus.DRAFT);
        venue.setActive(false);

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish("CMF-2026"))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPublishingUnknownEvent() {
        when(eventRepository.findByEventCode("CMF-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish("CMF-9999"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(eventRepository, never()).save(any());
    }

    // ---------- FR-SVC-005: eventos publicados ----------
    @Test
    void shouldReturnPublishedEventsAsSummaries() {
        Event event = eventWithStatus(activeVenue(), EventStatus.PUBLISHED);
        EventSummaryResponse summary = new EventSummaryResponse(1L, "CMF-2026", "Caribbean Music Fest",
                EventCategory.MUSIC, EventStatus.PUBLISHED, event.getEventDate(), "Marina Convention Center");

        when(eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)).thenReturn(List.of(event));
        when(mapper.toSummary(event)).thenReturn(summary);

        List<EventSummaryResponse> result = service.findPublishedEvents();

        assertThat(result).containsExactly(summary);
        verify(eventRepository).findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);
    }

    // ---------- FR-SVC-008: eventos por artista ----------
    @Test
    void shouldReturnEventsByArtistStageName() {
        Event event = eventWithStatus(activeVenue(), EventStatus.PUBLISHED);
        EventSummaryResponse summary = new EventSummaryResponse(1L, "CMF-2026", "Caribbean Music Fest",
                EventCategory.MUSIC, EventStatus.PUBLISHED, event.getEventDate(), "Marina Convention Center");

        when(eventRepository.findByArtistStageName("Solar Beat")).thenReturn(List.of(event));
        when(mapper.toSummary(event)).thenReturn(summary);

        List<EventSummaryResponse> result = service.findByArtist("Solar Beat");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).eventCode()).isEqualTo("CMF-2026");
    }

    // ---------- FR-SVC-007 / BR-EVENT-010..011: asociar artistas ----------
    @Test
    void shouldAddArtistToEvent() {
        Event event = eventWithStatus(activeVenue(), EventStatus.DRAFT);
        Artist artist = new Artist("Solar Beat", "Colombia", "Electronic");
        EventResponse response = new EventResponse(1L, "CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, event.getEventDate(), 18,
                "VEN-SMR-01", "Marina Convention Center", List.of("Solar Beat"));

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(eventRepository.save(event)).thenReturn(event);
        when(mapper.toResponse(event)).thenReturn(response);

        EventResponse result = service.addArtist("CMF-2026", 1L);

        assertThat(event.getArtists()).containsExactly(artist);
        assertThat(result.artists()).containsExactly("Solar Beat");
        verify(eventRepository).save(event);
    }

    @Test
    void shouldAllowAddingThreeDifferentArtists() {
        Event event = eventWithStatus(activeVenue(), EventStatus.DRAFT);
        Artist a1 = new Artist("Solar Beat", "Colombia", "Electronic");
        Artist a2 = new Artist("Neon Waves", "Colombia", "Synthwave");
        Artist a3 = new Artist("Caribbean Sound", "Colombia", "Caribbean");

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(a1));
        when(artistRepository.findById(2L)).thenReturn(Optional.of(a2));
        when(artistRepository.findById(3L)).thenReturn(Optional.of(a3));
        when(eventRepository.save(event)).thenReturn(event);
        when(mapper.toResponse(event)).thenReturn(responseFor(event));

        service.addArtist("CMF-2026", 1L);
        service.addArtist("CMF-2026", 2L);
        service.addArtist("CMF-2026", 3L);

        assertThat(event.getArtists()).containsExactlyInAnyOrder(a1, a2, a3);
    }

    // ---------- BR-EVENT-010: no duplicar artista ----------
    @Test
    void shouldThrowWhenArtistIsAlreadyAssociated() {
        Event event = eventWithStatus(activeVenue(), EventStatus.DRAFT);
        Artist artist = new Artist("Solar Beat", "Colombia", "Electronic");
        event.addArtist(artist);

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> service.addArtist("CMF-2026", 1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Solar Beat");

        assertThat(event.getArtists()).hasSize(1);
        verify(eventRepository, never()).save(any());
    }

    // ---------- BR-EVENT-011: no agregar a CANCELLED / FINISHED ----------
    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"CANCELLED", "FINISHED"})
    void shouldThrowWhenAddingArtistToClosedEvent(EventStatus status) {
        Event event = eventWithStatus(activeVenue(), status);
        Artist artist = new Artist("Solar Beat", "Colombia", "Electronic");

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        assertThatThrownBy(() -> service.addArtist("CMF-2026", 1L))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(event.getArtists()).isEmpty();
        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAddingArtistToUnknownEvent() {
        when(eventRepository.findByEventCode("CMF-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addArtist("CMF-9999", 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(artistRepository);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAddingUnknownArtist() {
        Event event = eventWithStatus(activeVenue(), EventStatus.DRAFT);

        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addArtist("CMF-2026", 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(eventRepository, never()).save(any());
    }
}
