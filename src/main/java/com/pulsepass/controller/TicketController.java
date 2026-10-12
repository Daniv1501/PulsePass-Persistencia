package com.pulsepass.controller;

import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.service.TicketService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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
 * Endpoints de tickets (PRD Controladores, sección 8.5).
 *
 * <pre>
 * POST  /api/tickets                       → TicketService.purchase()        → 201 | 404 | 409
 * GET   /api/tickets/by-user?email=...     → TicketService.findByUserEmail() → 200
 * GET   /api/tickets/{ticketCode}          → TicketService.findByCode()      → 200 | 404
 * PATCH /api/tickets/{ticketCode}/cancel   → TicketService.cancel()          → 200 | 404 | 409
 * PATCH /api/tickets/{ticketCode}/use      → TicketService.markAsUsed()      → 200 | 404 | 409
 * </pre>
 *
 * Los tickets PAID de un evento se exponen en EventController:
 * GET /api/events/{eventCode}/tickets/paid.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // FR-CTRL-TKT-001
    @PostMapping
    public ResponseEntity<TicketResponse> purchase(@Valid @RequestBody PurchaseTicketRequest request) {
        TicketResponse created = ticketService.purchase(request);
        URI location = UriComponentsBuilder.fromPath("/api/tickets/{ticketCode}")
                .buildAndExpand(created.ticketCode())
                .encode()
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // FR-CTRL-TKT-003
    @GetMapping("/by-user")
    public ResponseEntity<List<TicketResponse>> findByUserEmail(
            @RequestParam("email")
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be a valid email address") String email) {
        return ResponseEntity.ok(ticketService.findByUserEmail(email));
    }

    // FR-CTRL-TKT-002
    @GetMapping("/{ticketCode}")
    public ResponseEntity<TicketResponse> findByCode(@PathVariable("ticketCode") String ticketCode) {
        return ResponseEntity.ok(ticketService.findByCode(ticketCode));
    }

    // FR-CTRL-TKT-005
    @PatchMapping("/{ticketCode}/cancel")
    public ResponseEntity<TicketResponse> cancel(@PathVariable("ticketCode") String ticketCode) {
        return ResponseEntity.ok(ticketService.cancel(ticketCode));
    }

    // FR-CTRL-TKT-006
    @PatchMapping("/{ticketCode}/use")
    public ResponseEntity<TicketResponse> markAsUsed(@PathVariable("ticketCode") String ticketCode) {
        return ResponseEntity.ok(ticketService.markAsUsed(ticketCode));
    }
}
