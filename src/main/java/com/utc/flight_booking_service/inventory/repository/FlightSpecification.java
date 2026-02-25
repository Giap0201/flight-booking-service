package com.utc.flight_booking_service.inventory.repository;

import com.utc.flight_booking_service.inventory.entity.Flight;
import com.utc.flight_booking_service.inventory.entity.FlightClass;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FlightSpecification {

    public static Specification<Flight> searchFlights(
            String origin, String destination, LocalDate date, int passengers) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo điểm đi (Origin)
            if (origin != null && !origin.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("origin").get("code"), origin.toUpperCase()));
            }

            // 2. Lọc theo điểm đến (Destination)
            if (destination != null && !destination.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("destination").get("code"), destination.toUpperCase()));
            }

            // 3. Lọc theo ngày bay (Chỉ lấy trong ngày, từ 00:00:00 đến 23:59:59)
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("departureTime"), startOfDay, endOfDay));
            }

            // 4. Lọc chuyến bay CÒN ĐỦ CHỖ NGỒI (Join với bảng FlightClass)
            if (passengers > 0) {
                Join<Flight, FlightClass> flightClassJoin = root.join("flightClasses", JoinType.INNER);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(flightClassJoin.get("availableSeats"), passengers));

                // Tránh duplicate kết quả nếu cả ghế ECO và BUSINESS đều thỏa mãn số lượng
                query.distinct(true);
            }

            // 5. Chỉ hiển thị các chuyến bay có trạng thái SCHEDULED
            predicates.add(criteriaBuilder.equal(root.get("status"), "SCHEDULED"));

            // Gộp tất cả điều kiện lại bằng toán tử AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

