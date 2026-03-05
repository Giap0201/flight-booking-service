package com.utc.flight_booking_service.identity.service;

import com.nimbusds.jose.JOSEException;
import com.utc.flight_booking_service.identity.dto.request.AuthenticationRequest;
import com.utc.flight_booking_service.identity.dto.request.IntrospectRequest;
import com.utc.flight_booking_service.identity.dto.response.AuthenticationReponse;
import com.utc.flight_booking_service.identity.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface IAuthenticationService {

    AuthenticationReponse authenticate(AuthenticationRequest request);

    IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException;
}