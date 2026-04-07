package org.example.tourism.wishlist;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.hotel.HotelRepository;
import org.example.tourism.catalog.hotel.dto.HotelSearchResponseDto;
import org.example.tourism.common.CannotWishlistOwnHotelException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final HotelRepository hotelRepository;

    @Transactional
    public void addToWishlist(Long userId, Long hotelId) {
        log.info("Adding hotel {} to wishlist for user {}", hotelId, userId);

        // Check if hotel exists
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + hotelId));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndHotelId(userId, hotelId)) {
            throw new IllegalStateException("Hotel already in wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setHotelId(hotelId);

        wishlistRepository.save(wishlist);
        log.info("Hotel {} added to wishlist for user {}", hotelId, userId);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long hotelId) {
        log.info("Removing hotel {} from wishlist for user {}", hotelId, userId);

        Wishlist wishlist = wishlistRepository.findByUserIdAndHotelId(userId, hotelId)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found in wishlist"));

        wishlistRepository.delete(wishlist);
        log.info("Hotel {} removed from wishlist for user {}", hotelId, userId);
    }

    @Transactional(readOnly = true)
    public List<HotelSearchResponseDto> getUserWishlist(Long userId) {
        log.info("Fetching wishlist for user {}", userId);

        List<Wishlist> wishlist = wishlistRepository.findByUserId(userId);

        return wishlist.stream()
                .map(w -> hotelRepository.findById(w.getHotelId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::mapToSearchDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WishlistCheckResponse isInWishlist(Long userId, Long hotelId) {
        boolean exists = wishlistRepository.existsByUserIdAndHotelId(userId, hotelId);

        return WishlistCheckResponse.builder()
                .inWishlist(exists)
                .wishlistId(exists ?
                        wishlistRepository.findByUserIdAndHotelId(userId, hotelId).get().getId() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public Long getWishlistCount(Long hotelId) {
        return wishlistRepository.countByHotelId(hotelId);
    }

    private HotelSearchResponseDto mapToSearchDto(Hotel hotel) {
        return HotelSearchResponseDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .starRating(hotel.getStarRating())
                .mainImageUrl(hotel.getImageUrls().isEmpty() ? null : hotel.getImageUrls().get(0))
                .averageRating(hotel.getAverageRating())
                .reviewCount(hotel.getReviewCount())
                .build();
    }
}