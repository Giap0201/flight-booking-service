package com.utc.flight_booking_service.identity.service.impl;

import com.nimbusds.jose.JOSEException;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.configuration.JwtUtils;
import com.utc.flight_booking_service.identity.domain.entities.User;
import com.utc.flight_booking_service.identity.domain.repository.UserRepository;
import com.utc.flight_booking_service.identity.dto.request.AuthenticationRequest;
import com.utc.flight_booking_service.identity.dto.request.IntrospectRequest;
import com.utc.flight_booking_service.identity.dto.response.AuthenticationReponse;
import com.utc.flight_booking_service.identity.dto.response.IntrospectResponse;
import com.utc.flight_booking_service.identity.service.IAuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {
    UserRepository userRepository;
    JwtUtils jwtUtils;
    PasswordEncoder passwordEncoder;

    @Override
    public AuthenticationReponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
        String token = jwtUtils.generateToken(user);
        return AuthenticationReponse.builder()
                .token(token)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            jwtUtils.verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

}
