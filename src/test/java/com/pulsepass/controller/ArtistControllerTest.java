package com.pulsepass.controller;
import com.pulsepass.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistController.class)
class ArtistControllerTest extends ControllerTestSupport {
    @Test void findArtist() throws Exception {
        when(artists.findById(1L)).thenReturn(artist());
        mvc.perform(get("/api/artists/1")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));
        verify(artists).findById(1L);
    }
    @Test void missingArtist() throws Exception {
        when(artists.findById(99L)).thenThrow(new ResourceNotFoundException("Artist not found"));
        mvc.perform(get("/api/artists/99")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }
    @Test void stageName() throws Exception {
        when(artists.findByStageName("Solar Beat")).thenReturn(artist());
        mvc.perform(get("/api/artists/by-stage-name").param("stageName","Solar Beat")).andExpect(status().isOk());
        verify(artists).findByStageName("Solar Beat");
    }
    @Test void active() throws Exception {
        when(artists.findActiveArtists()).thenReturn(List.of(artist()));
        mvc.perform(get("/api/artists/active")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
        verify(artists).findActiveArtists();
    }
    @Test void invalidId() throws Exception {
        mvc.perform(get("/api/artists/no-numero")).andExpect(status().isBadRequest());
        verifyNoInteractions(artists);
    }
    @Test void missingQuery() throws Exception {
        mvc.perform(get("/api/artists/by-stage-name")).andExpect(status().isBadRequest());
        verifyNoInteractions(artists);
    }
}
