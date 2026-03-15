package com.utc.flight_booking_service.inventory.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ICheapestPriceProjection {
    LocalDate getDepartureDate();
    BigDecimal getMinPrice();
}
