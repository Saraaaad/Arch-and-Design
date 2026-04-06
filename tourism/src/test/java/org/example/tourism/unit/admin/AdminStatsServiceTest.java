package org.example.tourism.unit.admin;

import org.example.tourism.admin.AdminStatsService;
import org.example.tourism.admin.DashboardStatsDto;
import org.example.tourism.admin.TotalStatsDto;
import org.example.tourism.booking.*;
import org.example.tourism.booking.BookingRepository;
import org.example.tourism.booking.BookingStatus;
import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.hotel.HotelRepository;
import org.example.tourism.payment.PaymentRepository;
import org.example.tourism.payment.PaymentStatus;
import org.example.tourism.security.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminStatsService adminStatsService;

    @Test
    void getTotalStats_ShouldCalculateCorrectly_WhenDataExists() {
        // Given - Mock the COUNT methods that your service now uses
        when(userRepository.count()).thenReturn(10L);
        when(hotelRepository.count()).thenReturn(5L);

        // IMPORTANT: Mock the new repository methods your service is calling
        when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(2L);
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(1L);
        when(bookingRepository.countByStatus(BookingStatus.CANCELLED)).thenReturn(1L);

        when(paymentRepository.sumAmountByStatus(PaymentStatus.COMPLETED)).thenReturn(300L);

        // When
        DashboardStatsDto dashboardStats = adminStatsService.getDashboardStats();
        TotalStatsDto result = dashboardStats.getTotalStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalUsers()).isEqualTo(10L);
        assertThat(result.getTotalHotels()).isEqualTo(5L);
        assertThat(result.getTotalBookings()).isEqualTo(2L);
        assertThat(result.getTotalRevenue()).isEqualTo(300L);
        assertThat(result.getPendingBookings()).isEqualTo(1L);
        assertThat(result.getConfirmedBookings()).isEqualTo(2L);
        assertThat(result.getCancelledBookings()).isEqualTo(1L);
    }

    @Test
    void getTotalStats_ShouldReturnZero_WhenNoData() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(hotelRepository.count()).thenReturn(0L);

        when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(0L);
        when(bookingRepository.countByStatus(BookingStatus.PENDING)).thenReturn(0L);
        when(bookingRepository.countByStatus(BookingStatus.CANCELLED)).thenReturn(0L);

        when(paymentRepository.sumAmountByStatus(PaymentStatus.COMPLETED)).thenReturn(0L);

        // When
        DashboardStatsDto dashboardStats = adminStatsService.getDashboardStats();
        TotalStatsDto result = dashboardStats.getTotalStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalUsers()).isZero();
        assertThat(result.getTotalHotels()).isZero();
        assertThat(result.getTotalBookings()).isZero();
        assertThat(result.getTotalRevenue()).isZero();
        assertThat(result.getPendingBookings()).isZero();
        assertThat(result.getConfirmedBookings()).isZero();
        assertThat(result.getCancelledBookings()).isZero();
    }

    @Test
    void getMonthlyStats_ShouldReturnLast6Months() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Booking bookingThisMonth = createBooking(1L, BookingStatus.CONFIRMED, new BigDecimal("100.00"));
        bookingThisMonth.setCreatedAt(now.minusDays(5));

        Booking bookingLastMonth = createBooking(2L, BookingStatus.CONFIRMED, new BigDecimal("200.00"));
        bookingLastMonth.setCreatedAt(now.minusDays(35));

        when(bookingRepository.findAll()).thenReturn(Arrays.asList(bookingThisMonth, bookingLastMonth));

        // When
        DashboardStatsDto dashboardStats = adminStatsService.getDashboardStats();
        var result = dashboardStats.getMonthlyStats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(6); // Last 6 months
        // }
    }

    private Booking createBooking(Long id, BookingStatus status, BigDecimal price) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStatus(status);
        booking.setTotalPrice(price);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setCheckInDate(LocalDate.now().plusDays(10));
        booking.setCheckOutDate(LocalDate.now().plusDays(12));
        booking.setHotelId(1L);
        return booking;
    }
    @Test
    void getTopHotels_ShouldReturnTop5_WhenMultipleHotelsExist() {
        // Given
        Hotel hotel1 = createHotel(1L, "Hotel A");
        Hotel hotel2 = createHotel(2L, "Hotel B");
        Hotel hotel3 = createHotel(3L, "Hotel C");

        when(hotelRepository.findById(1L)).thenReturn(java.util.Optional.of(hotel1));
        when(hotelRepository.findById(2L)).thenReturn(java.util.Optional.of(hotel2));
        when(hotelRepository.findById(3L)).thenReturn(java.util.Optional.of(hotel3));

        // Create bookings for hotel1 (5 bookings)
        List<Booking> bookings = Arrays.asList(
                createBookingWithHotel(1L, 1L, BookingStatus.CONFIRMED, new BigDecimal("100.00")),
                createBookingWithHotel(2L, 1L, BookingStatus.CONFIRMED, new BigDecimal("150.00")),
                createBookingWithHotel(3L, 1L, BookingStatus.CONFIRMED, new BigDecimal("200.00")),
                createBookingWithHotel(4L, 1L, BookingStatus.CONFIRMED, new BigDecimal("120.00")),
                createBookingWithHotel(5L, 1L, BookingStatus.CONFIRMED, new BigDecimal("180.00")),
                // hotel2 (3 bookings)
                createBookingWithHotel(6L, 2L, BookingStatus.CONFIRMED, new BigDecimal("90.00")),
                createBookingWithHotel(7L, 2L, BookingStatus.CONFIRMED, new BigDecimal("110.00")),
                createBookingWithHotel(8L, 2L, BookingStatus.CONFIRMED, new BigDecimal("130.00")),
                // hotel3 (1 booking)
                createBookingWithHotel(9L, 3L, BookingStatus.CONFIRMED, new BigDecimal("80.00"))
        );

        when(bookingRepository.findAll()).thenReturn(bookings);

        // When
        DashboardStatsDto dashboardStats = adminStatsService.getDashboardStats();
        var result = dashboardStats.getTopHotels();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getHotelName()).isEqualTo("Hotel A");
        assertThat(result.get(0).getBookingCount()).isEqualTo(5);
    }

    private Booking createBookingWithHotel(Long id, Long hotelId, BookingStatus status, BigDecimal price) {
        Booking booking = createBooking(id, status, price);
        booking.setHotelId(hotelId);
        return booking;
    }
    private Hotel createHotel(Long id, String name) {
        Hotel hotel = new Hotel();
        hotel.setId(id);
        hotel.setName(name);
        hotel.setTotalRooms(100);
        return hotel;
    }

}