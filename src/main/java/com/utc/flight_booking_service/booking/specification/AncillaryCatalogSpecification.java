package com.utc.flight_booking_service.booking.specification;

import com.utc.flight_booking_service.booking.entity.AncillaryCatalog;
import com.utc.flight_booking_service.booking.request.AncillaryCatalogSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AncillaryCatalogSpecification {
    public static Specification<AncillaryCatalog> getAncillaryCatalogSpecification(AncillaryCatalogSearchRequest request) {
        return ((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keywordPattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate codeMatch = criteriaBuilder.like(criteriaBuilder
                        .lower(root.get("code")), keywordPattern);

                Predicate typeMatch = criteriaBuilder.like(criteriaBuilder.
                        lower(root.get("name")), keywordPattern);
                predicates.add(criteriaBuilder.or(codeMatch, typeMatch));
            }
            if (request.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
            }
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}
