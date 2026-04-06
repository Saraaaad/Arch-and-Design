package org.example.tourism.unit.catalog.roomtype;

import org.example.tourism.booking.BookingRepository;
import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.hotel.HotelRepository;
import org.example.tourism.catalog.roomtype.RoomType;
import org.example.tourism.catalog.roomtype.RoomTypeRepository;
import org.example.tourism.catalog.roomtype.RoomTypeServiceImpl;
import org.example.tourism.catalog.roomtype.dto.RoomTypeRequestDto;
import org.example.tourism.catalog.roomtype.dto.RoomTypeResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceImplTest {

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private RoomTypeServiceImpl roomTypeService;

    private Hotel hotel;
    private RoomType roomType;
    private RoomTypeRequestDto roomTypeRequest;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setCity("Test City");

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Suite");
        roomType.setCapacity(2);
        roomType.setBasePrice(new BigDecimal("250.00"));
        roomType.setBedType("King");
        roomType.setRoomSize(45);
        roomType.setHotel(hotel);
        roomType.setQuantity(10);
        roomType.setImageUrls(new ArrayList<>());
        roomType.setAmenities(new ArrayList<>());

        roomTypeRequest = new RoomTypeRequestDto();
        roomTypeRequest.setName("Deluxe Suite");
        roomTypeRequest.setHotelId(1L);
        roomTypeRequest.setCapacity(2);
        roomTypeRequest.setBasePrice(new BigDecimal("250.00"));
        roomTypeRequest.setBedType("King");
        roomTypeRequest.setRoomSize(45);
        roomTypeRequest.setAmenities(new ArrayList<>());
        roomTypeRequest.setImageUrls(new ArrayList<>());
        roomTypeRequest.setTotalRooms(10);
        roomTypeRequest.setSmokingAllowed(false);
        roomTypeRequest.setRefundable(true);
    }

    @Test
    void createRoomType_ShouldSucceed_WhenValidRequest() {
        // Given
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);

        // When
        RoomTypeResponseDto result = roomTypeService.createRoomType(roomTypeRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Deluxe Suite");
        assertThat(result.getHotelId()).isEqualTo(1L);
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
    }

    @Test
    void createRoomType_ShouldThrowException_WhenHotelNotFound() {
        // Given
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());
        roomTypeRequest.setHotelId(999L);

        // When/Then
        assertThatThrownBy(() -> roomTypeService.createRoomType(roomTypeRequest))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("Hotel not found");
    }

    @Test
    void getRoomType_ShouldReturnRoomType_WhenExists() {
        // Given
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        // When
        RoomTypeResponseDto result = roomTypeService.getRoomType(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Deluxe Suite");
    }

    @Test
    void getRoomType_ShouldThrowException_WhenNotFound() {
        // Given
        when(roomTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roomTypeService.getRoomType(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void getAllRoomTypes_ShouldReturnAllRoomTypes() {
        // Given
        when(roomTypeRepository.findAll()).thenReturn(List.of(roomType));

        // When
        List<RoomTypeResponseDto> results = roomTypeService.getAllRoomTypes();

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Deluxe Suite");
    }

    @Test
    void getRoomTypesByHotel_ShouldReturnRoomTypes_WhenHotelExists() {
        // Given
        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(roomTypeRepository.findByHotelId(1L)).thenReturn(List.of(roomType));

        // When
        List<RoomTypeResponseDto> results = roomTypeService.getRoomTypesByHotel(1L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getHotelId()).isEqualTo(1L);
    }

    @Test
    void getRoomTypesByHotel_ShouldThrowException_WhenHotelNotFound() {
        // Given
        when(hotelRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> roomTypeService.getRoomTypesByHotel(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void updateRoomType_ShouldSucceed_WhenValidRequest() {
        // Given
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);

        roomTypeRequest.setName("Presidential Suite");
        roomTypeRequest.setCapacity(4);
        roomTypeRequest.setBasePrice(new BigDecimal("500.00"));

        // When
        RoomTypeResponseDto result = roomTypeService.updateRoomType(1L, roomTypeRequest);

        // Then
        assertThat(result).isNotNull();
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
    }

    @Test
    void updateRoomType_ShouldThrowException_WhenNotFound() {
        // Given
        when(roomTypeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roomTypeService.updateRoomType(999L, roomTypeRequest))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void deleteRoomType_ShouldSucceed_WhenExists() {
        // Given
        when(roomTypeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomTypeRepository).deleteById(1L);

        // When
        roomTypeService.deleteRoomType(1L);

        // Then
        verify(roomTypeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRoomType_ShouldThrowException_WhenNotFound() {
        // Given
        when(roomTypeRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> roomTypeService.deleteRoomType(999L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void isRoomTypeAvailable_ShouldReturnTrue_WhenEnoughRoomsAvailable() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(12);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.countOverlappingBookings(1L, checkIn, checkOut)).thenReturn(5);

        // When
        boolean result = roomTypeService.isRoomTypeAvailable(1L, checkIn, checkOut);

        // Then
        assertThat(result).isTrue(); // 10 total - 5 booked = 5 available
    }

    @Test
    void isRoomTypeAvailable_ShouldReturnFalse_WhenNoRoomsAvailable() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(12);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(bookingRepository.countOverlappingBookings(1L, checkIn, checkOut)).thenReturn(10);

        // When
        boolean result = roomTypeService.isRoomTypeAvailable(1L, checkIn, checkOut);

        // Then
        assertThat(result).isFalse(); // 10 total - 10 booked = 0 available
    }

    @Test
    void addRoomTypeImages_ShouldAddImages_WhenRoomTypeExists() {
        // Given
        List<String> imageUrls = List.of("image1.jpg", "image2.jpg");
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);

        // When
        RoomTypeResponseDto result = roomTypeService.addRoomTypeImages(1L, imageUrls);

        // Then
        assertThat(result).isNotNull();
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
    }

    @Test
    void removeRoomTypeImage_ShouldRemoveImage_WhenExists() {
        // Given
        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("image1.jpg");
        imageUrls.add("image2.jpg");
        roomType.setImageUrls(imageUrls);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(roomType);

        // When
        RoomTypeResponseDto result = roomTypeService.removeRoomTypeImage(1L, "image1.jpg");

        // Then
        assertThat(result).isNotNull();
        assertThat(roomType.getImageUrls()).hasSize(1);
        assertThat(roomType.getImageUrls().get(0)).isEqualTo("image2.jpg");
    }

    @Test
    void removeRoomTypeImage_ShouldThrowException_WhenImageNotFound() {
        // Given
        roomType.setImageUrls(new ArrayList<>());
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        // When/Then
        assertThatThrownBy(() -> roomTypeService.removeRoomTypeImage(1L, "nonexistent.jpg"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}