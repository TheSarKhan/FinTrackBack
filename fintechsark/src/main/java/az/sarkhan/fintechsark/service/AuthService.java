package az.sarkhan.fintechsark.service;

import az.sarkhan.fintechsark.dto.request.LoginRequest;
import az.sarkhan.fintechsark.dto.request.RegisterRequest;
import az.sarkhan.fintechsark.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
