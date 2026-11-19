package com.pulsepass.controller;

import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;
    public TicketController(TicketService service) { this.service=service; }
    @PostMapping
    @PreAuthorize("hasRole('USER') and @authorizationService.ownsEmail(#request.userEmail())")
    public ResponseEntity<TicketResponse> purchase(@Valid @RequestBody PurchaseTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.purchase(request));
    }
    @GetMapping("/by-user")
    @PreAuthorize("@authorizationService.canReadUserEmail(#email)")
    public ResponseEntity<List<TicketResponse>> byUser(@RequestParam @NotBlank @Email String email) {
        return ResponseEntity.ok(service.findByUserEmail(email));
    }
    @GetMapping("/{ticketCode}")
    @PreAuthorize("@authorizationService.canReadTicket(#ticketCode)")
    public ResponseEntity<TicketResponse> find(@PathVariable String ticketCode) {
        return ResponseEntity.ok(service.findByCode(ticketCode));
    }
    @PatchMapping("/{ticketCode}/cancel")
    @PreAuthorize("@authorizationService.canCancelTicket(#ticketCode)")
    public ResponseEntity<TicketResponse> cancel(@PathVariable String ticketCode) {
        return ResponseEntity.ok(service.cancel(ticketCode));
    }
    @PatchMapping("/{ticketCode}/use")
    public ResponseEntity<TicketResponse> use(@PathVariable String ticketCode) {
        return ResponseEntity.ok(service.markAsUsed(ticketCode));
    }
}
