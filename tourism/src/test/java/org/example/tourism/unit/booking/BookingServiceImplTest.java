package org.example.tourism.unit.booking;

import org.example.tourism.availability.AvailabilityResponseDto;
import org.example.tourism.availability.AvailabilityService;
import org.example.tourism.booking.*;
import org.example.tourism.common.BookingNotAvailableException;
import org.example.tourism.notification.NotificationService;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private BookingRequestDto bookingRequest;
    private AvailabilityResponseDto availabilityResponse;
    private User testUser;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        bookingRequest = new BookingRequestDto();
        bookingRequest.setHotelId(1L);
        bookingRequest.setRoomTypeId(1L);
        bookingRequest.setUserId(100L);
        bookingRequest.setCheckInDate(LocalDate.now().plusDays(10));
        bookingRequest.setCheckOutDate(LocalDate.now().plusDays(12));
        bookingRequest.setGuests(2);

        availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("300.00"));

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setUserId(100L);
        testBooking.setHotelId(1L);
        testBooking.setRoomTypeId(1L);
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setTotalPrice(new BigDecimal("300.00"));
        testBooking.setCheckInDate(LocalDate.now().plusDays(10));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(12));
        testBooking.setGuests(2);
    }

    @Test
    void createBooking_ShouldSucceed_WhenRoomAvailable() {
        // Given
        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        BookingResponseDto result = bookingService.createBooking(bookingRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal("300.00"));

        verify(availabilityService, times(1)).checkAvailability(anyLong(), anyLong(), any(), any(), anyInt());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_ShouldThrowException_WhenRoomNotAvailable() {
        // Given
        availabilityResponse.setAvailable(false);
        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(BookingNotAvailableException.class)
                .hasMessageContaining("Room is not available for the selected dates");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_ShouldThrowException_WhenOverlappingBookingsExist() {
        // Given
        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), any(), any()))
                .thenReturn(List.of(new Booking())); // Existing booking

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(BookingNotAvailableException.class)
                .hasMessageContaining("Room is already booked for these dates");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_ShouldThrowException_WhenCheckOutBeforeCheckIn() {
        // Given
        bookingRequest.setCheckOutDate(LocalDate.now().plusDays(5));
        bookingRequest.setCheckInDate(LocalDate.now().plusDays(10));

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-out date must be after check-in date");
    }

    @Test
    void confirmBooking_ShouldSucceed_WhenBookingIsPending() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // When
        BookingResponseDto result = bookingService.confirmBooking(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void confirmBooking_ShouldThrowException_WhenBookingNotFound() {
        // Given
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> bookingService.confirmBooking(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void confirmBooking_ShouldThrowException_WhenBookingIsCancelled() {
        // Given
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // When/Then
        assertThatThrownBy(() -> bookingService.confirmBooking(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot confirm a cancelled booking");
    }

    @Test
    void cancelBooking_ShouldSucceed_WhenBookingIsPending() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        // When
        BookingResponseDto result = bookingService.cancelBooking(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(notificationService, times(1)).sendBookingCancellation(anyString(), anyLong());
    }

    @Test
    void cancelBooking_ShouldReturnExistingBooking_WhenAlreadyCancelled() {
        // Given
        testBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // When
        BookingResponseDto result = bookingService.cancelBooking(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBooking_ShouldReturnBooking_WhenExists() {
        // Given
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // When
        BookingResponseDto result = bookingService.getBooking(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBooking_ShouldThrowException_WhenNotFound() {
        // Given
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> bookingService.getBooking(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void getUserBookings_ShouldReturnList_WhenUserHasBookings() {
        // Given
        when(bookingRepository.findByUserId(100L)).thenReturn(List.of(testBooking));

        // When
        List<BookingResponseDto> results = bookingService.getUserBookings(100L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo(100L);
    }

    @Test
    void getUserBookings_ShouldReturnEmptyList_WhenUserHasNoBookings() {
        // Given
        when(bookingRepository.findByUserId(100L)).thenReturn(List.of());

        // When
        List<BookingResponseDto> results = bookingService.getUserBookings(100L);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void getHotelBookings_ShouldReturnList_WhenHotelHasBookings() {
        // Given
        when(bookingRepository.findByHotelId(1L)).thenReturn(List.of(testBooking));

        // When
        List<BookingResponseDto> results = bookingService.getHotelBookings(1L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getHotelId()).isEqualTo(1L);
    }
}