package org.example.tourism.unit.availability;

import org.example.tourism.availability.AvailabilityResponseDto;
import org.example.tourism.availability.AvailabilityServiceImpl;
import org.example.tourism.availability.PricingService;
import org.example.tourism.booking.Booking;
import org.example.tourism.booking.BookingRepository;
import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.roomtype.RoomType;
import org.example.tourism.catalog.roomtype.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    private RoomType roomType;
    private Hotel hotel;
    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Room");
        roomType.setCapacity(2);
        roomType.setBasePrice(new BigDecimal("150.00"));
        roomType.setHotel(hotel);
        roomType.setQuantity(5);

        checkIn = LocalDate.now().plusDays(10);
        checkOut = LocalDate.now().plusDays(12);
    }

    @Test
    void checkAvailability_ShouldReturnAvailable_WhenNoOverlappingBookings() {
        // Given
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(pricingService.calculateTotalPrice(any(BigDecimal.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("300.00"));

        // When
        AvailabilityResponseDto result = availabilityService.checkAvailability(1L, 1L, checkIn, checkOut, 2);

        // Then
        assertThat(result.isAvailable()).isTrue();
        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal("300.00"));
        assertThat(result.getHotelId()).isEqualTo(1L);
        assertThat(result.getRoomTypeId()).isEqualTo(1L);
    }

    @Test
    void checkAvailability_ShouldReturnUnavailable_WhenOverlappingBookingsExist() {
        // Given
        Booking overlappingBooking = new Booking();
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(overlappingBooking));

        // When
        AvailabilityResponseDto result = availabilityService.checkAvailability(1L, 1L, checkIn, checkOut, 2);

        // Then
        assertThat(result.isAvailable()).isFalse();
    }

    @Test
    void checkAvailability_ShouldThrowException_WhenCheckInAfterCheckOut() {
        // When/Then
        assertThatThrownBy(() -> availabilityService.checkAvailability(1L, 1L, checkOut, checkIn, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-out date must be after check-in date");
    }

    @Test
    void checkAvailability_ShouldThrowException_WhenCheckInEqualsCheckOut() {
        // When/Then
        assertThatThrownBy(() -> availabilityService.checkAvailability(1L, 1L, checkIn, checkIn, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-out date must be after check-in date");
    }

    @Test
    void checkAvailability_ShouldThrowException_WhenRoomTypeNotFound() {
        // Given
        when(roomTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> availabilityService.checkAvailability(1L, 999L, checkIn, checkOut, 2))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("RoomType not found");
    }

    @Test
    void checkAvailability_ShouldThrowException_WhenRoomTypeBelongsToDifferentHotel() {
        // Given
        Hotel differentHotel = new Hotel();
        differentHotel.setId(2L);
        roomType.setHotel(differentHotel);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        // When/Then - Pass hotelId=1L but roomType belongs to hotelId=2L
        assertThatThrownBy(() -> availabilityService.checkAvailability(1L, 1L, checkIn, checkOut, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RoomType does not belong to the specified Hotel");
    }

    @Test
    void checkAvailability_ShouldSucceed_WhenHotelIdMatches() {
        // Given
        Hotel matchingHotel = new Hotel();
        matchingHotel.setId(1L);
        roomType.setHotel(matchingHotel);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(pricingService.calculateTotalPrice(any(BigDecimal.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("300.00"));

        // When - Pass hotelId=1L which matches roomType's hotel
        AvailabilityResponseDto result = availabilityService.checkAvailability(1L, 1L, checkIn, checkOut, 2);

        // Then
        assertThat(result.isAvailable()).isTrue();
    }

    @Test
    void checkAvailability_ShouldThrowException_WhenGuestCapacityExceeded() {
        // Given
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        // When/Then - Request 5 guests but room capacity is 2
        assertThatThrownBy(() -> availabilityService.checkAvailability(1L, 1L, checkIn, checkOut, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Room capacity insufficient");
    }

    @Test
    void checkAvailability_ShouldHandleNullGuests_WithDefaultValue() {
        // Note: Your method requires guests parameter, so this test may not be needed
        // Keeping for completeness but you may want to remove
    }

    @Test
    void checkAvailability_ShouldCalculateTotalPrice_WhenAvailable() {
        // Given
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.findOverlappingBookings(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(pricingService.calculateTotalPrice(any(BigDecimal.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("350.00"));

        // When
        AvailabilityResponseDto result = availabilityService.checkAvailability(1L, 1L, checkIn, checkOut, 2);

        // Then
        assertThat(result.getTotalPrice()).isEqualTo(new BigDecimal("350.00"));
        verify(pricingService, times(1)).calculateTotalPrice(any(BigDecimal.class), any(LocalDate.class), any(LocalDate.class));
    }
}