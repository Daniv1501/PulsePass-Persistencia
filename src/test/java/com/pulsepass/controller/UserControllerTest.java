package com.pulsepass.controller;

import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest extends ControllerTestSupport {
    @Test void registration() throws Exception {
        when(auth.register(any())).thenReturn(userResponse());
        mvc.perform(post("/api/users").contentType("application/json").content(USER_JSON))
                .andExpect(status().isCreated()).andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.email").value("andrea@pulsepass.test"))
                .andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.passwordHash").doesNotExist());
        verify(auth).register(any());
    }
    @Test void invalidEmail() throws Exception {
        mvc.perform(post("/api/users").contentType("application/json").content(USER_JSON.replace("andrea@pulsepass.test","invalid")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.email").exists());
        verifyNoInteractions(auth);
    }
    @Test void passwordTooShort() throws Exception {
        mvc.perform(post("/api/users").contentType("application/json").content(USER_JSON.replace(PASSWORD,"short")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.password").exists());
        verifyNoInteractions(auth);
    }
    @Test void passwordLimitIsBytes() throws Exception {
        mvc.perform(post("/api/users").contentType("application/json").content(USER_JSON.replace(PASSWORD,"🔒".repeat(19))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.details.password").exists());
        verifyNoInteractions(auth);
    }
    @Test void duplicate() throws Exception {
        when(auth.register(any())).thenThrow(new DuplicateResourceException("Username already exists"));
        mvc.perform(post("/api/users").contentType("application/json").content(USER_JSON))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }
    @Test void ownEmail() throws Exception {
        when(users.findByEmail("andrea@pulsepass.test")).thenReturn(userResponse());
        mvc.perform(get("/api/users/by-email").param("email","andrea@pulsepass.test").with(actor(Role.USER)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value("andrea"));
        verify(users).findByEmail("andrea@pulsepass.test");
    }
    @Test void ownUsername() throws Exception {
        when(users.findByUsername("andrea")).thenReturn(userResponse());
        mvc.perform(get("/api/users/by-username").param("username","andrea").with(actor(Role.USER))).andExpect(status().isOk());
        verify(users).findByUsername("andrea");
    }
}
