package az.sarkhan.fintechsark.service.impl;

import az.sarkhan.fintechsark.dto.request.LoginRequest;
import az.sarkhan.fintechsark.dto.request.RegisterRequest;
import az.sarkhan.fintechsark.dto.response.AuthResponse;
import az.sarkhan.fintechsark.entity.User;
import az.sarkhan.fintechsark.exception.BusinessException;
import az.sarkhan.fintechsark.exception.UnauthorizedException;
import az.sarkhan.fintechsark.repository.UserRepository;
import az.sarkhan.fintechsark.security.JwtUtil;
import az.sarkhan.fintechsark.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use: " + request.email());
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .isActive(true)
                .build();
        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getId());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is deactivated");
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getId());
    }
}
