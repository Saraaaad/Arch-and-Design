package org.example.tourism.booking;

import org.example.tourism.availability.AvailabilityService;
import org.example.tourism.availability.AvailabilityResponseDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;

    @Override
    @Transactional
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
            throw new IllegalStateException("Room is not available for the selected dates");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                bookingRequestDto.getRoomTypeId(),
                bookingRequestDto.getCheckInDate(),
                bookingRequestDto.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Room is already booked for these dates");
        }

        Booking booking = new Booking();
        BeanUtils.copyProperties(bookingRequestDto, booking);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(availability.getTotalPrice());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with ID: {}", savedBooking.getId());

        return mapToDto(savedBooking);
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
            return mapToDto(booking);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking {} confirmed successfully", bookingId);
        return mapToDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(Long bookingId) {
        log.info("Cancelling booking with ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Booking {} is already cancelled", bookingId);
            return mapToDto(booking);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking {} cancelled successfully", bookingId);
        return mapToDto(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        return mapToDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private BookingResponseDto mapToDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        BeanUtils.copyProperties(booking, dto);
        return dto;
    }
}