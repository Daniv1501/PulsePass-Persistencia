package com.pulsepass.security;

import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.repository.UserRepository;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.UserService;
import com.pulsepass.validation.BcryptPasswordValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
@Transactional(readOnly=true)
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager manager;
    private final JwtService jwt;
    private final UserService users;
    private final UserRepository userRepository;
    private final UserCredentialRepository credentials;
    private final PasswordEncoder encoder;
    public AuthServiceImpl(AuthenticationManager manager, JwtService jwt, UserService users,
                           UserRepository userRepository, UserCredentialRepository credentials, PasswordEncoder encoder) {
        this.manager=manager; this.jwt=jwt; this.users=users; this.userRepository=userRepository;
        this.credentials=credentials; this.encoder=encoder;
    }
    @Override
    public AuthResponse login(LoginRequest request) {
        var authenticated = manager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                request.identifier(), request.password()));
        var principal = (PulsePassPrincipal) authenticated.getPrincipal();
        return new AuthResponse(jwt.issue(principal.userId()), "Bearer", jwt.ttlSeconds());
    }
    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (!new BcryptPasswordValidator().isValid(request.password(), null))
            throw new IllegalArgumentException("Contraseña inválida");
        UserResponse response = users.register(request);
        var user = userRepository.getReferenceById(response.id());
        credentials.save(new UserCredential(user, encoder.encode(request.password()), Set.of(Role.USER)));
        return response;
    }
}
