package com.utc.flight_booking_service.booking.specification;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.request.AdminBookingSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {
    public static Specification<Booking> getSearchSpec(AdminBookingSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            //Tim theo pnr code, tu dong format viet hoa tim theo like
            if (StringUtils.hasText(request.getPnrCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.upper(root.get("pnrCode")),
                        "%" + request.getPnrCode().toUpperCase() + "%"
                ));
            }
            if (StringUtils.hasText(request.getContactEmail())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("contactEmail")),
                        "%" + request.getContactEmail().toLowerCase() + "%"
                ));
            }
            if (StringUtils.hasText(request.getContactPhone())) {
                predicates.add(criteriaBuilder.equal(root.get("contactPhone"), request.getContactPhone()));
            }
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (request.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate()));
            }

            if (request.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getToDate()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
