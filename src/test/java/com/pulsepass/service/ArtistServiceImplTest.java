package com.pulsepass.service;

import com.pulsepass.domain.Artist;
import com.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.ArtistMapper;
import com.pulsepass.repository.ArtistRepository;
import com.pulsepass.service.impl.ArtistServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    @Mock
    private ArtistRepository repository;

    @Mock
    private ArtistMapper mapper;

    @InjectMocks
    private ArtistServiceImpl service;

    private Artist artist() {
        return new Artist("Solar Beat", "Colombia", "Electronic");
    }

    private ArtistResponse response() {
        return new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
    }

    // ---------- FR-SVC-009: consultar por id ----------
    @Test
    void shouldFindArtistById() {
        // ARRANGE
        Artist artist = artist();
        when(repository.findById(1L)).thenReturn(Optional.of(artist));
        when(mapper.toResponse(artist)).thenReturn(response());

        // ACT
        ArtistResponse result = service.findById(1L);

        // ASSERT
        assertThat(result.stageName()).isEqualTo("Solar Beat");
    }

    // ---------- BR-ARTIST-001: artista inexistente (por id) ----------
    @Test
    void shouldThrowWhenArtistIdNotFound() {
        // ARRANGE
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------- FR-SVC-009: consultar por nombre artístico ----------
    @Test
    void shouldFindArtistByStageName() {
        // ARRANGE
        Artist artist = artist();
        when(repository.findByStageNameIgnoreCase("solar beat")).thenReturn(Optional.of(artist));
        when(mapper.toResponse(artist)).thenReturn(response());

        // ACT
        ArtistResponse result = service.findByStageName("solar beat");

        // ASSERT
        assertThat(result.stageName()).isEqualTo("Solar Beat");
    }

    // ---------- BR-ARTIST-001: artista inexistente (por nombre) ----------
    @Test
    void shouldThrowWhenStageNameNotFound() {
        // ARRANGE
        when(repository.findByStageNameIgnoreCase("Nadie")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> service.findByStageName("Nadie"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nadie");
    }

    // ---------- BR-ARTIST-002: solo artistas activos ----------
    @Test
    void shouldReturnOnlyActiveArtists() {
        // ARRANGE
        Artist artist = artist();
        when(repository.findByActiveTrueOrderByStageNameAsc()).thenReturn(List.of(artist));
        when(mapper.toResponse(artist)).thenReturn(response());

        // ACT
        List<ArtistResponse> result = service.findActiveArtists();

        // ASSERT
        assertThat(result).extracting(ArtistResponse::active).containsOnly(true);
        verify(repository).findByActiveTrueOrderByStageNameAsc();
    }
}
