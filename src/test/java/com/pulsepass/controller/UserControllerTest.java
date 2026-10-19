package com.pulsepass.controller;

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato HTTP de UserController (PRD Controladores, sección 22.4).
 */
@WebMvcTest(UserController.class)
@Import(ControllerTestSecurityConfig.class)
@WithMockUser(roles = "USER")
class UserControllerTest {

    private static final String VALID_BODY = """
            {
              "username": "andrea",
              "email": "andrea@pulsepass.com",
              "firstName": "Andrea",
              "lastName": "Gómez",
              "phone": "3001234567",
              "city": "Santa Marta",
              "birthDate": "1998-04-10",
              "password": "UnaFraseSeguraLarga2026!"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    private RegisterUserRequest validRequest() {
        return new RegisterUserRequest("andrea", "andrea@pulsepass.com", "Andrea", "Gómez",
                "3001234567", "Santa Marta", LocalDate.of(1998, 4, 10), "UnaFraseSeguraLarga2026!");
    }

    private UserResponse andrea() {
        return new UserResponse(1L, "andrea", "andrea@pulsepass.com", true, "Andrea", "Gómez",
                "3001234567", "Santa Marta", LocalDate.of(1998, 4, 10));
    }

    // TEST-CTRL-USR-001 / AC-CTRL-007
    @Test
    void shouldReturn201WhenRegisteringValidUser() throws Exception {
        when(authService.register(validRequest())).thenReturn(andrea());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Location", containsString("/api/users/by-username?username=andrea")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@pulsepass.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.birthDate").value("1998-04-10"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(authService).register(validRequest());
    }

    // TEST-CTRL-USR-002
    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        String body = VALID_BODY.replace("andrea@pulsepass.com", "not-an-email");

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.email").value("Email must be a valid email address"));

        verify(authService, never()).register(any());
    }

    @Test
    void shouldReturn400WithEveryInvalidFieldWhenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").value("Username is required"))
                .andExpect(jsonPath("$.details.email").value("Email is required"))
                .andExpect(jsonPath("$.details.firstName").value("First name is required"))
                .andExpect(jsonPath("$.details.lastName").value("Last name is required"))
                .andExpect(jsonPath("$.details.birthDate").value("Birth date is required"));

        verify(authService, never()).register(any());
    }

    // TEST-CTRL-USR-003
    @Test
    void shouldReturn409WhenUsernameIsDuplicated() throws Exception {
        when(authService.register(validRequest()))
                .thenThrow(new DuplicateResourceException("Username already exists: andrea"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already exists: andrea"));

        verify(authService).register(validRequest());
    }

    // AC-CTRL-008: email duplicado → 409 sin exponer detalles internos
    @Test
    void shouldReturn409WithoutInternalDetailsWhenEmailIsDuplicated() throws Exception {
        when(authService.register(validRequest()))
                .thenThrow(new DuplicateResourceException("Email already exists: andrea@pulsepass.com"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists: andrea@pulsepass.com"))
                .andExpect(jsonPath("$.details").isEmpty())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(content().string(not(containsString("DuplicateResourceException"))));
    }

    // TEST-CTRL-USR-004
    @Test
    void shouldReturn200WhenFindingByEmail() throws Exception {
        when(userService.findByEmail("andrea@pulsepass.com")).thenReturn(andrea());

        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@pulsepass.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@pulsepass.com"))
                .andExpect(jsonPath("$.firstName").value("Andrea"));

        verify(userService).findByEmail("andrea@pulsepass.com");
    }

    @Test
    void shouldReturn404WhenEmailDoesNotExist() throws Exception {
        when(userService.findByEmail("nadie@pulsepass.com"))
                .thenThrow(new ResourceNotFoundException("User not found: nadie@pulsepass.com"));

        mockMvc.perform(get("/api/users/by-email").param("email", "nadie@pulsepass.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: nadie@pulsepass.com"));
    }

    @Test
    void shouldReturn400WhenEmailQueryParamIsInvalid() throws Exception {
        mockMvc.perform(get("/api/users/by-email").param("email", "bad-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.email").value("Email must be a valid email address"));

        verifyNoInteractions(userService);
    }

    // TEST-CTRL-USR-005
    @Test
    void shouldReturn200WhenFindingByUsername() throws Exception {
        when(userService.findByUsername("andrea")).thenReturn(andrea());

        mockMvc.perform(get("/api/users/by-username").param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(userService).findByUsername("andrea");
    }

    @Test
    void shouldReturn404WhenUsernameDoesNotExist() throws Exception {
        when(userService.findByUsername("ghost"))
                .thenThrow(new ResourceNotFoundException("User not found: ghost"));

        mockMvc.perform(get("/api/users/by-username").param("username", "ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        String json = VALID_BODY.replace("UnaFraseSeguraLarga2026!", "corta");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").value("Password must be between 12 and 72 characters"));

        verify(authService, never()).register(any());
    }

    @Test
    void shouldReturn400WhenUsernameContainsAtSign() throws Exception {
        String json = VALID_BODY.replace("\"username\": \"andrea\"", "\"username\": \"andrea@x\"");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.username").value("Username must not contain '@' or whitespace"));

        verify(authService, never()).register(any());
    }
}
