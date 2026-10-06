package com.pulsepass.service;

import com.pulsepass.domain.Event;
import com.pulsepass.domain.EventCategory;
import com.pulsepass.domain.EventStatus;
import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.TicketStatus;
import com.pulsepass.domain.TicketType;
import com.pulsepass.domain.User;
import com.pulsepass.domain.UserProfile;
import com.pulsepass.domain.Venue;
import com.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.dto.response.TicketResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.TicketMapper;
import com.pulsepass.repository.EventRepository;
import com.pulsepass.repository.TicketRepository;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.impl.TicketServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper mapper;

    @InjectMocks
    private TicketServiceImpl service;

    private User activeUser(LocalDate birthDate) {
        User user = new User("andrea", "andrea@email.com");
        UserProfile profile = new UserProfile("Andrea", "Gomez", "300", "Santa Marta", birthDate);
        user.assignProfile(profile);
        return user;
    }

    private Venue venueWithCapacity(int capacity) {
        return new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta", "Cra 1 #2-3", capacity);
    }

    private Event publishedEvent(Venue venue, int minimumAge) {
        Event event = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED, LocalDateTime.now().plusMonths(1), minimumAge);
        venue.addEvent(event);
        return event;
    }

    private PurchaseTicketRequest request() {
        return new PurchaseTicketRequest("andrea@email.com", "CMF-2026", TicketType.GENERAL);
    }

    // ---------- TEST-TICKET-001: compra válida -> PAID ----------
    @Test
    void shouldPurchaseTicketWhenRequestIsValid() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Venue venue = venueWithCapacity(3);
        Event event = publishedEvent(venue, 18);

        TicketResponse response = new TicketResponse(1L, "TCK-ABC", TicketType.GENERAL,
                new BigDecimal("100.00"), TicketStatus.PAID, LocalDateTime.now(),
                "andrea@email.com", "CMF-2026", "Caribbean Music Fest");

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(java.util.Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(java.util.Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(0L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(response);

        TicketResponse result = service.purchase(request());

        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository).save(any(Ticket.class));
    }

    // ---------- TEST-TICKET-002: usuario inexistente ----------
    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---------- TEST-TICKET-003: usuario inactivo ----------
    @Test
    void shouldThrowWhenUserIsInactive() {
        User inactiveUser = activeUser(LocalDate.now().minusYears(25));
        inactiveUser.setActive(false);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(java.util.Optional.of(inactiveUser));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---------- TEST-TICKET-004: evento DRAFT ----------
    @Test
    void shouldThrowWhenEventIsDraft() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Venue venue = venueWithCapacity(3);
        Event event = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.DRAFT, LocalDateTime.now().plusMonths(1), 18);
        venue.addEvent(event);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(java.util.Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---------- TEST-TICKET-005: evento CANCELLED ----------
    @Test
    void shouldThrowWhenEventIsCancelled() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Venue venue = venueWithCapacity(3);
        Event event = new Event("CMF-2026", "Caribbean Music Fest", "Festival",
                EventCategory.MUSIC, EventStatus.CANCELLED, LocalDateTime.now().plusMonths(1), 18);
        venue.addEvent(event);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(java.util.Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---------- TEST-TICKET-006: usuario menor de edad ----------
    @Test
    void shouldThrowWhenUserDoesNotMeetMinimumAge() {
        User minorUser = activeUser(LocalDate.now().minusYears(17));
        Venue venue = venueWithCapacity(3);
        Event event = publishedEvent(venue, 18);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(java.util.Optional.of(minorUser));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(java.util.Optional.of(event));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---------- TEST-TICKET-007: sin capacidad ----------
    @Test
    void shouldThrowWhenNoCapacityAvailable() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Venue venue = venueWithCapacity(1);
        Event event = publishedEvent(venue, 18);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(java.util.Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(java.util.Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(1L);

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---------- TEST-TICKET-008: último ticket disponible -> SOLD_OUT ----------
    @Test
    void shouldMarkEventAsSoldOutWhenLastTicketIsPurchased() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Venue venue = venueWithCapacity(1);
        Event event = publishedEvent(venue, 18);

        TicketResponse response = new TicketResponse(1L, "TCK-ABC", TicketType.GENERAL,
                new BigDecimal("100.00"), TicketStatus.PAID, LocalDateTime.now(),
                "andrea@email.com", "CMF-2026", "Caribbean Music Fest");

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(java.util.Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(java.util.Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(0L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(response);

        service.purchase(request());

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        verify(ticketRepository).save(any(Ticket.class));

        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(EventStatus.SOLD_OUT);
    }

    // ---------- TEST-TICKET-009: cancelar PAID -> CANCELLED ----------
    @Test
    void shouldCancelPaidTicket() {
        Venue venue = venueWithCapacity(3);
        Event event = publishedEvent(venue, 0);
        User user = activeUser(LocalDate.now().minusYears(25));
        Ticket ticket = new Ticket("TCK-ABC", TicketType.GENERAL, new BigDecimal("100.00"),
                TicketStatus.PAID, LocalDateTime.now(), user, event);

        TicketResponse response = new TicketResponse(1L, "TCK-ABC", TicketType.GENERAL,
                new BigDecimal("100.00"), TicketStatus.CANCELLED, ticket.getPurchaseDate(),
                "andrea@email.com", "CMF-2026", "Caribbean Music Fest");

        when(ticketRepository.findByTicketCode("TCK-ABC")).thenReturn(java.util.Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(mapper.toResponse(ticket)).thenReturn(response);

        TicketResponse result = service.cancel("TCK-ABC");

        assertThat(result.status()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
    }

    // ---------- TEST-TICKET-010: cancelar USED -> error ----------
    @Test
    void shouldThrowWhenCancellingUsedTicket() {
        Venue venue = venueWithCapacity(3);
        Event event = publishedEvent(venue, 0);
        User user = activeUser(LocalDate.now().minusYears(25));
        Ticket ticket = new Ticket("TCK-ABC", TicketType.GENERAL, new BigDecimal("100.00"),
                TicketStatus.USED, LocalDateTime.now(), user, event);

        when(ticketRepository.findByTicketCode("TCK-ABC")).thenReturn(java.util.Optional.of(ticket));

        assertThatThrownBy(() -> service.cancel("TCK-ABC"))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---------- TEST-TICKET-011: marcar PAID como usado -> USED ----------
    @Test
    void shouldMarkPaidTicketAsUsed() {
        Venue venue = venueWithCapacity(3);
        Event event = publishedEvent(venue, 0);
        User user = activeUser(LocalDate.now().minusYears(25));
        Ticket ticket = new Ticket("TCK-ABC", TicketType.GENERAL, new BigDecimal("100.00"),
                TicketStatus.PAID, LocalDateTime.now(), user, event);

        TicketResponse response = new TicketResponse(1L, "TCK-ABC", TicketType.GENERAL,
                new BigDecimal("100.00"), TicketStatus.USED, ticket.getPurchaseDate(),
                "andrea@email.com", "CMF-2026", "Caribbean Music Fest");

        when(ticketRepository.findByTicketCode("TCK-ABC")).thenReturn(java.util.Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(mapper.toResponse(ticket)).thenReturn(response);

        TicketResponse result = service.markAsUsed("TCK-ABC");

        assertThat(result.status()).isEqualTo(TicketStatus.USED);
    }

    // ---------- TEST-TICKET-012: usar ticket CANCELLED -> error ----------
    @Test
    void shouldThrowWhenUsingCancelledTicket() {
        Venue venue = venueWithCapacity(3);
        Event event = publishedEvent(venue, 0);
        User user = activeUser(LocalDate.now().minusYears(25));
        Ticket ticket = new Ticket("TCK-ABC", TicketType.GENERAL, new BigDecimal("100.00"),
                TicketStatus.CANCELLED, LocalDateTime.now(), user, event);

        when(ticketRepository.findByTicketCode("TCK-ABC")).thenReturn(java.util.Optional.of(ticket));

        assertThatThrownBy(() -> service.markAsUsed("TCK-ABC"))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // =====================================================================
    // Pruebas adicionales: reglas BR-TICKET-001..014 completas
    // =====================================================================

    private TicketResponse responseWith(TicketStatus status) {
        return new TicketResponse(1L, "TCK-ABC", TicketType.GENERAL, new BigDecimal("100.00"),
                status, LocalDateTime.now(), "andrea@email.com", "CMF-2026", "Caribbean Music Fest");
    }

    private Ticket ticketWith(TicketStatus status, Event event) {
        return new Ticket("TCK-ABC", TicketType.GENERAL, new BigDecimal("100.00"),
                status, LocalDateTime.now(), activeUser(LocalDate.now().minusYears(25)), event);
    }

    // ---------- BR-TICKET-003: evento inexistente ----------
    @Test
    void shouldThrowWhenEventNotFound() {
        User user = activeUser(LocalDate.now().minusYears(25));

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("CMF-2026");

        verify(ticketRepository, never()).save(any());
    }

    // ---------- BR-TICKET-004: SOLD_OUT y FINISHED tampoco permiten compra ----------
    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"SOLD_OUT", "FINISHED"})
    void shouldThrowWhenEventIsSoldOutOrFinished(EventStatus status) {
        User user = activeUser(LocalDate.now().minusYears(25));
        Event event = publishedEvent(venueWithCapacity(3), 18);
        event.setStatus(status);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    // ---------- BR-TICKET-005: evento que ya ocurrió ----------
    @Test
    void shouldThrowWhenEventAlreadyOccurred() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Event event = publishedEvent(venueWithCapacity(3), 18);
        event.setEventDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    // ---------- BR-TICKET-006: la edad se evalúa en la fecha del evento ----------
    @Test
    void shouldAllowUserWhoTurnsExactMinimumAgeOnEventDate() {
        Event event = publishedEvent(venueWithCapacity(3), 18);
        LocalDate birthDate = event.getEventDate().toLocalDate().minusYears(18);
        User user = activeUser(birthDate);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(0L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(responseWith(TicketStatus.PAID));

        TicketResponse result = service.purchase(request());

        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void shouldRejectUserWhoTurnsMinimumAgeOneDayAfterEventDate() {
        Event event = publishedEvent(venueWithCapacity(3), 18);
        LocalDate birthDate = event.getEventDate().toLocalDate().minusYears(18).plusDays(1);
        User user = activeUser(birthDate);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("minimum age");

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldRejectUserWithoutBirthDateWhenEventHasMinimumAge() {
        User user = activeUser(null);
        Event event = publishedEvent(venueWithCapacity(3), 18);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldNotCheckAgeWhenEventHasNoAgeRestriction() {
        User userWithoutProfile = new User("andrea", "andrea@email.com");
        Event event = publishedEvent(venueWithCapacity(3), 0);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(userWithoutProfile));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(0L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(responseWith(TicketStatus.PAID));

        TicketResponse result = service.purchase(request());

        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
    }

    // ---------- Precio calculado por el sistema, ticket nace PAID ----------
    @Test
    void shouldCreateTicketWithServerSidePriceAndPaidStatus() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Event event = publishedEvent(venueWithCapacity(3), 18);
        PurchaseTicketRequest vipRequest =
                new PurchaseTicketRequest("andrea@email.com", "CMF-2026", TicketType.VIP);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(0L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(responseWith(TicketStatus.PAID));

        service.purchase(vipRequest);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(saved.getType()).isEqualTo(TicketType.VIP);
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(saved.getPrice().signum()).isGreaterThanOrEqualTo(0);
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getEvent()).isSameAs(event);
        assertThat(saved.getTicketCode()).startsWith("TCK-");
        assertThat(saved.getPurchaseDate()).isNotNull();
    }

    @Test
    void shouldThrowWhenTicketTypeIsMissing() {
        PurchaseTicketRequest noType = new PurchaseTicketRequest("andrea@email.com", "CMF-2026", null);

        assertThatThrownBy(() -> service.purchase(noType))
                .isInstanceOf(BusinessRuleException.class);

        verifyNoInteractions(userRepository, eventRepository, ticketRepository);
    }

    // ---------- BR-TICKET-008: SOLD_OUT solo cuando se completa el cupo ----------
    @Test
    void shouldKeepEventPublishedWhenCapacityIsNotCompleted() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Event event = publishedEvent(venueWithCapacity(3), 18);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(1L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(responseWith(TicketStatus.PAID));

        service.purchase(request());

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository, never()).save(any());
    }

    // ---------- BR-TICKET-007: paidTickets >= capacity ----------
    @Test
    void shouldThrowWhenPaidTicketsExceedCapacity() {
        User user = activeUser(LocalDate.now().minusYears(25));
        Event event = publishedEvent(venueWithCapacity(3), 18);

        when(userRepository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID)).thenReturn(5L);

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("capacity");

        verify(ticketRepository, never()).save(any());
        verify(eventRepository, never()).save(any());
    }

    // ---------- Consultas (lecturas) ----------
    @Test
    void shouldFindTicketByCode() {
        Ticket ticket = ticketWith(TicketStatus.PAID, publishedEvent(venueWithCapacity(3), 0));

        when(ticketRepository.findByTicketCode("TCK-ABC")).thenReturn(Optional.of(ticket));
        when(mapper.toResponse(ticket)).thenReturn(responseWith(TicketStatus.PAID));

        TicketResponse result = service.findByCode("TCK-ABC");

        assertThat(result.ticketCode()).isEqualTo("TCK-ABC");
    }

    @Test
    void shouldThrowWhenTicketCodeNotFound() {
        when(ticketRepository.findByTicketCode("TCK-NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("TCK-NOPE"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("TCK-NOPE");
    }

    @Test
    void shouldFindTicketsByUserEmail() {
        Ticket ticket = ticketWith(TicketStatus.PAID, publishedEvent(venueWithCapacity(3), 0));

        when(ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc("andrea@email.com"))
                .thenReturn(List.of(ticket));
        when(mapper.toResponse(ticket)).thenReturn(responseWith(TicketStatus.PAID));

        List<TicketResponse> result = service.findByUserEmail("andrea@email.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userEmail()).isEqualTo("andrea@email.com");
    }

    @Test
    void shouldFindOnlyPaidTicketsByEvent() {
        Ticket ticket = ticketWith(TicketStatus.PAID, publishedEvent(venueWithCapacity(3), 0));

        when(ticketRepository.findByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID))
                .thenReturn(List.of(ticket));
        when(mapper.toResponse(ticket)).thenReturn(responseWith(TicketStatus.PAID));

        List<TicketResponse> result = service.findPaidTicketsByEvent("CMF-2026");

        assertThat(result).extracting(TicketResponse::status).containsOnly(TicketStatus.PAID);
        verify(ticketRepository).findByEventEventCodeAndStatus("CMF-2026", TicketStatus.PAID);
    }

    // ---------- Cancelación: BR-TICKET-010..012 ----------
    @Test
    void shouldThrowWhenCancellingUnknownTicket() {
        when(ticketRepository.findByTicketCode("TCK-NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel("TCK-NOPE"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCancellingAlreadyCancelledTicket() {
        Ticket ticket = ticketWith(TicketStatus.CANCELLED, publishedEvent(venueWithCapacity(3), 0));

        when(ticketRepository.findByTicketCode("TCK-ABC")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.cancel("TCK-ABC"))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCancellingAfterEventDate() {
        Event event = publishedEvent(venueWithCapacity(3), 0);
        event.setEventDate(LocalDateTime.now().minusHours(1));
        Ticket ticket = ticketWith(TicketStatus.PAID, event);

        when(ticketRepository.findByTicketCode("TCK-ABC")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.cancel("TCK-ABC"))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository, never()).save(any());
    }

    // ---------- Uso: BR-TICKET-013..014 ----------
    @Test
    void shouldThrowWhenMarkingUnknownTicketAsUsed() {
        when(ticketRepository.findByTicketCode("TCK-NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markAsUsed("TCK-NOPE"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(ticketRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMarkingAlreadyUsedTicketAsUsed() {
        Ticket ticket = ticketWith(TicketStatus.USED, publishedEvent(venueWithCapacity(3), 0));

        when(ticketRepository.findByTicketCode("TCK-ABC")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.markAsUsed("TCK-ABC"))
                .isInstanceOf(BusinessRuleException.class);

        verify(ticketRepository, never()).save(any());
    }
}
