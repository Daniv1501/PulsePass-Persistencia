package com.pulsepass.repository;

import com.pulsepass.domain.Ticket;
import com.pulsepass.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // FR-TKT-006: tickets de un usuario por email, opcionalmente por status
    List<Ticket> findByUserEmail(String email);

    List<Ticket> findByUserEmailAndStatus(String email, TicketStatus status);

    // FR-TKT-007: tickets PAID de un evento por eventCode (Query Method, navega Ticket -> Event)
    List<Ticket> findByEventEventCodeAndStatus(String eventCode, TicketStatus status);

    // FR-TKT-008: conteo de tickets PAID de un evento (JPQL + COUNT, como pide la Sección 14)
    @Query("""
        select count(t)
        from Ticket t
        where t.event.eventCode = :eventCode
        and t.status = :status
        """)
    long countByEventEventCodeAndStatus(@Param("eventCode") String eventCode,
                                         @Param("status") TicketStatus status);

    // FR-SRC-004: tickets de eventos futuros, ordenados cronológicamente
    List<Ticket> findByEventEventDateAfterOrderByEventEventDateAsc(LocalDateTime date);
}