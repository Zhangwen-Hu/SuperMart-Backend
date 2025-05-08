package com.example.supermartbackend.service;

import com.example.supermartbackend.dto.JwtResponse;
import com.example.supermartbackend.dto.LoginRequest;
import com.example.supermartbackend.dto.SignupRequest;

public interface AuthService {
    
    void register(SignupRequest signupRequest);
    
    JwtResponse login(LoginRequest loginRequest);
} 