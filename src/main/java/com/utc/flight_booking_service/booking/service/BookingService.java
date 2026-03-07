package com.utc.flight_booking_service.booking.service;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.request.BookingSearchRequest;
import com.utc.flight_booking_service.booking.response.BookingDetailsResponse;
import com.utc.flight_booking_service.booking.response.BookingResponse;
import com.utc.flight_booking_service.booking.response.BookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.ClientETicketResponse;
import com.utc.flight_booking_service.booking.response.page.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    //Tao ra mot booking moi
    BookingResponse createBooking(BookingRequest request);

    // Lay booking theo id
    BookingResponse getBookingById(UUID id);

    // Tu dong huy booking khi chua thanh toan sau 15p
    void cancelExpiredBookings();

    // Xoa booking da huy trong 30 ngay
    int deleteByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAt);


    Booking getBookingEntityByPnr(String pnrCode);

    Booking getBookingEntityById(UUID id);

    // Cap nhap trang thai booking
    void updateBookingStatus(UUID id, BookingStatus status);

    // Ham tao ve khi thanh toan thanh cong
    void issueTicketsForBooking(UUID bookingId);

    // Lay danh sach ve xuat pdf
    List<ClientETicketResponse> getTicketsByBookingId(UUID bookingId);

    // Khach hang tra cuu booking thong qua ma pnr va email
    BookingDetailsResponse getBookingClientByPnrAndContactEmail(BookingSearchRequest request);

    // Tra cuu lich su dat ve
    PageResponse<BookingSummaryResponse> getMyBookings(String filter, int page, int size);

    //Khach tu huy ve khi chua thanh toan
    void cancelUnpaidBooking(UUID bookingId);

}
