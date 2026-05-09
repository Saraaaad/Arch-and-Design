package org.example.tourism.booking;

import org.example.tourism.availability.AvailabilityService;
import org.example.tourism.availability.AvailabilityResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourism.common.BookingAlreadyCancelledException;
import org.example.tourism.common.BookingNotAvailableException;
import org.example.tourism.common.CancellationNotAllowedException;
import org.example.tourism.mapper.DtoMapperFactory;
import org.example.tourism.notification.NotificationService;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // DESIGN PATTERN: FACTORY METHOD
    // Using DtoMapperFactory instead of private mapping methods
    // This eliminates code duplication and centralizes mapping logic
    private final DtoMapperFactory dtoMapperFactory;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto) {
        log.info("Creating booking for roomTypeId: {}, dates: {} to {}",
                bookingRequestDto.getRoomTypeId(),
                bookingRequestDto.getCheckInDate(),
                bookingRequestDto.getCheckOutDate());

        if (bookingRequestDto.getCheckOutDate().isBefore(bookingRequestDto.getCheckInDate()) ||
                bookingRequestDto.getCheckOutDate().isEqual(bookingRequestDto.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        AvailabilityResponseDto availability = availabilityService.checkAvailability(
                bookingRequestDto.getHotelId(),
                bookingRequestDto.getRoomTypeId(),
                bookingRequestDto.getCheckInDate(),
                bookingRequestDto.getCheckOutDate(),
                bookingRequestDto.getGuests() != null ? bookingRequestDto.getGuests() : 1
        );

        if (!availability.isAvailable()) {
            throw new BookingNotAvailableException("Room is not available for the selected dates");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                bookingRequestDto.getRoomTypeId(),
                bookingRequestDto.getCheckInDate(),
                bookingRequestDto.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            throw new BookingNotAvailableException("Room is already booked for these dates");
        }

        Booking booking = new Booking();
        BeanUtils.copyProperties(bookingRequestDto, booking);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(availability.getTotalPrice());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with ID: {}", savedBooking.getId());

        // Using Factory Method instead of private mapToDto method
        return dtoMapperFactory.createBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto confirmBooking(Long bookingId) {
        log.info("Confirming booking with ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled booking");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            log.warn("Booking {} is already confirmed", bookingId);
            return dtoMapperFactory.createBookingResponseDto(booking);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking {} confirmed successfully", bookingId);
        return dtoMapperFactory.createBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Long bookingId) {
        log.info("Cancelling booking with ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Booking {} is already cancelled", bookingId);
            return dtoMapperFactory.createBookingResponseDto(booking);
        }

        LocalDate today = LocalDate.now();
        if (booking.getCheckInDate().minusDays(1).isBefore(today)) {
            throw new CancellationNotAllowedException("Cancellation not allowed within 24 hours of check-in");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking {} cancelled successfully", bookingId);

        try {
            User user = userRepository.findById(booking.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + booking.getUserId()));

            notificationService.sendBookingCancellation(
                    user.getEmail(),
                    booking.getId()
            );
            log.info("Cancellation notification sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send cancellation notification for booking {}: {}", bookingId, e.getMessage());
        }

        return dtoMapperFactory.createBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        return dtoMapperFactory.createBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(dtoMapperFactory::createBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getHotelBookings(Long hotelId) {
        log.info("Fetching all bookings for hotel ID: {}", hotelId);
        return bookingRepository.findByHotelId(hotelId).stream()
                .map(dtoMapperFactory::createBookingResponseDto)
                .collect(Collectors.toList());
    }
}