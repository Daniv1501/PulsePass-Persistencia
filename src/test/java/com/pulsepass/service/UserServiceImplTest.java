package com.pulsepass.service;

import com.pulsepass.domain.User;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.exception.BusinessRuleException;
import com.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.mapper.UserMapper;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.impl.UserServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl service;

    private RegisterUserRequest validRequest() {
                return new RegisterUserRequest(
                "andrea", "andrea@email.com", "Andrea", "Gomez",
                "3001234567", "Santa Marta", LocalDate.of(1998, 5, 10), "UnaFraseSeguraLarga2026!");
    }

    // ---------- TEST-USER-001: registro válido ----------
    @Test
    void shouldRegisterValidUser() {
        UserResponse response = new UserResponse(
                1L, "andrea", "andrea@email.com", true,
                "Andrea", "Gomez", "3001234567", "Santa Marta", LocalDate.of(1998, 5, 10));

        when(repository.existsByUsername("andrea")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(User.class))).thenReturn(response);

        UserResponse result = service.register(validRequest());

        assertThat(result).isEqualTo(response);
        verify(repository).save(any(User.class));
    }

    // ---------- TEST-USER-002: username duplicado ----------
    @Test
    void shouldThrowWhenUsernameAlreadyExists() {
        when(repository.existsByUsername("andrea")).thenReturn(true);

        assertThatThrownBy(() -> service.register(validRequest()))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    // ---------- TEST-USER-003: email duplicado ----------
    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(repository.existsByUsername("andrea")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(validRequest()))
                .isInstanceOf(DuplicateResourceException.class);

        verify(repository, never()).save(any());
    }

    // ---------- TEST-USER-004: fecha de nacimiento futura ----------
    @Test
    void shouldThrowWhenBirthDateIsInFuture() {
        RegisterUserRequest request = new RegisterUserRequest(
                "andrea", "andrea@email.com", "Andrea", "Gomez",
                "3001234567", "Santa Marta", LocalDate.now().plusDays(1), "UnaFraseSeguraLarga2026!");

        when(repository.existsByUsername("andrea")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    private UserResponse responseFor(String username, String email) {
        return new UserResponse(1L, username, email, true,
                "Andrea", "Gomez", "3001234567", "Santa Marta", LocalDate.of(1998, 5, 10));
    }

    // ---------- BR-USER-003 y BR-USER-004: activo + perfil en la misma operación ----------
    @Test
    void shouldRegisterUserAsActiveWithProfile() {
        when(repository.existsByUsername("andrea")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("andrea@email.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(User.class))).thenReturn(responseFor("andrea", "andrea@email.com"));

        service.register(validRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getUsername()).isEqualTo("andrea");
        assertThat(saved.getEmail()).isEqualTo("andrea@email.com");
        assertThat(saved.getProfile()).isNotNull();
        assertThat(saved.getProfile().getFirstName()).isEqualTo("Andrea");
        assertThat(saved.getProfile().getLastName()).isEqualTo("Gomez");
        assertThat(saved.getProfile().getBirthDate()).isEqualTo(LocalDate.of(1998, 5, 10));
        assertThat(saved.getProfile().getUser()).isSameAs(saved);
    }

    // ---------- BR-USER-002: el email se compara ignorando mayúsculas ----------
    @Test
    void shouldCheckEmailUniquenessIgnoringCase() {
        RegisterUserRequest request = new RegisterUserRequest(
                "andrea2", "ANDREA@EMAIL.COM", "Andrea", "Gomez",
                "3001234567", "Santa Marta", LocalDate.of(1998, 5, 10));

        when(repository.existsByUsername("andrea2")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("ANDREA@EMAIL.COM")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldAllowBirthDateEqualToToday() {
        RegisterUserRequest request = new RegisterUserRequest(
                "baby", "baby@email.com", "Baby", "Test", null, null, LocalDate.now());

        when(repository.existsByUsername("baby")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("baby@email.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(User.class))).thenReturn(responseFor("baby", "baby@email.com"));

        service.register(request);

        verify(repository).save(any(User.class));
    }

    // ---------- FR-SVC-011 / FR-SVC-012: consultas ----------
    @Test
    void shouldFindUserByEmail() {
        User user = new User("andrea", "andrea@email.com");
        UserResponse response = responseFor("andrea", "andrea@email.com");

        when(repository.findByEmailIgnoreCase("andrea@email.com")).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        assertThat(service.findByEmail("andrea@email.com")).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        when(repository.findByEmailIgnoreCase("nadie@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByEmail("nadie@email.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nadie@email.com");
    }

    @Test
    void shouldFindUserByUsername() {
        User user = new User("andrea", "andrea@email.com");
        UserResponse response = responseFor("andrea", "andrea@email.com");

        when(repository.findByUsername("andrea")).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        assertThat(service.findByUsername("andrea")).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenUsernameNotFound() {
        when(repository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByUsername("nadie"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("nadie");
    }
}
