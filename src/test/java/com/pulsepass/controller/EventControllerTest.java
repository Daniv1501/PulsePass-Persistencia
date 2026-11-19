package com.pulsepass.controller;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.exception.*;
import com.pulsepass.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest extends ControllerTestSupport {
    @Test void create() throws Exception {
        when(events.create(any())).thenReturn(event(EventStatus.DRAFT));
        mvc.perform(post("/api/events").with(actor(Role.ADMIN)).contentType("application/json").content(EVENT_JSON))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("DRAFT"));
        verify(events).create(any());
    }
    @Test void invalidBeforeService() throws Exception {
        mvc.perform(post("/api/events").with(actor(Role.ADMIN)).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.eventCode").exists());
        verifyNoInteractions(events);
    }
    @Test void find() throws Exception {
        when(events.findByCode("CMF-2026")).thenReturn(event(EventStatus.PUBLISHED));
        mvc.perform(get("/api/events/CMF-2026")).andExpect(status().isOk()).andExpect(jsonPath("$.eventCode").value("CMF-2026"));
        verify(events).findByCode("CMF-2026");
    }
    @Test void missing() throws Exception {
        when(events.findByCode("missing")).thenThrow(new ResourceNotFoundException("Event not found"));
        mvc.perform(get("/api/events/missing")).andExpect(status().isNotFound());
    }
    @Test void published() throws Exception {
        when(events.findPublishedEvents()).thenReturn(List.of(summary(EventStatus.PUBLISHED)));
        mvc.perform(get("/api/events/published")).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("PUBLISHED"));
        verify(events).findPublishedEvents();
    }
    @Test void publish() throws Exception {
        when(events.publish("CMF-2026")).thenReturn(event(EventStatus.PUBLISHED));
        mvc.perform(patch("/api/events/CMF-2026/publish").with(actor(Role.ADMIN))).andExpect(status().isOk());
        verify(events).publish("CMF-2026");
    }
    @Test void publishConflict() throws Exception {
        when(events.publish("CMF-2026")).thenThrow(new BusinessRuleException("Only DRAFT"));
        mvc.perform(patch("/api/events/CMF-2026/publish").with(actor(Role.ADMIN))).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only DRAFT"));
    }
    @Test void associate() throws Exception {
        when(events.addArtist("CMF-2026",1L)).thenReturn(event(EventStatus.DRAFT));
        mvc.perform(post("/api/events/CMF-2026/artists/1").with(actor(Role.ADMIN))).andExpect(status().isOk());
        verify(events).addArtist("CMF-2026",1L);
    }
    @Test void artistVisibility() throws Exception {
        when(events.findByArtist("Solar Beat")).thenReturn(List.of(summary(EventStatus.DRAFT),summary(EventStatus.PUBLISHED)));
        mvc.perform(get("/api/events/by-artist").param("stageName","Solar Beat"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].status").value("PUBLISHED"));
        verify(events).findByArtist("Solar Beat");
    }
    @Test void draftNotPublic() throws Exception {
        when(events.findByCode("CMF-2026")).thenReturn(event(EventStatus.DRAFT));
        mvc.perform(get("/api/events/CMF-2026")).andExpect(status().isNotFound()).andExpect(jsonPath("$.eventCode").doesNotExist());
        mvc.perform(get("/api/events/CMF-2026").with(actor(Role.STAFF))).andExpect(status().isOk());
    }
}
