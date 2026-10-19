package com.pulsepass.controller;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.service.VenueService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de venues (PRD Controladores, sección 8.1).
 *
 * <pre>
 * GET /api/venues/active   → VenueService.findActiveVenues()  → 200
 * GET /api/venues/{code}   → VenueService.findByCode(code)    → 200 | 404
 * </pre>
 */
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    // FR-CTRL-VEN-002
    @GetMapping("/active")
    public ResponseEntity<List<VenueResponse>> findActiveVenues() {
        return ResponseEntity.ok(venueService.findActiveVenues());
    }

    // FR-CTRL-VEN-001
    @GetMapping("/{code}")
    public ResponseEntity<VenueResponse> findByCode(@PathVariable("code") String code) {
        return ResponseEntity.ok(venueService.findByCode(code));
    }
}
