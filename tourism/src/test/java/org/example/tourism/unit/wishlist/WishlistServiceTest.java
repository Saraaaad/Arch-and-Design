package org.example.tourism.unit.wishlist;

import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.hotel.HotelRepository;
import org.example.tourism.catalog.hotel.dto.HotelSearchResponseDto;
import org.example.tourism.wishlist.Wishlist;
import org.example.tourism.wishlist.WishlistCheckResponse;
import org.example.tourism.wishlist.WishlistRepository;
import org.example.tourism.wishlist.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private WishlistService wishlistService;

    private Wishlist wishlist;
    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setCity("Test City");

        wishlist = new Wishlist();
        wishlist.setId(1L);
        wishlist.setUserId(100L);
        wishlist.setHotelId(1L);
    }


    @Test
    void addToWishlist_ShouldThrowException_WhenHotelNotFound() {
        // Given
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> wishlistService.addToWishlist(100L, 999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("Hotel not found");
    }

    @Test
    void addToWishlist_ShouldThrowException_WhenAlreadyInWishlist() {
        // Given
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(wishlistRepository.existsByUserIdAndHotelId(100L, 1L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> wishlistService.addToWishlist(100L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already in wishlist");
    }

    @Test
    void removeFromWishlist_ShouldSucceed_WhenValidRequest() {
        // Given
        when(wishlistRepository.findByUserIdAndHotelId(100L, 1L)).thenReturn(Optional.of(wishlist));

        // When
        wishlistService.removeFromWishlist(100L, 1L);

        // Then
        verify(wishlistRepository, times(1)).delete(wishlist);
    }

    @Test
    void removeFromWishlist_ShouldThrowException_WhenNotInWishlist() {
        // Given
        when(wishlistRepository.findByUserIdAndHotelId(100L, 999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> wishlistService.removeFromWishlist(100L, 999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("Hotel not found in wishlist");
    }

    @Test
    void getUserWishlist_ShouldReturnHotels_WhenUserHasWishlist() {
        // Given
        when(wishlistRepository.findByUserId(100L)).thenReturn(List.of(wishlist));
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        // When
        List<HotelSearchResponseDto> results = wishlistService.getUserWishlist(100L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Test Hotel");
    }

    @Test
    void isInWishlist_ShouldReturnTrue_WhenHotelInWishlist() {
        // Given
        when(wishlistRepository.existsByUserIdAndHotelId(100L, 1L)).thenReturn(true);
        when(wishlistRepository.findByUserIdAndHotelId(100L, 1L)).thenReturn(Optional.of(wishlist));

        // When
        WishlistCheckResponse result = wishlistService.isInWishlist(100L, 1L);

        // Then
        assertThat(result.isInWishlist()).isTrue();
        assertThat(result.getWishlistId()).isEqualTo(1L);
    }

    @Test
    void isInWishlist_ShouldReturnFalse_WhenHotelNotInWishlist() {
        // Given
        when(wishlistRepository.existsByUserIdAndHotelId(100L, 999L)).thenReturn(false);

        // When
        WishlistCheckResponse result = wishlistService.isInWishlist(100L, 999L);

        // Then
        assertThat(result.isInWishlist()).isFalse();
        assertThat(result.getWishlistId()).isNull();
    }

    @Test
    void getWishlistCount_ShouldReturnCorrectCount() {
        // Given
        when(wishlistRepository.countByHotelId(1L)).thenReturn(5L);

        // When
        Long count = wishlistService.getWishlistCount(1L);

        // Then
        assertThat(count).isEqualTo(5L);
    }
}