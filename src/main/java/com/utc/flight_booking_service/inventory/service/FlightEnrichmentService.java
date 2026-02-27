package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.entity.Aircraft;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FlightEnrichmentService {
    private static final double TAX_PERCENTAGE = 0.10; // Thuế 10%

    public List<FlightClass> enrichFlight(Flight flight, Aircraft aircraft) {
        List<FlightClass> flightClasses = new ArrayList<>();

        // 1. Giả lập khoảng cách bay (Từ 500km -> 2000km)
        double distanceKm = ThreadLocalRandom.current().nextDouble(500, 2000);

        // 2. Tính giá vé = (Khoảng cách * 2000đ) + Random(100k -> 500k)
        double randomFee = ThreadLocalRandom.current().nextDouble(100_000, 500_000);
        double basePriceEcoDouble = (distanceKm * 2000) + randomFee;

        // Làm tròn đến hàng nghìn (VD: 1.254.320 -> 1.254.000)
        BigDecimal basePriceEco = BigDecimal.valueOf(basePriceEcoDouble)
                .divide(BigDecimal.valueOf(1000), 0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(1000));

        // Giá thương gia đắt gấp 3.5 lần
        BigDecimal basePriceBiz = basePriceEco.multiply(BigDecimal.valueOf(3.5));

        // 3. Khởi tạo hạng ghế ECONOMY dựa trên thông số Aircraft
        if (aircraft.getTotalEconomySeats() != null && aircraft.getTotalEconomySeats() > 0) {
            FlightClass economy = new FlightClass();
            economy.setFlight(flight);
            economy.setClassType("ECONOMY");
            economy.setBasePrice(basePriceEco);
            economy.setTaxPercentage(TAX_PERCENTAGE);
            economy.setTotalSeats(aircraft.getTotalEconomySeats());
            economy.setAvailableSeats(aircraft.getTotalEconomySeats()); // Ban đầu available = total
            economy.setVersion(0); // Khởi tạo version cho Khóa lạc quan
            flightClasses.add(economy);
        }

        // 4. Khởi tạo hạng ghế BUSINESS dựa trên thông số Aircraft
        if (aircraft.getTotalBusinessSeats() != null && aircraft.getTotalBusinessSeats() > 0) {
            FlightClass business = new FlightClass();
            business.setFlight(flight);
            business.setClassType("BUSINESS");
            business.setBasePrice(basePriceBiz);
            business.setTaxPercentage(TAX_PERCENTAGE);
            business.setTotalSeats(aircraft.getTotalBusinessSeats());
            business.setAvailableSeats(aircraft.getTotalBusinessSeats());
            business.setVersion(0);
            flightClasses.add(business);
        }

        return flightClasses;
    }
}
