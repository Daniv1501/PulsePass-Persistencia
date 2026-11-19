package com.pulsepass.controller;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.service.VenueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {
    private final VenueService service;
    public VenueController(VenueService service) { this.service=service; }
    @GetMapping("/active")
    public ResponseEntity<List<VenueResponse>> active() { return ResponseEntity.ok(service.findActiveVenues()); }
    @GetMapping("/{code}")
    public ResponseEntity<VenueResponse> find(@PathVariable String code) { return ResponseEntity.ok(service.findByCode(code)); }
}
