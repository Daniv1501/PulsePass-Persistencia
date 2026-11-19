package com.pulsepass.controller;

import com.pulsepass.domain.TicketStatus;
import com.pulsepass.exception.*;
import com.pulsepass.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({TicketController.class,EventController.class})
class TicketControllerTest extends ControllerTestSupport {
    @Test void purchase() throws Exception {
        when(tickets.purchase(any())).thenReturn(ticket(TicketStatus.PAID));
        mvc.perform(post("/api/tickets").with(actor(Role.USER)).contentType("application/json").content(TICKET_JSON))
                .andExpect(status().isCreated()).andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value("PAID"));
        verify(tickets).purchase(any());
    }
    @Test void invalid() throws Exception {
        mvc.perform(post("/api/tickets").with(actor(Role.USER)).contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.type").exists());
        verifyNoInteractions(tickets);
    }
    @Test void missingEvent() throws Exception {
        when(tickets.purchase(any())).thenThrow(new ResourceNotFoundException("Event not found"));
        mvc.perform(post("/api/tickets").with(actor(Role.USER)).contentType("application/json").content(TICKET_JSON))
                .andExpect(status().isNotFound());
    }
    @Test void conflict() throws Exception {
        when(tickets.purchase(any())).thenThrow(new BusinessRuleException("No capacity"));
        mvc.perform(post("/api/tickets").with(actor(Role.USER)).contentType("application/json").content(TICKET_JSON))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.message").value("No capacity"));
    }
    @Test void findOwnTicket() throws Exception {
        when(tickets.findByCode("TKT-A")).thenReturn(ticket(TicketStatus.PAID));
        mvc.perform(get("/api/tickets/TKT-A").with(actor(Role.USER))).andExpect(status().isOk()).andExpect(jsonPath("$.ticketCode").value("TKT-A"));
        verify(tickets).findByCode("TKT-A");
    }
    @Test void byUser() throws Exception {
        when(tickets.findByUserEmail("andrea@pulsepass.test")).thenReturn(List.of(ticket(TicketStatus.PAID)));
        mvc.perform(get("/api/tickets/by-user").param("email","andrea@pulsepass.test").with(actor(Role.USER)))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].type").value("VIP"));
        verify(tickets).findByUserEmail("andrea@pulsepass.test");
    }
    @Test void paid() throws Exception {
        when(tickets.findPaidTicketsByEvent("CMF-2026")).thenReturn(List.of(ticket(TicketStatus.PAID)));
        mvc.perform(get("/api/events/CMF-2026/tickets/paid").with(actor(Role.STAFF))).andExpect(status().isOk());
        verify(tickets).findPaidTicketsByEvent("CMF-2026");
    }
    @Test void cancel() throws Exception {
        when(tickets.cancel("TKT-A")).thenReturn(ticket(TicketStatus.CANCELLED));
        mvc.perform(patch("/api/tickets/TKT-A/cancel").with(actor(Role.USER))).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        verify(tickets).cancel("TKT-A");
    }
    @Test void cancelConflict() throws Exception {
        when(tickets.cancel("TKT-A")).thenThrow(new BusinessRuleException("Cannot cancel used ticket"));
        mvc.perform(patch("/api/tickets/TKT-A/cancel").with(actor(Role.USER))).andExpect(status().isConflict());
    }
    @Test void use() throws Exception {
        when(tickets.markAsUsed("TKT-A")).thenReturn(ticket(TicketStatus.USED));
        mvc.perform(patch("/api/tickets/TKT-A/use").with(actor(Role.STAFF))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("USED"));
        verify(tickets).markAsUsed("TKT-A");
    }
    @Test void useConflict() throws Exception {
        when(tickets.markAsUsed("TKT-A")).thenThrow(new BusinessRuleException("Already used"));
        mvc.perform(patch("/api/tickets/TKT-A/use").with(actor(Role.STAFF))).andExpect(status().isConflict());
    }
}
