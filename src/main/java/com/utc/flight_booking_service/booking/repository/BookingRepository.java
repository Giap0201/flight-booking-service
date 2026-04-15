package com.utc.flight_booking_service.booking.repository;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.dashboard.dto.DailyRevenueResponse;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    boolean existsByPnrCode(String pnrCode);

    List<Booking> findByStatusAndExpireAtBefore(BookingStatus bookingStatus, LocalDateTime now);

    Optional<Booking> findByPnrCode(String pnrCode);

    int deleteByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAtBefore);

    Optional<Booking> findByPnrCodeAndContactEmail(String pnrCode, String contactEmail);

    // Lay tat ca (Loc theo userId)
    Page<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Loc ve theo trang thai
    Page<Booking> findByUserIdAndStatusInOrderByCreatedAtDesc(UUID userId, List<BookingStatus> statuses, Pageable pageable);

    // 3. Lọc vé SẮP BAY (Status là PAID/CONFIRMED VÀ Giờ bay > Hiện tại)
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingFlights bf " +
            "WHERE b.userId = :userId " +
            "AND b.status IN :statuses " +
            "AND bf.originDepartureTime > :now " +
            "ORDER BY b.createdAt DESC")
    Page<Booking> findUpcomingBookings(
            @Param("userId") UUID userId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    // 4. Lọc vé ĐÃ BAY (Status là PAID/CONFIRMED VÀ Giờ bay <= Hiện tại)
    @Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingFlights bf " +
            "WHERE b.userId = :userId " +
            "AND b.status IN :statuses " +
            "AND bf.originDepartureTime <= :now " +
            "ORDER BY b.createdAt DESC")
    Page<Booking> findCompletedBookings(
            @Param("userId") UUID userId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    // 1. Tính tổng doanh thu thuần (Thu - Hoàn) trong 1 khoảng thời gian
    // Lưu ý: Query thẳng vào entity Transaction, dùng COALESCE để tránh lỗi Null
    @Query("SELECT COALESCE(SUM(CASE WHEN t.paymentMethod = 'VNPAY' THEN t.amount WHEN t.paymentMethod = 'VNPAY_REFUND' THEN -t.amount ELSE 0 END), 0) " +
            "FROM Transaction t WHERE t.status = 'SUCCESS' AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 2. Đếm số lượng Booking theo trạng thái (Giữ nguyên của bạn vì đã chuẩn)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status IN :statuses AND b.createdAt BETWEEN :startDate AND :endDate")
    long countBookingsByStatus(@Param("statuses") List<BookingStatus> statuses, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 3. Biểu đồ doanh thu thuần theo từng ngày (Native Query trỏ vào bảng transactions)
    // Dùng COUNT(DISTINCT booking_id) để đếm số lượng vé có giao dịch trong ngày đó
    @Query(value = "SELECT DATE(created_at) as reportDate, " +
            "SUM(CASE WHEN payment_method = 'VNPAY' THEN amount WHEN payment_method = 'VNPAY_REFUND' THEN -amount ELSE 0 END) as revenue, " +
            "COUNT(DISTINCT booking_id) as bookingCount " +
            "FROM transactions " +
            "WHERE status = 'SUCCESS' AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY reportDate ASC", nativeQuery = true)
    List<DailyRevenueResponse> getDailyRevenueChart(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}


