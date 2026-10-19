package com.pulsepass.controller;

import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.service.ArtistService;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de artistas (PRD Controladores, sección 8.3).
 *
 * <pre>
 * GET /api/artists/active                       → ArtistService.findActiveArtists() → 200
 * GET /api/artists/by-stage-name?stageName=...  → ArtistService.findByStageName()   → 200 | 404
 * GET /api/artists/{id}                         → ArtistService.findById()          → 200 | 404
 * </pre>
 */
@RestController
@RequestMapping("/api/artists")
public class ArtistController {

    private final ArtistService artistService;

    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }

    // FR-CTRL-ART-003
    @GetMapping("/active")
    public ResponseEntity<List<ArtistResponse>> findActiveArtists() {
        return ResponseEntity.ok(artistService.findActiveArtists());
    }

    // FR-CTRL-ART-002
    @GetMapping("/by-stage-name")
    public ResponseEntity<ArtistResponse> findByStageName(
            @RequestParam("stageName") @NotBlank(message = "Stage name is required") String stageName) {
        return ResponseEntity.ok(artistService.findByStageName(stageName));
    }

    // FR-CTRL-ART-001
    @GetMapping("/{id}")
    public ResponseEntity<ArtistResponse> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(artistService.findById(id));
    }
}
