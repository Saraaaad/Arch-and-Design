package org.example.tourism.availability;

import org.example.tourism.booking.Booking;
import org.example.tourism.booking.BookingRepository;
import org.example.tourism.catalog.roomtype.RoomType;
import org.example.tourism.catalog.roomtype.RoomTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// DESIGN PATTERN: STRATEGY - Using PricingStrategyContext
import org.example.tourism.pricing.PricingStrategyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityServiceImpl implements AvailabilityService {

    private final RoomTypeRepository roomTypeRepository;
    private final PricingService pricingService;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponseDto checkAvailability(Long hotelId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, Integer guests) {
        log.info("Checking availability for hotelId: {}, roomTypeId: {}, dates: {} to {}, guests: {}",
                hotelId, roomTypeId, checkIn, checkOut, guests);

        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + roomTypeId));

        if (!roomType.getHotel().getId().equals(hotelId)) {
            throw new IllegalArgumentException("RoomType does not belong to the specified Hotel");
        }

        if (roomType.getCapacity() < guests) {
            throw new IllegalArgumentException("Room capacity insufficient for " + guests + " guests");
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                roomTypeId, checkIn, checkOut);

        boolean available = overlappingBookings.isEmpty();

        log.info("Availability result: {}, found {} overlapping bookings", available, overlappingBookings.size());

        AvailabilityResponseDto response = new AvailabilityResponseDto();
        response.setHotelId(hotelId);
        response.setRoomTypeId(roomTypeId);
        response.setCheckInDate(checkIn);
        response.setCheckOutDate(checkOut);
        response.setAvailable(available);

        // DESIGN PATTERN: STRATEGY
        // The pricing strategy is selected dynamically based on dates
        // This allows flexible pricing without changing this service
        response.setTotalPrice(pricingService.calculateTotalPrice(roomType.getBasePrice(), checkIn, checkOut));

        return response;
    }
}