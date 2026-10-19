package com.pulsepass.controller;

import com.pulsepass.dto.response.VenueResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.VenueService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.context.annotation.Import;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de VenueController (PRD Controladores, sección 22.1).
 * Solo se carga la capa web; VenueService es un mock.
 */
@WebMvcTest(VenueController.class)
@Import(ControllerTestSecurityConfig.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    private VenueResponse marina() {
        return new VenueResponse(1L, "VEN-SMR-01", "Marina Convention Center",
                "Santa Marta", "Cra 1 #2-3", 3, true);
    }

    // TEST-CTRL-VEN-001 / AC-CTRL-001
    @Test
    void shouldReturn200WhenVenueExists() throws Exception {
        when(venueService.findByCode("VEN-SMR-01")).thenReturn(marina());

        mockMvc.perform(get("/api/venues/{code}", "VEN-SMR-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name").value("Marina Convention Center"))
                .andExpect(jsonPath("$.city").value("Santa Marta"))
                .andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.active").value(true));

        verify(venueService).findByCode("VEN-SMR-01");
    }

    // TEST-CTRL-VEN-002 / AC-CTRL-002
    @Test
    void shouldReturn404WithErrorResponseWhenVenueDoesNotExist() throws Exception {
        when(venueService.findByCode("VEN-XXX"))
                .thenThrow(new ResourceNotFoundException("Venue not found: VEN-XXX"));

        mockMvc.perform(get("/api/venues/{code}", "VEN-XXX"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Venue not found: VEN-XXX"))
                .andExpect(jsonPath("$.details").isEmpty());

        verify(venueService).findByCode("VEN-XXX");
    }

    // TEST-CTRL-VEN-003
    @Test
    void shouldReturn200WithActiveVenues() throws Exception {
        VenueResponse other = new VenueResponse(2L, "VEN-BAQ-01", "Puerta de Oro",
                "Barranquilla", "Vía 40", 5000, true);
        when(venueService.findActiveVenues()).thenReturn(List.of(marina(), other));

        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$[1].code").value("VEN-BAQ-01"));

        verify(venueService).findActiveVenues();
    }

    // La ruta literal /active no debe confundirse con /{code}
    @Test
    void activeRouteShouldNotBeTreatedAsVenueCode() throws Exception {
        when(venueService.findActiveVenues()).thenReturn(List.of());

        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(venueService).findActiveVenues();
        verify(venueService, never()).findByCode("active");
    }
}
