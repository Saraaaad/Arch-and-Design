package org.example.tourism.integration.catalog.roomtype;

import org.example.tourism.catalog.hotel.*;
import org.example.tourism.catalog.hotel.dto.*;
import org.example.tourism.catalog.roomtype.*;
import org.example.tourism.catalog.roomtype.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RoomTypeServiceIntegrationTest {

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    private HotelResponseDto testHotel;
    private RoomTypeRequestDto roomTypeRequest;

    @BeforeEach
    void setUp() {
        // Create a hotel first
        HotelRequestDto hotelRequest = new HotelRequestDto();
        hotelRequest.setName("Test Hotel for Rooms");
        hotelRequest.setCity("Room City");
        hotelRequest.setCountry("Room Country");
        hotelRequest.setAddress("123 Room St");
        hotelRequest.setStarRating(4);
        hotelRequest.setDescription("Hotel for room testing");
        hotelRequest.setAmenities(new ArrayList<>());
        hotelRequest.setImageUrls(new ArrayList<>());

        testHotel = hotelService.createHotel(hotelRequest);

        // Create room type request
        roomTypeRequest = new RoomTypeRequestDto();
        roomTypeRequest.setName("Deluxe Suite");
        roomTypeRequest.setHotelId(testHotel.getId());
        roomTypeRequest.setCapacity(2);
        roomTypeRequest.setBasePrice(new BigDecimal("250.00"));
        roomTypeRequest.setBedType("King");
        roomTypeRequest.setRoomSize(45);
        roomTypeRequest.setAmenities(new ArrayList<>());
        roomTypeRequest.setImageUrls(new ArrayList<>());
        roomTypeRequest.setTotalRooms(10);
        roomTypeRequest.setSmokingAllowed(false);
        roomTypeRequest.setView("City View");
        roomTypeRequest.setBoardBasis("Breakfast Included");
        roomTypeRequest.setRefundable(true);
    }

    @Test
    void createRoomType_ShouldSucceed_WhenValidRequest() {
        // When
        RoomTypeResponseDto result = roomTypeService.createRoomType(roomTypeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Deluxe Suite");
        assertThat(result.getCapacity()).isEqualTo(2);
        assertThat(result.getBasePrice()).isEqualTo(new BigDecimal("250.00"));
        assertThat(result.getHotelId()).isEqualTo(testHotel.getId());
    }

    @Test
    void getRoomType_ShouldReturnRoomType_WhenExists() {
        // Given
        RoomTypeResponseDto created = roomTypeService.createRoomType(roomTypeRequest);

        // When
        RoomTypeResponseDto found = roomTypeService.getRoomType(created.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("Deluxe Suite");
    }

    @Test
    void getRoomType_ShouldThrowException_WhenNotFound() {
        // When/Then
        assertThatThrownBy(() -> roomTypeService.getRoomType(9999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void getRoomTypesByHotel_ShouldReturnAllRoomTypes_ForHotel() {
        // Given
        roomTypeService.createRoomType(roomTypeRequest);

        RoomTypeRequestDto secondRoomType = new RoomTypeRequestDto();
        secondRoomType.setName("Standard Room");
        secondRoomType.setHotelId(testHotel.getId());
        secondRoomType.setCapacity(2);
        secondRoomType.setBasePrice(new BigDecimal("150.00"));
        secondRoomType.setBedType("Queen");
        secondRoomType.setRoomSize(30);
        secondRoomType.setAmenities(new ArrayList<>());
        secondRoomType.setImageUrls(new ArrayList<>());
        roomTypeService.createRoomType(secondRoomType);

        // When
        var roomTypes = roomTypeService.getRoomTypesByHotel(testHotel.getId());

        // Then
        assertThat(roomTypes).hasSize(2);
        assertThat(roomTypes).extracting("name").containsExactlyInAnyOrder("Deluxe Suite", "Standard Room");
    }

    @Test
    void updateRoomType_ShouldUpdateFields_WhenValidRequest() {
        // Given
        RoomTypeResponseDto created = roomTypeService.createRoomType(roomTypeRequest);

        RoomTypeRequestDto updateRequest = new RoomTypeRequestDto();
        updateRequest.setName("Presidential Suite");
        updateRequest.setHotelId(testHotel.getId());
        updateRequest.setCapacity(4);
        updateRequest.setBasePrice(new BigDecimal("500.00"));
        updateRequest.setBedType("King");
        updateRequest.setRoomSize(80);
        updateRequest.setAmenities(new ArrayList<>());
        updateRequest.setImageUrls(new ArrayList<>());

        // When
        RoomTypeResponseDto updated = roomTypeService.updateRoomType(created.getId(), updateRequest);

        // Then
        assertThat(updated.getName()).isEqualTo("Presidential Suite");
        assertThat(updated.getCapacity()).isEqualTo(4);
        assertThat(updated.getBasePrice()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    void deleteRoomType_ShouldRemoveRoomType_WhenExists() {
        // Given
        RoomTypeResponseDto created = roomTypeService.createRoomType(roomTypeRequest);

        // When
        roomTypeService.deleteRoomType(created.getId());

        // Then
        assertThatThrownBy(() -> roomTypeService.getRoomType(created.getId()))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }
}