package com.pulsepass.service;

import com.pulsepass.domain.Venue;
import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.VenueMapper;
import com.pulsepass.repository.VenueRepository;
import com.pulsepass.service.impl.VenueServiceImpl;

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
class VenueServiceImplTest {

    @Mock
    private VenueRepository repository;

    @Mock
    private VenueMapper mapper;

    @InjectMocks
    private VenueServiceImpl service;

    private Venue venue() {
        return new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", 3);
    }

    private VenueResponse response() {
        return new VenueResponse(1L, "VEN-SMR-01", "Marina Convention Center",
                "Santa Marta", "Cra 1 #2-3", 3, true);
    }

    // ---------- FR-SVC-001: venue existente -> DTO ----------
    @Test
    void shouldFindVenueByCode() {
        // ARRANGE
        Venue venue = venue();
        when(repository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));
        when(mapper.toResponse(venue)).thenReturn(response());

        // ACT
        VenueResponse result = service.findByCode("VEN-SMR-01");

        // ASSERT
        assertThat(result.code()).isEqualTo("VEN-SMR-01");
        assertThat(result.capacity()).isEqualTo(3);
    }

    // ---------- BR-VENUE-001: venue inexistente ----------
    @Test
    void shouldThrowWhenVenueNotFound() {
        // ARRANGE
        when(repository.findByCode("VEN-XXX")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> service.findByCode("VEN-XXX"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("VEN-XXX");
    }

    // ---------- BR-VENUE-002: solo venues activos ----------
    @Test
    void shouldReturnOnlyActiveVenues() {
        // ARRANGE
        Venue venue = venue();
        when(repository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(venue));
        when(mapper.toResponse(venue)).thenReturn(response());

        // ACT
        List<VenueResponse> result = service.findActiveVenues();

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).active()).isTrue();
        verify(repository).findByActiveTrueOrderByNameAsc();
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoActiveVenues() {
        // ARRANGE
        when(repository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of());

        // ACT
        List<VenueResponse> result = service.findActiveVenues();

        // ASSERT
        assertThat(result).isEmpty();
    }
}
