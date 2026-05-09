package org.example.tourism.mapper;

import org.example.tourism.booking.Booking;
import org.example.tourism.booking.BookingResponseDto;
import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.hotel.dto.HotelDetailResponseDto;
import org.example.tourism.catalog.hotel.dto.HotelResponseDto;
import org.example.tourism.catalog.hotel.dto.HotelSearchResponseDto;
import org.example.tourism.catalog.roomtype.RoomType;
import org.example.tourism.catalog.roomtype.dto.RoomTypeResponseDto;
import org.example.tourism.catalog.roomtype.dto.RoomTypeSummaryDto;
import org.example.tourism.payment.Payment;
import org.example.tourism.payment.PaymentResponseDto;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: FACTORY METHOD
 *
 * This factory centralizes all DTO mapping logic in one place.
 * Benefits:
 * 1. Single Responsibility - mapping logic is isolated
 * 2. Easy to maintain - changes to mapping affect only this class
 * 3. Reusability - same mapping logic used across services
 * 4. Consistency - ensures all DTOs are created the same way
 *
 * Instead of each service having its own mapping methods (which was causing
 * code duplication), this factory provides a unified way to create DTOs.
 */
@Component
public class DtoMapperFactory {

    // Factory method for HotelResponseDto
    public HotelResponseDto createHotelResponseDto(Hotel hotel) {
        HotelResponseDto dto = new HotelResponseDto();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setCity(hotel.getCity());
        dto.setAddress(hotel.getAddress());
        dto.setStarRating(hotel.getStarRating());
        dto.setCountry(hotel.getCountry());
        dto.setMainImageUrl(hotel.getImageUrls().isEmpty() ? null : hotel.getImageUrls().get(0));
        dto.setAverageRating(hotel.getAverageRating());
        dto.setFeatured(hotel.getFeatured());
        return dto;
    }

    // Factory method for HotelSearchResponseDto
    public HotelSearchResponseDto createHotelSearchDto(Hotel hotel) {
        java.math.BigDecimal minPrice = hotel.getRoomTypes().stream()
                .map(RoomType::getBasePrice)
                .min(java.math.BigDecimal::compareTo)
                .orElse(java.math.BigDecimal.ZERO);

        return HotelSearchResponseDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .starRating(hotel.getStarRating())
                .mainImageUrl(hotel.getImageUrls().isEmpty() ? null : hotel.getImageUrls().get(0))
                .averageRating(hotel.getAverageRating() != null ? hotel.getAverageRating() : 0.0)
                .reviewCount(hotel.getReviewCount() != null ? hotel.getReviewCount() : 0)
                .priceFrom(minPrice)
                .available(true)
                .build();
    }

    // Factory method for BookingResponseDto
    public BookingResponseDto createBookingResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setHotelId(booking.getHotelId());
        dto.setRoomTypeId(booking.getRoomTypeId());
        dto.setUserId(booking.getUserId());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());
        dto.setGuests(booking.getGuests());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }

    // Factory method for PaymentResponseDto
    public PaymentResponseDto createPaymentResponseDto(Payment payment) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(payment.getId());
        dto.setBookingId(payment.getBookingId());
        dto.setAmount(payment.getAmount());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "CREDIT_CARD");
        dto.setRefundedAmount(payment.getRefundedAmount() != null ? payment.getRefundedAmount() : java.math.BigDecimal.ZERO);
        return dto;
    }

    // Factory method for RoomTypeResponseDto
    public RoomTypeResponseDto createRoomTypeResponseDto(RoomType roomType) {
        RoomTypeResponseDto dto = new RoomTypeResponseDto();
        dto.setId(roomType.getId());
        dto.setName(roomType.getName());
        dto.setDescription(roomType.getDescription());
        dto.setCapacity(roomType.getCapacity());
        dto.setBasePrice(roomType.getBasePrice());
        dto.setBedType(roomType.getBedType());
        dto.setRoomSize(roomType.getRoomSize());
        dto.setAmenities(roomType.getAmenities());
        dto.setImageUrls(roomType.getImageUrls() != null ? roomType.getImageUrls() : new java.util.ArrayList<>());
        dto.setTotalRooms(roomType.getTotalRooms());
        dto.setSmokingAllowed(roomType.getSmokingAllowed());
        dto.setView(roomType.getView());
        dto.setBoardBasis(roomType.getBoardBasis());
        dto.setRefundable(roomType.getRefundable());
        dto.setCancellationPolicy(roomType.getCancellationPolicy());
        dto.setHotelId(roomType.getHotel().getId());
        return dto;
    }

    // Factory method for RoomTypeSummaryDto
    public RoomTypeSummaryDto createRoomTypeSummaryDto(RoomType roomType) {
        return RoomTypeSummaryDto.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .capacity(roomType.getCapacity())
                .basePrice(roomType.getBasePrice())
                .bedType(roomType.getBedType())
                .amenities(roomType.getAmenities())
                .imageUrl(roomType.getImageUrls().isEmpty() ? null : roomType.getImageUrls().get(0))
                .available(true)
                .build();
    }

    // Factory method for HotelDetailResponseDto
    public HotelDetailResponseDto createHotelDetailDto(Hotel hotel,
                                                       java.util.List<RoomTypeSummaryDto> roomTypeSummaries,
                                                       Long wishlistCount) {
        return HotelDetailResponseDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .address(hotel.getAddress())
                .zipCode(hotel.getZipCode())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .starRating(hotel.getStarRating())
                .contactEmail(hotel.getContactEmail())
                .contactPhone(hotel.getContactPhone())
                .website(hotel.getWebsite())
                .checkInTime(hotel.getCheckInTime())
                .checkOutTime(hotel.getCheckOutTime())
                .totalRooms(hotel.getTotalRooms())
                .childrenAllowed(hotel.getChildrenAllowed())
                .petsAllowed(hotel.getPetsAllowed())
                .petPolicy(hotel.getPetPolicy())
                .smokingAllowed(hotel.getSmokingAllowed())
                .smokingPolicy(hotel.getSmokingPolicy())
                .cancellationPolicy(hotel.getCancellationPolicy())
                .paymentPolicy(hotel.getPaymentPolicy())
                .checkInInstructions(hotel.getCheckInInstructions())
                .amenities(new java.util.ArrayList<>(hotel.getAmenities()))
                .imageUrls(hotel.getImageUrls())
                .spokenLanguages(hotel.getSpokenLanguages())
                .nearbyAttractions(hotel.getNearbyAttractions())
                .roomTypes(roomTypeSummaries)
                .averageRating(hotel.getAverageRating())
                .reviewCount(hotel.getReviewCount())
                .featured(hotel.getFeatured())
                .featuredOrder(hotel.getFeaturedOrder())
                .dealTag(hotel.getDealTag())
                .wishlistCount(wishlistCount)
                .build();
    }
}