package com.pulsepass.controller;

import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.BadCredentialsException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest extends ControllerTestSupport {
    @Test void login() throws Exception {
        when(auth.login(any())).thenReturn(new AuthResponse("test-token","Bearer",900));
        mvc.perform(post("/api/auth/login").contentType("application/json").content("""
                {"identifier":"andrea","password":"PruebasSeguras2026!"}
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900)).andExpect(header().doesNotExist("Set-Cookie"));
        verify(auth).login(any());
    }
    @Test void wrongCredentials() throws Exception {
        when(auth.login(any())).thenThrow(new BadCredentialsException("DO NOT LEAK"));
        mvc.perform(post("/api/auth/login").contentType("application/json").content("""
                {"identifier":"andrea","password":"wrong"}
                """)).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Credenciales inválidas"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }
    @Test void missingFields() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.identifier").exists());
        verifyNoInteractions(auth);
    }
    @Test void me() throws Exception {
        mvc.perform(get("/api/auth/me").with(actor(Role.USER))).andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1)).andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.passwordHash").doesNotExist());
    }
}
