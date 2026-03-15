package com.utc.flight_booking_service.payment.specification;

import com.utc.flight_booking_service.payment.dto.request.TransactionSearchRequest;
import com.utc.flight_booking_service.payment.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> getTransactionSpecification(TransactionSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keywordPattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate transactionNoMatch = criteriaBuilder.like(criteriaBuilder
                        .lower(root.get("transactionNo")), keywordPattern);

                Predicate bankRefNoMatch = criteriaBuilder.like(criteriaBuilder
                        .lower(root.get("bankRefNo")), keywordPattern);

                predicates.add(criteriaBuilder.or(transactionNoMatch, bankRefNoMatch));
            }
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }
            if (request.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}