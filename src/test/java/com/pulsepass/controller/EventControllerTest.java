package com.pulsepass.controller;

import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de EventController (PRD Controladores, sección 22.2).
 * EventService y TicketService son mocks; no se ejecuta lógica de negocio real.
 */
@WebMvcTest(EventController.class)
@Import(ControllerTestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
class EventControllerTest {

    private static final LocalDateTime EVENT_DATE = LocalDateTime.of(2026, 12, 5, 20, 0);

    private static final String VALID_BODY = """
            {
              "eventCode": "CMF-2026",
              "name": "Caribbean Music Fest 2026",
              "description": "Festival de música del Caribe",
              "category": "MUSIC",
              "eventDate": "2026-12-05T20:00:00",
              "minimumAge": 18,
              "venueCode": "VEN-SMR-01"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private TicketService ticketService;

    private CreateEventRequest validRequest() {
        return new CreateEventRequest("CMF-2026", "Caribbean Music Fest 2026",
                "Festival de música del Caribe", EventCategory.MUSIC, EVENT_DATE, 18, "VEN-SMR-01");
    }

    private EventResponse cmf(EventStatus status, List<String> artists) {
        return new EventResponse(10L, "CMF-2026", "Caribbean Music Fest 2026",
                "Festival de música del Caribe", EventCategory.MUSIC, status, EVENT_DATE, 18,
                "VEN-SMR-01", "Marina Convention Center", artists);
    }

    private EventSummaryResponse cmfSummary() {
        return new EventSummaryResponse(10L, "CMF-2026", "Caribbean Music Fest 2026",
                EventCategory.MUSIC, EventStatus.PUBLISHED, EVENT_DATE, "Marina Convention Center");
    }

    // TEST-CTRL-EVT-001 / AC-CTRL-003
    @Test
    void shouldReturn201WhenCreatingValidEvent() throws Exception {
        when(eventService.create(validRequest())).thenReturn(cmf(EventStatus.DRAFT, List.of()));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", endsWith("/api/events/CMF-2026")))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.category").value("MUSIC"))
                .andExpect(jsonPath("$.minimumAge").value(18))
                .andExpect(jsonPath("$.venueCode").value("VEN-SMR-01"));

        verify(eventService).create(validRequest());
    }

    // TEST-CTRL-EVT-002 / AC-CTRL-004
    @Test
    void shouldReturn400WithInvalidFieldsWhenRequestIsInvalid() throws Exception {
        String body = """
                {
                  "eventCode": "",
                  "name": "Caribbean Music Fest 2026",
                  "category": "MUSIC",
                  "eventDate": "2026-12-05T20:00:00",
                  "minimumAge": -1,
                  "venueCode": " "
                }
                """;

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.venueCode").value("Venue code is required"))
                .andExpect(jsonPath("$.details.minimumAge").value("Minimum age cannot be negative"));

        verify(eventService, never()).create(any());
    }

    @Test
    void shouldReturn400WhenRequiredEventFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.eventCode").exists())
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.category").value("Category is required"))
                .andExpect(jsonPath("$.details.eventDate").value("Event date is required"))
                .andExpect(jsonPath("$.details.minimumAge").value("Minimum age is required"))
                .andExpect(jsonPath("$.details.venueCode").exists());

        verify(eventService, never()).create(any());
    }

    @Test
    void shouldReturn400WhenDescriptionExceedsMaximumLength() throws Exception {
        String body = VALID_BODY.replace("Festival de música del Caribe", "x".repeat(2001));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.description").value("Description must have at most 2000 characters"));

        verify(eventService, never()).create(any());
    }

    @Test
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{\"eventCode\": "))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        verifyNoInteractions(eventService);
    }

    @Test
    void shouldReturn400WhenCategoryIsNotAValidEnum() throws Exception {
        String body = VALID_BODY.replace("\"MUSIC\"", "\"OPERA\"");

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        verifyNoInteractions(eventService);
    }

    @Test
    void shouldReturn409WhenEventCodeIsDuplicated() throws Exception {
        when(eventService.create(validRequest()))
                .thenThrow(new DuplicateResourceException("Event code already exists: CMF-2026"));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Event code already exists: CMF-2026"));
    }

    @Test
    void shouldReturn404WhenCreatingEventInUnknownVenue() throws Exception {
        when(eventService.create(validRequest()))
                .thenThrow(new ResourceNotFoundException("Venue not found: VEN-SMR-01"));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Venue not found: VEN-SMR-01"));
    }

    // TEST-CTRL-EVT-003
    @Test
    void shouldReturn200WhenEventExists() throws Exception {
        when(eventService.findByCode("CMF-2026")).thenReturn(cmf(EventStatus.DRAFT, List.of()));

        mockMvc.perform(get("/api/events/{eventCode}", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.name").value("Caribbean Music Fest 2026"))
                .andExpect(jsonPath("$.eventDate").value("2026-12-05T20:00:00"));

        verify(eventService).findByCode("CMF-2026");
    }

    // TEST-CTRL-EVT-004
    @Test
    void shouldReturn404WhenEventDoesNotExist() throws Exception {
        when(eventService.findByCode("NOPE-2026"))
                .thenThrow(new ResourceNotFoundException("Event not found: NOPE-2026"));

        mockMvc.perform(get("/api/events/{eventCode}", "NOPE-2026"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event not found: NOPE-2026"))
                .andExpect(jsonPath("$.details").isEmpty());
    }

    // TEST-CTRL-EVT-005
    @Test
    void shouldReturn200WithPublishedEvents() throws Exception {
        when(eventService.findPublishedEvents()).thenReturn(List.of(cmfSummary()));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].venueName").value("Marina Convention Center"));

        verify(eventService).findPublishedEvents();
        verify(eventService, never()).findByCode(anyString());
    }

    // TEST-CTRL-EVT-006 / AC-CTRL-005
    @Test
    void shouldReturn200WhenPublishingDraftEvent() throws Exception {
        when(eventService.publish("CMF-2026")).thenReturn(cmf(EventStatus.PUBLISHED, List.of()));

        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish("CMF-2026");
    }

    // TEST-CTRL-EVT-007 / AC-CTRL-006
    @Test
    void shouldReturn409WhenPublishingIsNotAllowed() throws Exception {
        when(eventService.publish("CMF-2026")).thenThrow(new BusinessRuleException(
                "Only DRAFT events can be published. Current status: PUBLISHED"));

        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Only DRAFT events can be published. Current status: PUBLISHED"));

        verify(eventService).publish("CMF-2026");
    }

    // TEST-CTRL-EVT-008
    @Test
    void shouldReturn200WhenAddingArtist() throws Exception {
        when(eventService.addArtist("CMF-2026", 1L))
                .thenReturn(cmf(EventStatus.DRAFT, List.of("Solar Beat")));

        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.artists", hasSize(1)))
                .andExpect(jsonPath("$.artists[0]").value("Solar Beat"));

        verify(eventService).addArtist("CMF-2026", 1L);
    }

    @Test
    void shouldReturn409WhenArtistIsAlreadyAssociated() throws Exception {
        when(eventService.addArtist("CMF-2026", 1L)).thenThrow(new BusinessRuleException(
                "Artist Solar Beat is already associated with this event"));

        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Artist Solar Beat is already associated with this event"));
    }

    @Test
    void shouldReturn400WhenArtistIdIsNotNumeric() throws Exception {
        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.artistId").exists());

        verify(eventService, never()).addArtist(anyString(), anyLong());
    }

    // TEST-CTRL-EVT-009
    @Test
    void shouldReturn200WhenSearchingByArtist() throws Exception {
        when(eventService.findByArtist("Solar Beat")).thenReturn(List.of(cmfSummary()));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(eventService).findByArtist("Solar Beat");
    }

    @Test
    void shouldReturn400WhenStageNameIsBlank() throws Exception {
        mockMvc.perform(get("/api/events/by-artist").param("stageName", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.stageName").value("Stage name is required"));

        verify(eventService, never()).findByArtist(anyString());
    }

    // Error inesperado → 500 sin exponer detalles internos (NFR-CTRL-007)
    @Test
    void shouldReturn500WithoutInternalDetailsOnUnexpectedError() throws Exception {
        when(eventService.findPublishedEvents())
                .thenThrow(new IllegalStateException("connection pool exhausted at db-01:5432"));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(content().string(not(containsString("db-01"))));
    }
}
