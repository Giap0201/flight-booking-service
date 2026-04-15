package com.utc.flight_booking_service.booking.service;

import com.utc.flight_booking_service.booking.entity.Booking;
import com.utc.flight_booking_service.booking.enums.BookingStatus;
import com.utc.flight_booking_service.booking.request.AdminBookingSearchRequest;
import com.utc.flight_booking_service.booking.request.BookingRequest;
import com.utc.flight_booking_service.booking.request.BookingSearchRequest;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingDetailResponse;
import com.utc.flight_booking_service.booking.response.admin.AdminBookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.client.BookingCreatedResponse;
import com.utc.flight_booking_service.booking.response.client.BookingDetailResponse;
import com.utc.flight_booking_service.booking.response.client.BookingSummaryResponse;
import com.utc.flight_booking_service.booking.response.share.ETicketEmailModel;
import com.utc.flight_booking_service.common.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingService {
    //Tao ra mot booking moi
    BookingCreatedResponse createBooking(BookingRequest request);

    // Lay booking theo id
    BookingDetailResponse getBookingById(UUID id);

    // Lay danh sach ve co trang thai gi do
    List<Booking> getExpiredBookingsByStatus(BookingStatus bookingStatus);

    // Huy ve het han
    void cancelSingleBookingBySystem(UUID bookingId);

    // Xoa booking da huy trong 30 ngay
    int deleteByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime createdAt);

    Booking getBookingEntityByPnr(String pnrCode);

    Booking getBookingEntityById(UUID id);

    // Cap nhap trang thai booking
    void updateBookingStatus(UUID id, BookingStatus status);

    // Ham tao ve khi thanh toan thanh cong
    void issueTicketsForBooking(UUID bookingId);
    List<ETicketEmailModel> getTicketsByPnrCode(String pnrCode);
    List<ETicketEmailModel> getTicketsByBookingId(UUID bookingId);
    // Khach hang tra cuu booking thong qua ma pnr va email
    BookingDetailResponse getBookingClientByPnrAndContactEmail(BookingSearchRequest request);

    PageResponse<BookingSummaryResponse> getMyBookings(String filter, int page, int size);

    //Khach tu huy ve khi chua thanh toan
    void cancelUnpaidBooking(UUID bookingId);

    // Ham tim kiem theo nhieu tieu chi danh cho admin
    PageResponse<AdminBookingSummaryResponse> searchBookingsForAdmin(AdminBookingSearchRequest request, int page, int size);

    // Lay thong tin booking danh cho admin (ca transaction)
    AdminBookingDetailResponse getBookingDetailsForAdmin(UUID id);

    void forceCancelAndRefund(UUID bookingId);

    void resendBookingEmail(UUID bookingId);

}
