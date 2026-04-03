package org.example.tourism.booking;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto);
    BookingResponseDto confirmBooking(Long bookingId);
    BookingResponseDto cancelBooking(Long bookingId);
    BookingResponseDto getBooking(Long bookingId);
    List<BookingResponseDto> getUserBookings(Long userId);
    List<BookingResponseDto> getHotelBookings(Long hotelId);

}