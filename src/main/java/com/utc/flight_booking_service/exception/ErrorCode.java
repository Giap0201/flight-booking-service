package com.utc.flight_booking_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    //SYSTEM - COMMON (1xxx)
    UNCATEGORIZED_EXCEPTION(1001, "uncategorized", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1002, "Invalid key", HttpStatus.INTERNAL_SERVER_ERROR),


    //BOOKING (2XXX)
    FLIGHT_ID_REQUIRED(2001, "Flight id required", HttpStatus.BAD_REQUEST),
    FLIGHT_CLASS_ID_REQUIRED(2002, "Flight class id required", HttpStatus.BAD_REQUEST),


    FIRST_NAME_REQUIRED(2003, "First name required", HttpStatus.BAD_REQUEST),
    FIRST_NAME_TOO_LONG(2004, "First name too long", HttpStatus.BAD_REQUEST),
    LAST_NAME_REQUIRED(2005, "Last name required", HttpStatus.BAD_REQUEST),
    LAST_NAME_TOO_LONG(2006, "Last name too long", HttpStatus.BAD_REQUEST),
    DOB_REQUIRED(2007, "Dob required", HttpStatus.BAD_REQUEST),
    DOB_MUST_BE_IN_PAST(2008, "Dob must be in past", HttpStatus.BAD_REQUEST),
    GENDER_REQUIRED(2009, "Gender required", HttpStatus.BAD_REQUEST),
    PASSENGER_TYPE_REQUIRED(2010, "Passenger type required", HttpStatus.BAD_REQUEST),

    CANNOT_CREATE_PNR_CODE(2011, "Can't create pnr code", HttpStatus.INTERNAL_SERVER_ERROR),
    BOOKING_NOT_FOUND(2021, "Booking not found", HttpStatus.NOT_FOUND),
    ;
    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
