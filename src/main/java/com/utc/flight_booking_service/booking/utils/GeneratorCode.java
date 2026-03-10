package com.utc.flight_booking_service.booking.utils;

import java.security.SecureRandom;

public class GeneratorCode {
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int PNR_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    // Ham random ra ma pnrcode
    public static String generatePnr() {
        StringBuilder result = new StringBuilder(PNR_LENGTH);
        for (int i = 0; i < PNR_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            result.append(CHARACTERS.charAt(index));
        }
        return result.toString();
    }



}
