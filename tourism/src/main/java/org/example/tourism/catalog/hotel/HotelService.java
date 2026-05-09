package org.example.tourism.catalog.hotel;

import org.example.tourism.catalog.hotel.dto.*;
import org.example.tourism.catalog.shared.CityInfoDto;
import org.example.tourism.catalog.shared.ReviewRequestDto;
import org.example.tourism.catalog.shared.ReviewResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface HotelService {
    HotelResponseDto createHotel(HotelRequestDto hotelRequestDto, Long managerId);
    HotelDetailResponseDto getHotelDetail(Long id);
    HotelResponseDto updateHotel(Long id, HotelRequestDto hotelRequestDto);

    Page<HotelSearchResponseDto> searchHotels(HotelSearchCriteria criteria, Pageable pageable);

    List<HotelSearchResponseDto> getFeaturedHotels();
    List<CityInfoDto> getAvailableCities();
    HotelAvailabilityDto checkHotelAvailability(Long hotelId, LocalDate checkIn, LocalDate checkOut, Integer guests);
    HotelResponseDto addHotelImages(Long hotelId, List<String> imageUrls);
    void deleteHotel(Long id);
    ReviewResponseDto addHotelReview(Long hotelId, Long userId, ReviewRequestDto request);
    Page<ReviewResponseDto> getHotelReviews(Long hotelId, Pageable pageable);
    void markReviewHelpful(Long reviewId);
    List<HotelAvailabilityDto> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer guests,
                                                    String city, Integer starRating, BigDecimal maxPrice);
    HotelResponseDto removeHotelImage(Long hotelId, String imageUrl);

}