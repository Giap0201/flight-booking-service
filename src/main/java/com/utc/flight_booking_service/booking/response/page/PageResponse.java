package com.utc.flight_booking_service.booking.response.page;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageResponse<T> {
    int currentPage; // Trang hien tai
    int pageSize; // Tong so phan tu tren mot trang
    int totalPages;
    long totalElements; // Tong so phan tu
    List<T> content; //Du lieu thuc te
}
