package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.service.ArtistService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/artists")
public class ArtistController {
    private final ArtistService service;
    public ArtistController(ArtistService service) { this.service=service; }
    @GetMapping("/active")
    public ResponseEntity<List<ArtistResponse>> active() { return ResponseEntity.ok(service.findActiveArtists()); }
    @GetMapping("/by-stage-name")
    public ResponseEntity<ArtistResponse> byName(@RequestParam @NotBlank String stageName) {
        return ResponseEntity.ok(service.findByStageName(stageName));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> find(@PathVariable Long id) { return ResponseEntity.ok(service.findById(id)); }
}
