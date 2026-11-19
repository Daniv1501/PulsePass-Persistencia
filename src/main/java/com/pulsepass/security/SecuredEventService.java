package com.pulsepass.security;

import com.pulsepass.domain.EventStatus;
import com.pulsepass.dto.response.EventResponse;
import com.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.EventService;
import org.springframework.stereotype.Service;
import java.util.List;

/** Visibilidad HTTP del catálogo; las reglas comerciales siguen en EventService. */
@Service
public class SecuredEventService {
    private final EventService events;
    private final AuthorizationService authorization;
    public SecuredEventService(EventService events, AuthorizationService authorization) {
        this.events=events; this.authorization=authorization;
    }
    public EventResponse findByCode(String code) {
        var event = events.findByCode(code);
        if (event.status() != EventStatus.PUBLISHED && !authorization.hasRole(Role.STAFF, Role.ADMIN))
            throw new ResourceNotFoundException("Evento no disponible");
        return event;
    }
    public List<EventSummaryResponse> findByArtist(String stageName) {
        var eventsByArtist = events.findByArtist(stageName);
        if (authorization.hasRole(Role.STAFF, Role.ADMIN)) return eventsByArtist;
        return eventsByArtist.stream().filter(e -> e.status() == EventStatus.PUBLISHED).toList();
    }
}
