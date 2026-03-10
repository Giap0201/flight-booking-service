package com.utc.flight_booking_service.identity.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import com.utc.flight_booking_service.exception.AppException;
import com.utc.flight_booking_service.exception.ErrorCode;
import com.utc.flight_booking_service.identity.configuration.JwtUtils;
import com.utc.flight_booking_service.identity.domain.entities.InvalidatedToken;
import com.utc.flight_booking_service.identity.domain.entities.User;
import com.utc.flight_booking_service.identity.domain.repository.InvalidatedTokenRepository;
import com.utc.flight_booking_service.identity.domain.repository.UserRepository;
import com.utc.flight_booking_service.identity.dto.request.AuthenticationRequest;
import com.utc.flight_booking_service.identity.dto.request.IntrospectRequest;
import com.utc.flight_booking_service.identity.dto.request.LogoutRequest;
import com.utc.flight_booking_service.identity.dto.request.RefreshRequest;
import com.utc.flight_booking_service.identity.dto.response.AuthenticationReponse;
import com.utc.flight_booking_service.identity.dto.response.IntrospectResponse;
import com.utc.flight_booking_service.identity.service.IAuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService implements IAuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    UserRepository userRepository;
    JwtUtils jwtUtils;
    PasswordEncoder passwordEncoder;
    InvalidatedTokenRepository invalidatedTokenRepository;

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

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = jwtUtils.verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    @Override
    public AuthenticationReponse refreshtoken(RefreshRequest request) throws ParseException, JOSEException {
        SignedJWT signedJWT = jwtUtils.verifyToken(request.getToken(), true);
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expirytime = signedJWT.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .expiryTime(expirytime)
                .id(jit)
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
        var id = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findById(UUID.fromString(id)).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String token = jwtUtils.generateToken(user);
        return AuthenticationReponse.builder()
                .token(token)
                .build();
    }

}
