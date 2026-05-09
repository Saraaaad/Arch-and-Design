package org.example.tourism.integration.catalog.hotel;

import org.example.tourism.catalog.hotel.*;
import org.example.tourism.catalog.hotel.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class HotelServiceIntegrationTest {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    private HotelRequestDto hotelRequest;

    @BeforeEach
    void setUp() {
        hotelRequest = new HotelRequestDto();
        hotelRequest.setName("Test Hotel");
        hotelRequest.setCity("Test City");
        hotelRequest.setCountry("Test Country");
        hotelRequest.setAddress("123 Test Street");
        hotelRequest.setStarRating(4);
        hotelRequest.setDescription("A wonderful test hotel");
        hotelRequest.setAmenities(new ArrayList<>());
        hotelRequest.setImageUrls(new ArrayList<>());
        hotelRequest.setTotalRooms(100);
        hotelRequest.setCheckInTime("15:00");
        hotelRequest.setCheckOutTime("11:00");
    }

    @Test
    void createHotel_ShouldSucceed_WhenValidRequest() {
        // When
        HotelResponseDto result = hotelService.createHotel(hotelRequest,null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Hotel");
        assertThat(result.getCity()).isEqualTo("Test City");
        assertThat(result.getStarRating()).isEqualTo(4);
    }

    @Test
    void getHotelDetail_ShouldReturnHotel_WhenExists() {
        // Given
        HotelResponseDto created = hotelService.createHotel(hotelRequest,null);

        // When
        var result = hotelService.getHotelDetail(created.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getName()).isEqualTo("Test Hotel");
        assertThat(result.getCity()).isEqualTo("Test City");
    }

    @Test
    void getHotelDetail_ShouldThrowException_WhenNotFound() {
        // When/Then
        assertThatThrownBy(() -> hotelService.getHotelDetail(9999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void updateHotel_ShouldUpdateFields_WhenValidRequest() {
        // Given
        HotelResponseDto created = hotelService.createHotel(hotelRequest,null);

        HotelRequestDto updateRequest = new HotelRequestDto();
        updateRequest.setName("Updated Hotel Name");
        updateRequest.setCity("Updated City");
        updateRequest.setCountry("Updated Country");
        updateRequest.setAddress("456 Updated Street");
        updateRequest.setStarRating(5);
        updateRequest.setDescription("Updated description");
        updateRequest.setAmenities(new ArrayList<>());
        updateRequest.setImageUrls(new ArrayList<>());

        // When
        HotelResponseDto updated = hotelService.updateHotel(created.getId(), updateRequest);

        // Then
        assertThat(updated.getName()).isEqualTo("Updated Hotel Name");
        assertThat(updated.getCity()).isEqualTo("Updated City");
        assertThat(updated.getStarRating()).isEqualTo(5);
    }

    @Test
    void searchHotels_ShouldReturnResults_WhenMatchingCriteria() {
        // Given
        hotelService.createHotel(hotelRequest,null);

        HotelRequestDto secondHotel = new HotelRequestDto();
        secondHotel.setName("Another Hotel");
        secondHotel.setCity("Another City");
        secondHotel.setCountry("Another Country");
        secondHotel.setAddress("789 Another St");
        secondHotel.setStarRating(3);
        secondHotel.setDescription("Another hotel");
        secondHotel.setAmenities(new ArrayList<>());
        secondHotel.setImageUrls(new ArrayList<>());
        hotelService.createHotel(secondHotel,null);

        HotelSearchCriteria criteria = HotelSearchCriteria.builder()
                .city("Test City")
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        var results = hotelService.searchHotels(criteria, pageable);

        // Then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getCity()).isEqualTo("Test City");
    }

    @Test
    void deleteHotel_ShouldRemoveHotel_WhenExists() {
        // Given
        HotelResponseDto created = hotelService.createHotel(hotelRequest,null);

        // When
        hotelService.deleteHotel(created.getId());

        // Then
        assertThat(hotelRepository.findById(created.getId())).isEmpty();
    }
}