package com.pulsepass.controller;

import com.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.dto.response.*;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;
import com.pulsepass.security.SecuredEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService events;
    private final TicketService tickets;
    private final SecuredEventService catalog;
    public EventController(EventService events, TicketService tickets, SecuredEventService catalog) {
        this.events=events; this.tickets=tickets; this.catalog=catalog;
    }
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(events.create(request));
    }
    @GetMapping("/published")
    public ResponseEntity<List<EventSummaryResponse>> published() { return ResponseEntity.ok(events.findPublishedEvents()); }
    @GetMapping("/by-artist")
    public ResponseEntity<List<EventSummaryResponse>> byArtist(@RequestParam @NotBlank String stageName) {
        return ResponseEntity.ok(catalog.findByArtist(stageName));
    }
    @GetMapping("/{eventCode}")
    public ResponseEntity<EventResponse> find(@PathVariable String eventCode) { return ResponseEntity.ok(catalog.findByCode(eventCode)); }
    @PatchMapping("/{eventCode}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable String eventCode) { return ResponseEntity.ok(events.publish(eventCode)); }
    @PostMapping("/{eventCode}/artists/{artistId}")
    public ResponseEntity<EventResponse> associate(@PathVariable String eventCode, @PathVariable Long artistId) {
        return ResponseEntity.ok(events.addArtist(eventCode, artistId));
    }
    @GetMapping("/{eventCode}/tickets/paid")
    public ResponseEntity<List<TicketResponse>> paid(@PathVariable String eventCode) {
        return ResponseEntity.ok(tickets.findPaidTicketsByEvent(eventCode));
    }
}
