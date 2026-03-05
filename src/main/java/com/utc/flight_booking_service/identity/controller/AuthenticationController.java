package com.utc.flight_booking_service.identity.controller;

import com.nimbusds.jose.JOSEException;
import com.utc.flight_booking_service.common.ApiResponse;
import com.utc.flight_booking_service.identity.dto.request.AuthenticationRequest;
import com.utc.flight_booking_service.identity.dto.request.IntrospectRequest;
import com.utc.flight_booking_service.identity.dto.response.AuthenticationReponse;
import com.utc.flight_booking_service.identity.dto.response.IntrospectResponse;
import com.utc.flight_booking_service.identity.service.IAuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    IAuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationReponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        return ApiResponse.<AuthenticationReponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

}
