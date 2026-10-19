package com.pulsepass.controller;

import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.EventService;
import com.pulsepass.service.TicketService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de los endpoints de tickets (PRD Controladores, sección 22.5).
 *
 * Se cargan TicketController y EventController porque la sección 8.5 ubica
 * GET /api/events/{eventCode}/tickets/paid bajo el recurso evento; ambos Services son mocks.
 */
@WebMvcTest({TicketController.class, EventController.class})
@Import(ControllerTestSecurityConfig.class)
@WithMockUser(roles = {"USER", "STAFF"})
class TicketControllerTest {

    private static final String VALID_BODY = """
            {
              "userEmail": "andrea@pulsepass.com",
              "eventCode": "CMF-2026",
              "type": "VIP"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private EventService eventService;

    private PurchaseTicketRequest validRequest() {
        return new PurchaseTicketRequest("andrea@pulsepass.com", "CMF-2026", TicketType.VIP);
    }

    private TicketResponse ticket(TicketStatus status) {
        return new TicketResponse(100L, "TCK-AB12CD34", TicketType.VIP, new BigDecimal("350000.00"),
                status, LocalDateTime.of(2026, 10, 4, 20, 0), "andrea@pulsepass.com",
                "CMF-2026", "Caribbean Music Fest 2026");
    }

    // TEST-CTRL-TKT-001 / AC-CTRL-009
    @Test
    void shouldReturn201WhenPurchaseIsValid() throws Exception {
        when(ticketService.purchase(validRequest())).thenReturn(ticket(TicketStatus.PAID));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", endsWith("/api/tickets/TCK-AB12CD34")))
                .andExpect(jsonPath("$.ticketCode").value("TCK-AB12CD34"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.type").value("VIP"))
                .andExpect(jsonPath("$.userEmail").value("andrea@pulsepass.com"))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(ticketService).purchase(validRequest());
    }

    // TEST-CTRL-TKT-002
    @Test
    void shouldReturn400WhenPurchaseRequestIsInvalid() throws Exception {
        String body = """
                {
                  "userEmail": "andrea-at-pulsepass",
                  "eventCode": ""
                }
                """;

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.userEmail").value("User email must be a valid email address"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.type").value("Ticket type is required"));

        verify(ticketService, never()).purchase(any());
    }

    @Test
    void shouldReturn400WhenTicketTypeIsUnknown() throws Exception {
        String body = VALID_BODY.replace("\"VIP\"", "\"PLATINUM\"");

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));

        verifyNoInteractions(ticketService);
    }

    // TEST-CTRL-TKT-003
    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        when(ticketService.purchase(validRequest()))
                .thenThrow(new ResourceNotFoundException("User not found: andrea@pulsepass.com"));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: andrea@pulsepass.com"));

        verify(ticketService).purchase(validRequest());
    }

    // TEST-CTRL-TKT-004 / AC-CTRL-010: menor de edad → la regla vive en Service, el Controller traduce a 409
    @Test
    void shouldReturn409WhenBusinessRuleRejectsPurchase() throws Exception {
        PurchaseTicketRequest laura = new PurchaseTicketRequest("laura@pulsepass.com", "CMF-2026", TicketType.GENERAL);
        when(ticketService.purchase(laura))
                .thenThrow(new BusinessRuleException("User does not meet minimum age of 18"));

        String body = """
                {"userEmail": "laura@pulsepass.com", "eventCode": "CMF-2026", "type": "GENERAL"}
                """;

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User does not meet minimum age of 18"));

        verify(ticketService).purchase(laura);
    }

    // TEST-CTRL-TKT-005
    @Test
    void shouldReturn200WhenTicketExists() throws Exception {
        when(ticketService.findByCode("TCK-AB12CD34")).thenReturn(ticket(TicketStatus.PAID));

        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-AB12CD34"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TCK-AB12CD34"))
                .andExpect(jsonPath("$.price").value(350000.00));

        verify(ticketService).findByCode("TCK-AB12CD34");
    }

    @Test
    void shouldReturn404WhenTicketDoesNotExist() throws Exception {
        when(ticketService.findByCode("TCK-NOPE"))
                .thenThrow(new ResourceNotFoundException("Ticket not found: TCK-NOPE"));

        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found: TCK-NOPE"));
    }

    // TEST-CTRL-TKT-006
    @Test
    void shouldReturn200WithTicketsOfUser() throws Exception {
        when(ticketService.findByUserEmail("andrea@pulsepass.com"))
                .thenReturn(List.of(ticket(TicketStatus.PAID), ticket(TicketStatus.CANCELLED)));

        mockMvc.perform(get("/api/tickets/by-user").param("email", "andrea@pulsepass.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userEmail").value("andrea@pulsepass.com"));

        verify(ticketService).findByUserEmail("andrea@pulsepass.com");
        verify(ticketService, never()).findByCode(anyString());
    }

    @Test
    void shouldReturn400WhenEmailParameterIsMissing() throws Exception {
        mockMvc.perform(get("/api/tickets/by-user"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.details.email").value("Parameter is required"));

        verifyNoInteractions(ticketService);
    }

    // TEST-CTRL-TKT-007
    @Test
    void shouldReturn200WithPaidTicketsOfEvent() throws Exception {
        when(ticketService.findPaidTicketsByEvent("CMF-2026")).thenReturn(List.of(ticket(TicketStatus.PAID)));

        mockMvc.perform(get("/api/events/{eventCode}/tickets/paid", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(ticketService).findPaidTicketsByEvent("CMF-2026");
        verifyNoInteractions(eventService);
    }

    // TEST-CTRL-TKT-008 / AC-CTRL-011
    @Test
    void shouldReturn200WhenCancellingPaidTicket() throws Exception {
        when(ticketService.cancel("TCK-AB12CD34")).thenReturn(ticket(TicketStatus.CANCELLED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TCK-AB12CD34"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(ticketService).cancel("TCK-AB12CD34");
    }

    // TEST-CTRL-TKT-009 / AC-CTRL-012
    @Test
    void shouldReturn409WhenCancellingUsedTicket() throws Exception {
        when(ticketService.cancel("TCK-AB12CD34"))
                .thenThrow(new BusinessRuleException("Cannot cancel ticket with status USED"));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TCK-AB12CD34"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot cancel ticket with status USED"));

        verify(ticketService).cancel("TCK-AB12CD34");
    }

    // TEST-CTRL-TKT-010 / AC-CTRL-013
    @Test
    void shouldReturn200WhenMarkingPaidTicketAsUsed() throws Exception {
        when(ticketService.markAsUsed("TCK-AB12CD34")).thenReturn(ticket(TicketStatus.USED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TCK-AB12CD34"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).markAsUsed("TCK-AB12CD34");
    }

    // TEST-CTRL-TKT-011 / AC-CTRL-014
    @Test
    void shouldReturn409WhenMarkingCancelledTicketAsUsed() throws Exception {
        when(ticketService.markAsUsed("TCK-AB12CD34"))
                .thenThrow(new BusinessRuleException("Cannot mark ticket as used with status CANCELLED"));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TCK-AB12CD34"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Cannot mark ticket as used with status CANCELLED"));

        verify(ticketService).markAsUsed("TCK-AB12CD34");
    }

    // Un verbo no soportado no se convierte en 200 ni en 500
    @Test
    void shouldReturn405WhenHttpMethodIsNotSupported() throws Exception {
        mockMvc.perform(delete("/api/tickets/{ticketCode}", "TCK-AB12CD34"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(405));

        verifyNoInteractions(ticketService);
    }
}
