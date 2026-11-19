package com.pulsepass.controller;

import com.pulsepass.domain.*;
import com.pulsepass.dto.response.*;
import com.pulsepass.exception.GlobalExceptionHandler;
import com.pulsepass.repository.*;
import com.pulsepass.security.*;
import com.pulsepass.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, CustomUserDetailsService.class,
        SecurityErrorHandler.class, AuthorizationService.class, SecuredEventService.class, GlobalExceptionHandler.class})
abstract class ControllerTestSupport {
    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @MockitoBean VenueService venues;
    @MockitoBean ArtistService artists;
    @MockitoBean EventService events;
    @MockitoBean TicketService tickets;
    @MockitoBean UserService users;
    @MockitoBean AuthService auth;
    @MockitoBean UserRepository userRepository;
    @MockitoBean TicketRepository ticketRepository;
    @MockitoBean UserCredentialRepository credentials;

    static final String PASSWORD="PruebasSeguras2026!";
    static final String HASH=new BCryptPasswordEncoder().encode(PASSWORD);
    static final LocalDateTime FUTURE=LocalDateTime.of(2099,12,12,20,0);
    User andrea;
    User carlos;

    @BeforeEach
    void identities() {
        andrea=user(1L,"andrea"); carlos=user(2L,"carlos");
        when(userRepository.findByEmailIgnoreCase("andrea@pulsepass.test")).thenReturn(Optional.of(andrea));
        when(userRepository.findByEmailIgnoreCase("carlos@pulsepass.test")).thenReturn(Optional.of(carlos));
        when(userRepository.findByUsername("andrea")).thenReturn(Optional.of(andrea));
        when(userRepository.findByUsername("carlos")).thenReturn(Optional.of(carlos));
        when(credentials.findByUserId(1L)).thenReturn(Optional.of(new UserCredential(andrea,HASH,Set.of(Role.USER))));
        var event=new Event("CMF-2026","Festival","Descripción",EventCategory.MUSIC,EventStatus.PUBLISHED,FUTURE,18);
        var ticket=new Ticket("TKT-A",TicketType.VIP,new BigDecimal("200.00"),TicketStatus.PAID,FUTURE.minusMonths(1),andrea,event);
        when(ticketRepository.findByTicketCode("TKT-A")).thenReturn(Optional.of(ticket));
        var other=new Ticket("TKT-B",TicketType.GENERAL,new BigDecimal("100.00"),TicketStatus.PAID,FUTURE.minusMonths(1),carlos,event);
        when(ticketRepository.findByTicketCode("TKT-B")).thenReturn(Optional.of(other));
    }
    static User user(Long id,String username) {
        var user=new User(username,username+"@pulsepass.test");
        ReflectionTestUtils.setField(user,"id",id);
        return user;
    }
    static RequestPostProcessor actor(Role role) { return actor(1L,"andrea",role); }
    static RequestPostProcessor actor(Long id,String username,Role role) {
        var principal=new PulsePassPrincipal(id,username,username+"@pulsepass.test",HASH,true,Set.of(role));
        return authentication(UsernamePasswordAuthenticationToken.authenticated(principal,null,principal.getAuthorities()));
    }
    static VenueResponse venue() { return new VenueResponse(1L,"VEN-SMR-01","Marina Convention Center","Santa Marta","Cra 1",3,true); }
    static ArtistResponse artist() { return new ArtistResponse(1L,"Solar Beat","Colombia","Electronic",true); }
    static EventResponse event(EventStatus status) { return new EventResponse(1L,"CMF-2026","Festival","Descripción",EventCategory.MUSIC,status,FUTURE,18,"VEN-SMR-01","Marina",List.of("Solar Beat")); }
    static EventSummaryResponse summary(EventStatus status) { return new EventSummaryResponse(1L,"CMF-2026","Festival",EventCategory.MUSIC,status,FUTURE,"Marina"); }
    static UserResponse userResponse() { return new UserResponse(1L,"andrea","andrea@pulsepass.test",true,"Andrea","Ruiz",null,"Santa Marta",LocalDate.of(1998,4,12)); }
    static TicketResponse ticket(TicketStatus status) { return new TicketResponse(1L,"TKT-A",TicketType.VIP,new BigDecimal("200.00"),status,FUTURE.minusMonths(1),"andrea@pulsepass.test","CMF-2026","Festival"); }
    static final String EVENT_JSON="""
            {"eventCode":"CMF-2026","name":"Festival","description":"Descripción","category":"MUSIC",
             "eventDate":"2099-12-12T20:00:00","minimumAge":18,"venueCode":"VEN-SMR-01"}
            """;
    static final String USER_JSON="""
            {"username":"andrea","email":"andrea@pulsepass.test","firstName":"Andrea","lastName":"Ruiz",
             "birthDate":"1998-04-12","password":"PruebasSeguras2026!"}
            """;
    static final String TICKET_JSON="""
            {"userEmail":"andrea@pulsepass.test","eventCode":"CMF-2026","type":"VIP"}
            """;
}
