package org.example.tourism.integration.booking;

import org.example.tourism.availability.*;
import org.example.tourism.booking.*;
import org.example.tourism.common.BookingNotAvailableException;
import org.example.tourism.common.Role;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AvailabilityService availabilityService;

    private User testUser;
    private BookingRequestDto bookingRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("encoded");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setEnabled(true);
        testUser.setRoles(Set.of(Role.GUEST));
        testUser = userRepository.save(testUser);

        // Create booking request
        bookingRequest = new BookingRequestDto();
        bookingRequest.setHotelId(1L);
        bookingRequest.setRoomTypeId(1L);
        bookingRequest.setUserId(testUser.getId());
        bookingRequest.setCheckInDate(LocalDate.now().plusDays(10));
        bookingRequest.setCheckOutDate(LocalDate.now().plusDays(12));
        bookingRequest.setGuests(2);
    }

    @Test
    void createBooking_ShouldSucceed_WhenRoomAvailable() {
        // Given - Mock availability service
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("300.00"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        // When
        BookingResponseDto result = bookingService.createBooking(bookingRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal("300.00"));
        assertThat(result.getUserId()).isEqualTo(testUser.getId());
    }

    @Test
    void createBooking_ShouldThrowException_WhenRoomNotAvailable() {
        // Given
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(false);

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        // When/Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest))
                .isInstanceOf(BookingNotAvailableException.class)
                .hasMessageContaining("Room is not available for the selected dates");
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
    void getBooking_ShouldReturnBooking_WhenExists() {
        // Given
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("300.00"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        BookingResponseDto created = bookingService.createBooking(bookingRequest);

        // When
        BookingResponseDto found = bookingService.getBooking(created.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void getBooking_ShouldThrowException_WhenNotFound() {
        // When/Then
        assertThatThrownBy(() -> bookingService.getBooking(9999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void confirmBooking_ShouldChangeStatus_WhenBookingIsPending() {
        // Given
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("300.00"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        BookingResponseDto created = bookingService.createBooking(bookingRequest);

        // When
        BookingResponseDto confirmed = bookingService.confirmBooking(created.getId());

        // Then
        assertThat(confirmed.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void confirmBooking_ShouldThrowException_WhenBookingIsCancelled() {
        // Given
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("300.00"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        BookingResponseDto created = bookingService.createBooking(bookingRequest);
        BookingResponseDto cancelled = bookingService.cancelBooking(created.getId());

        // When/Then
        assertThatThrownBy(() -> bookingService.confirmBooking(created.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot confirm a cancelled booking");
    }

    @Test
    void cancelBooking_ShouldChangeStatus_WhenBookingIsPending() {
        // Given
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("300.00"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        BookingResponseDto created = bookingService.createBooking(bookingRequest);

        // When
        BookingResponseDto cancelled = bookingService.cancelBooking(created.getId());

        // Then
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_ShouldReturnExistingBooking_WhenAlreadyCancelled() {
        // Given
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("300.00"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        BookingResponseDto created = bookingService.createBooking(bookingRequest);
        bookingService.cancelBooking(created.getId());

        // When
        BookingResponseDto cancelledAgain = bookingService.cancelBooking(created.getId());

        // Then
        assertThat(cancelledAgain.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void getUserBookings_ShouldReturnAllUserBookings() {
        // Given
        AvailabilityResponseDto availabilityResponse = new AvailabilityResponseDto();
        availabilityResponse.setAvailable(true);
        availabilityResponse.setTotalPrice(new BigDecimal("300.00"));

        when(availabilityService.checkAvailability(anyLong(), anyLong(), any(), any(), anyInt()))
                .thenReturn(availabilityResponse);

        // Create multiple bookings
        bookingService.createBooking(bookingRequest);

        BookingRequestDto secondBooking = new BookingRequestDto();
        secondBooking.setHotelId(2L);
        secondBooking.setRoomTypeId(2L);
        secondBooking.setUserId(testUser.getId());
        secondBooking.setCheckInDate(LocalDate.now().plusDays(15));
        secondBooking.setCheckOutDate(LocalDate.now().plusDays(17));
        secondBooking.setGuests(2);
        bookingService.createBooking(secondBooking);

        // When
        var bookings = bookingService.getUserBookings(testUser.getId());

        // Then
        assertThat(bookings).hasSize(2);
        assertThat(bookings).allMatch(b -> b.getUserId().equals(testUser.getId()));
    }

    @Test
    void getUserBookings_ShouldReturnEmptyList_WhenUserHasNoBookings() {
        // When
        var bookings = bookingService.getUserBookings(testUser.getId());

        // Then
        assertThat(bookings).isEmpty();
    }
}