package org.example.tourism.unit.catalog.hotel;

import org.example.tourism.booking.BookingRepository;
import org.example.tourism.catalog.hotel.*;
import org.example.tourism.catalog.hotel.dto.*;
import org.example.tourism.catalog.roomtype.RoomTypeRepository;
import org.example.tourism.catalog.shared.ReviewRequestDto;
import org.example.tourism.catalog.shared.ReviewResponseDto;
import org.example.tourism.security.User;
import org.example.tourism.security.UserRepository;
import org.example.tourism.wishlist.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelReviewRepository reviewRepository;

    @Mock
    private WishlistService wishlistService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel hotel;
    private HotelRequestDto hotelRequest;
    private User testUser;
    private ReviewRequestDto reviewRequest;
    private HotelReview hotelReview;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setCity("Test City");
        hotel.setCountry("Test Country");
        hotel.setAddress("123 Test St");
        hotel.setStarRating(4);
        hotel.setDescription("A wonderful test hotel");
        hotel.setAmenities(Set.of("WiFi", "Pool"));
        hotel.setImageUrls(new ArrayList<>());
        hotel.setTotalRooms(100);
        hotel.setAverageRating(4.5);
        hotel.setReviewCount(10);
        hotel.setRoomTypes(new ArrayList<>());

        hotelRequest = new HotelRequestDto();
        hotelRequest.setName("Test Hotel");
        hotelRequest.setCity("Test City");
        hotelRequest.setCountry("Test Country");
        hotelRequest.setAddress("123 Test St");
        hotelRequest.setStarRating(4);
        hotelRequest.setDescription("A wonderful test hotel");
        hotelRequest.setAmenities(new ArrayList<>());
        hotelRequest.setImageUrls(new ArrayList<>());

        testUser = new User();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        reviewRequest = new ReviewRequestDto();
        reviewRequest.setRating(5);
        reviewRequest.setTitle("Great hotel!");
        reviewRequest.setComment("Really enjoyed my stay.");
        reviewRequest.setStayDate(LocalDate.now().minusDays(10));

        hotelReview = new HotelReview();
        hotelReview.setId(1L);
        hotelReview.setHotel(hotel);
        hotelReview.setUserId(100L);
        hotelReview.setRating(5);
        hotelReview.setTitle("Great hotel!");
        hotelReview.setComment("Really enjoyed my stay.");
        hotelReview.setHelpfulCount(0);
        hotelReview.setVerifiedBooking(true);
    }

    @Test
    void createHotel_ShouldSucceed_WhenValidRequest() {
        // Given
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        // When
        HotelResponseDto result = hotelService.createHotel(hotelRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Hotel");
        assertThat(result.getCity()).isEqualTo("Test City");
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void getHotelDetail_ShouldReturnHotel_WhenExists() {
        // Given - FIXED: Use findByIdWithRoomTypes instead of findById
        when(hotelRepository.findByIdWithRoomTypes(1L)).thenReturn(Optional.of(hotel));
        when(wishlistService.getWishlistCount(1L)).thenReturn(5L);

        // When
        HotelDetailResponseDto result = hotelService.getHotelDetail(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Hotel");
        assertThat(result.getWishlistCount()).isEqualTo(5L);

        verify(hotelRepository, times(1)).findByIdWithRoomTypes(1L);
        verify(wishlistService, times(1)).getWishlistCount(1L);
    }

    @Test
    void getHotelDetail_ShouldThrowException_WhenNotFound() {
        // Given - FIXED: Use findByIdWithRoomTypes instead of findById
        when(hotelRepository.findByIdWithRoomTypes(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hotelService.getHotelDetail(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("Hotel not found");

        verify(hotelRepository, times(1)).findByIdWithRoomTypes(999L);
        verify(wishlistService, never()).getWishlistCount(anyLong());
    }

    @Test
    void updateHotel_ShouldSucceed_WhenValidRequest() {
        // Given
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        // When
        HotelResponseDto result = hotelService.updateHotel(1L, hotelRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Hotel");
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void updateHotel_ShouldThrowException_WhenNotFound() {
        // Given
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hotelService.updateHotel(999L, hotelRequest))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void deleteHotel_ShouldSucceed_WhenExists() {
        // Given
        when(hotelRepository.existsById(1L)).thenReturn(true);
        doNothing().when(hotelRepository).deleteById(1L);

        // When
        hotelService.deleteHotel(1L);

        // Then
        verify(hotelRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteHotel_ShouldThrowException_WhenNotFound() {
        // Given
        when(hotelRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> hotelService.deleteHotel(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void searchHotels_ShouldReturnResults_WhenCriteriaMatch() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hotel> hotelPage = new PageImpl<>(List.of(hotel));

        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .city("Test City")
                .build();

        when(hotelRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(hotelPage);

        // When
        Page<HotelSearchResponseDto> result = hotelService.searchHotels(criteria, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Hotel");
        verify(hotelRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getFeaturedHotels_ShouldReturnFeaturedHotels() {
        // Given
        when(hotelRepository.findByFeaturedTrue()).thenReturn(List.of(hotel));

        // When
        List<HotelSearchResponseDto> result = hotelService.getFeaturedHotels();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Hotel");
        verify(hotelRepository, times(1)).findByFeaturedTrue();
    }

    @Test
    void addHotelReview_ShouldSucceed_WhenValidRequest() {
        // Given
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(reviewRepository.existsByHotelIdAndUserId(1L, 100L)).thenReturn(false);
        when(reviewRepository.save(any(HotelReview.class))).thenReturn(hotelReview);
        when(reviewRepository.getAverageRatingForHotel(1L)).thenReturn(4.8);
        when(reviewRepository.getReviewCountForHotel(1L)).thenReturn(11);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        // When
        ReviewResponseDto result = hotelService.addHotelReview(1L, 100L, reviewRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getTitle()).isEqualTo("Great hotel!");
        verify(reviewRepository, times(1)).save(any(HotelReview.class));
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void addHotelReview_ShouldThrowException_WhenHotelNotFound() {
        // Given
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hotelService.addHotelReview(999L, 100L, reviewRequest))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void addHotelReview_ShouldThrowException_WhenUserAlreadyReviewed() {
        // Given
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(reviewRepository.existsByHotelIdAndUserId(1L, 100L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> hotelService.addHotelReview(1L, 100L, reviewRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already reviewed");
    }

    @Test
    void getHotelReviews_ShouldReturnReviews_WhenExists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<HotelReview> reviewPage = new PageImpl<>(List.of(hotelReview));

        when(reviewRepository.findByHotelId(eq(1L), eq(pageable))).thenReturn(reviewPage);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        // When
        Page<ReviewResponseDto> result = hotelService.getHotelReviews(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRating()).isEqualTo(5);
        assertThat(result.getContent().get(0).getUserName()).isEqualTo("testuser");

        verify(reviewRepository, times(1)).findByHotelId(eq(1L), eq(pageable));
        verify(userRepository, times(1)).findById(100L);
    }

    @Test
    void getHotelReviews_ShouldReturnAnonymousName_WhenUserNotFound() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<HotelReview> reviewPage = new PageImpl<>(List.of(hotelReview));

        when(reviewRepository.findByHotelId(eq(1L), eq(pageable))).thenReturn(reviewPage);
        when(userRepository.findById(100L)).thenReturn(Optional.empty());

        // When
        Page<ReviewResponseDto> result = hotelService.getHotelReviews(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserName()).isEqualTo("User 100");
    }

    @Test
    void markReviewHelpful_ShouldIncrementCount_WhenReviewExists() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(hotelReview));
        when(reviewRepository.save(any(HotelReview.class))).thenReturn(hotelReview);

        // When
        hotelService.markReviewHelpful(1L);

        // Then
        assertThat(hotelReview.getHelpfulCount()).isEqualTo(1);
        verify(reviewRepository, times(1)).save(hotelReview);
    }

    @Test
    void markReviewHelpful_ShouldThrowException_WhenReviewNotFound() {
        // Given
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hotelService.markReviewHelpful(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void addHotelImages_ShouldAddImages_WhenHotelExists() {
        // Given
        List<String> imageUrls = List.of("image1.jpg", "image2.jpg");
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        // When
        HotelResponseDto result = hotelService.addHotelImages(1L, imageUrls);

        // Then
        assertThat(result).isNotNull();
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void addHotelImages_ShouldThrowException_WhenHotelNotFound() {
        // Given
        List<String> imageUrls = List.of("image1.jpg");
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hotelService.addHotelImages(999L, imageUrls))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void removeHotelImage_ShouldRemoveImage_WhenExists() {
        // Given
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("image1.jpg");
        imageUrls.add("image2.jpg");
        hotel.setImageUrls(imageUrls);

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        // When
        HotelResponseDto result = hotelService.removeHotelImage(1L, "image1.jpg");

        // Then
        assertThat(result).isNotNull();
        assertThat(hotel.getImageUrls()).hasSize(1);
        assertThat(hotel.getImageUrls().get(0)).isEqualTo("image2.jpg");
        verify(hotelRepository, times(1)).save(hotel);
    }

    @Test
    void removeHotelImage_ShouldThrowException_WhenImageNotFound() {
        // Given
        hotel.setImageUrls(new ArrayList<>());
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        // When/Then
        assertThatThrownBy(() -> hotelService.removeHotelImage(1L, "nonexistent.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image URL not found");
    }

    @Test
    void removeHotelImage_ShouldThrowException_WhenHotelNotFound() {
        // Given
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hotelService.removeHotelImage(999L, "image.jpg"))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }
}