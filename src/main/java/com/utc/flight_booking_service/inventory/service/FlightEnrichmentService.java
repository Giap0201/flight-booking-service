package com.utc.flight_booking_service.inventory.service;

import com.utc.flight_booking_service.inventory.entity.Aircraft;
import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import com.utc.flight_booking_service.inventory.entity.FlightClassType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FlightEnrichmentService implements IFlightEnrichmentService{
    private static final double TAX_PERCENTAGE = 0.10; // Thuế 10%

    @Override
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

        // 3. Tính giá cho các hạng vé khác
        BigDecimal basePricePremiumEco = basePriceEco.multiply(BigDecimal.valueOf(1.5));
        BigDecimal basePriceBiz = basePriceEco.multiply(BigDecimal.valueOf(3.5));
        BigDecimal basePriceFirst = basePriceEco.multiply(BigDecimal.valueOf(5.0));

        // 4. Phân bổ số ghế (Do bảng Aircraft chỉ có 2 loại ghế tổng)
        int totalEcoSeats = aircraft.getTotalEconomySeats() != null ? aircraft.getTotalEconomySeats() : 0;
        int totalBizSeats = aircraft.getTotalBusinessSeats() != null ? aircraft.getTotalBusinessSeats() : 0;

        // Tỷ lệ chia ghế (Ví dụ: 20% Premium Eco, 80% Eco | 20% First, 80% Business)
        int premiumEcoSeats = (int) (totalEcoSeats * 0.2);
        int ecoSeats = totalEcoSeats - premiumEcoSeats;

        int firstClassSeats = (int) (totalBizSeats * 0.2);
        int bizSeats = totalBizSeats - firstClassSeats;

        // 5. Khởi tạo các hạng ghế nếu có số lượng > 0
        if (ecoSeats > 0) {
            flightClasses.add(createFlightClass(flight, FlightClassType.ECONOMY, basePriceEco, ecoSeats));
        }

        if (premiumEcoSeats > 0) {
            flightClasses.add(createFlightClass(flight, FlightClassType.PREMIUM_ECONOMY, basePricePremiumEco, premiumEcoSeats));
        }

        if (bizSeats > 0) {
            flightClasses.add(createFlightClass(flight, FlightClassType.BUSINESS, basePriceBiz, bizSeats));
        }

        if (firstClassSeats > 0) {
            flightClasses.add(createFlightClass(flight, FlightClassType.FIRST_CLASS, basePriceFirst, firstClassSeats));
        }

        return flightClasses;
    }

    // Hàm phụ trợ giúp code DRY (Don't Repeat Yourself)
    private FlightClass createFlightClass(Flight flight, FlightClassType type, BigDecimal basePrice, int seats) {
        FlightClass flightClass = new FlightClass();
        flightClass.setFlight(flight);
        flightClass.setClassType(type);
        flightClass.setBasePrice(basePrice);
        flightClass.setTaxPercentage(TAX_PERCENTAGE);
        flightClass.setTotalSeats(seats);
        flightClass.setAvailableSeats(seats); // Ban đầu available = total
        flightClass.setVersion(0); // Khởi tạo version cho Khóa lạc quan
        return flightClass;
    }
}
