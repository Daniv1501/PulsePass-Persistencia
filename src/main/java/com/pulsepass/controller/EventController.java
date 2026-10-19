package com.pulsepass.controller;

import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Endpoints de eventos (PRD Controladores, secciones 8.2 y 8.5).
 *
 * <pre>
 * POST  /api/events                                  → EventService.create            → 201
 * GET   /api/events/published                        → EventService.findPublishedEvents → 200
 * GET   /api/events/by-artist?stageName=...          → EventService.findByArtist      → 200
 * GET   /api/events/{eventCode}                      → EventService.findByCode        → 200 | 404
 * PATCH /api/events/{eventCode}/publish              → EventService.publish           → 200 | 404 | 409
 * POST  /api/events/{eventCode}/artists/{artistId}   → EventService.addArtist         → 200 | 404 | 409
 * GET   /api/events/{eventCode}/tickets/paid         → TicketService.findPaidTicketsByEvent → 200
 * </pre>
 *
 * El endpoint de tickets pagados vive aquí porque su recurso raíz es el evento
 * (sección 8.5 "TicketController / EventController"); solo delega en TicketService.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final TicketService ticketService;

    public EventController(EventService eventService, TicketService ticketService) {
        this.eventService = eventService;
        this.ticketService = ticketService;
    }

    // FR-CTRL-EVT-001
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        EventResponse created = eventService.create(request);
        URI location = UriComponentsBuilder.fromPath("/api/events/{eventCode}")
                .buildAndExpand(created.eventCode())
                .encode()
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // FR-CTRL-EVT-003
    @GetMapping("/published")
    public ResponseEntity<List<EventSummaryResponse>> findPublishedEvents() {
        return ResponseEntity.ok(eventService.findPublishedEvents());
    }

    // FR-CTRL-EVT-006
    @GetMapping("/by-artist")
    public ResponseEntity<List<EventSummaryResponse>> findByArtist(
            @RequestParam("stageName") @NotBlank(message = "Stage name is required") String stageName) {
        return ResponseEntity.ok(eventService.findByArtist(stageName));
    }

    // FR-CTRL-EVT-002
    @GetMapping("/{eventCode}")
    public ResponseEntity<EventResponse> findByCode(@PathVariable("eventCode") String eventCode) {
        return ResponseEntity.ok(eventService.findByCode(eventCode));
    }

    // FR-CTRL-EVT-004
    @PatchMapping("/{eventCode}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable("eventCode") String eventCode) {
        return ResponseEntity.ok(eventService.publish(eventCode));
    }

    // FR-CTRL-EVT-005
    @PostMapping("/{eventCode}/artists/{artistId}")
    public ResponseEntity<EventResponse> addArtist(@PathVariable("eventCode") String eventCode,
                                                   @PathVariable("artistId") Long artistId) {
        return ResponseEntity.ok(eventService.addArtist(eventCode, artistId));
    }

    // FR-CTRL-TKT-004
    @GetMapping("/{eventCode}/tickets/paid")
    public ResponseEntity<List<TicketResponse>> findPaidTickets(@PathVariable("eventCode") String eventCode) {
        return ResponseEntity.ok(ticketService.findPaidTicketsByEvent(eventCode));
    }
}
