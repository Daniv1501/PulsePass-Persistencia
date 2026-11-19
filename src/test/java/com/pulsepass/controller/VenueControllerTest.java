package com.pulsepass.controller;
import com.pulsepass.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
class VenueControllerTest extends ControllerTestSupport {
    @Test void findVenue() throws Exception {
        when(venues.findByCode("VEN-SMR-01")).thenReturn(venue());
        mvc.perform(get("/api/venues/VEN-SMR-01")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json")).andExpect(jsonPath("$.capacity").value(3));
        verify(venues).findByCode("VEN-SMR-01");
    }
    @Test void missingVenue() throws Exception {
        when(venues.findByCode("missing")).thenThrow(new ResourceNotFoundException("Venue not found"));
        mvc.perform(get("/api/venues/missing")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.details").isMap());
    }
    @Test void activeVenuesArePublic() throws Exception {
        when(venues.findActiveVenues()).thenReturn(List.of(venue()));
        mvc.perform(get("/api/venues/active")).andExpect(status().isOk()).andExpect(jsonPath("$[0].active").value(true));
        verify(venues).findActiveVenues();
    }
}
