package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.dto.auth.AuthResponse;
import io.github.alexeyaleksandrov.jacademicsupport.dto.auth.LoginRequest;
import io.github.alexeyaleksandrov.jacademicsupport.dto.auth.RegisterRequest;
import io.github.alexeyaleksandrov.jacademicsupport.models.User;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.UserRepository;
import io.github.alexeyaleksandrov.jacademicsupport.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email уже используется!");
        }

        // Создаём нового пользователя
        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .middleName(registerRequest.getMiddleName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Генерируем JWT токен
        String jwt = tokenProvider.generateToken(savedUser);

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getMiddleName()
        );
    }

    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        // Аутентификация пользователя
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Получаем пользователя из базы данных
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Генерируем JWT токен
        String jwt = tokenProvider.generateToken(user);

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMiddleName()
        );
    }
}
