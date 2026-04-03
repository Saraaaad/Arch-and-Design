package org.example.tourism.catalog.roomtype;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourism.booking.BookingRepository;
import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.hotel.HotelRepository;
import org.example.tourism.catalog.roomtype.dto.RoomTypeRequestDto;
import org.example.tourism.catalog.roomtype.dto.RoomTypeResponseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public RoomTypeResponseDto createRoomType(RoomTypeRequestDto request) {
        log.info("Creating room type: {} for hotel ID: {}", request.getName(), request.getHotelId());

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + request.getHotelId()));

        RoomType roomType = new RoomType();
        BeanUtils.copyProperties(request, roomType);
        roomType.setHotel(hotel);

        RoomType savedRoomType = roomTypeRepository.save(roomType);
        log.info("Room type created with ID: {}", savedRoomType.getId());

        return mapToDto(savedRoomType);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponseDto getRoomType(Long id) {
        log.info("Fetching room type with ID: {}", id);

        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + id));

        return mapToDto(roomType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponseDto> getAllRoomTypes() {
        log.info("Fetching all room types");

        return roomTypeRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponseDto> getRoomTypesByHotel(Long hotelId) {
        log.info("Fetching room types for hotel ID: {}", hotelId);

        if (!hotelRepository.existsById(hotelId)) {
            throw new EntityNotFoundException("Hotel not found with id: " + hotelId);
        }

        return roomTypeRepository.findByHotelId(hotelId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoomTypeResponseDto updateRoomType(Long id, RoomTypeRequestDto request) {
        log.info("Updating room type with ID: {}", id);

        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + id));

        if (!roomType.getHotel().getId().equals(request.getHotelId())) {
            Hotel newHotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + request.getHotelId()));
            roomType.setHotel(newHotel);
        }


        roomType.setName(request.getName());
        roomType.setCapacity(request.getCapacity());
        roomType.setBasePrice(request.getBasePrice());

        RoomType updatedRoomType = roomTypeRepository.save(roomType);
        log.info("Room type updated successfully: {}", id);

        return mapToDto(updatedRoomType);
    }

    @Override
    @Transactional
    public void deleteRoomType(Long id) {
        log.info("Deleting room type with ID: {}", id);

        if (!roomTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("RoomType not found with id: " + id);
        }

        roomTypeRepository.deleteById(id);
        log.info("Room type deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomTypeAvailable(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        log.info("Checking availability for room type {} from {} to {}", roomTypeId, checkIn, checkOut);

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + roomTypeId));

        int totalRoomsOfType = roomType.getQuantity() != null ? roomType.getQuantity() : 1;

        int bookedRooms = bookingRepository.countOverlappingBookings(
                roomTypeId, checkIn, checkOut);

        boolean available = bookedRooms < totalRoomsOfType;

        log.info("Room type {} availability: {} (booked: {}, total: {})",
                roomTypeId, available, bookedRooms, totalRoomsOfType);

        return available;
    }
    @Override
    @Transactional
    public RoomTypeResponseDto addRoomTypeImages(Long roomTypeId, List<String> imageUrls) {
        log.info("Adding {} images to room type ID: {}", imageUrls.size(), roomTypeId);

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + roomTypeId));

        // Initialize imageUrls list if null
        if (roomType.getImageUrls() == null) {
            roomType.setImageUrls(new ArrayList<>());
        }

        // Add new image URLs
        roomType.getImageUrls().addAll(imageUrls);

        RoomType updatedRoomType = roomTypeRepository.save(roomType);
        log.info("Added {} images to room type ID: {}. Total images: {}",
                imageUrls.size(), roomTypeId, updatedRoomType.getImageUrls().size());

        return mapToDto(updatedRoomType);
    }

    @Override
    @Transactional
    public RoomTypeResponseDto removeRoomTypeImage(Long roomTypeId, String imageUrl) {
        log.info("Removing image from room type ID: {}, image URL: {}", roomTypeId, imageUrl);

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + roomTypeId));

        if (roomType.getImageUrls() == null || roomType.getImageUrls().isEmpty()) {
            log.warn("Room type has no images to remove");
            throw new IllegalArgumentException("Room type has no images");
        }

        // Remove the URL from the list
        boolean removed = roomType.getImageUrls().remove(imageUrl);

        if (!removed) {
            log.warn("Image URL not found in room type: {}", imageUrl);
            throw new IllegalArgumentException("Image URL not found for this room type");
        }

        RoomType updatedRoomType = roomTypeRepository.save(roomType);
        log.info("Image removed successfully. Remaining images: {}", updatedRoomType.getImageUrls().size());

        return mapToDto(updatedRoomType);
    }

    // Make sure your mapToDto method includes imageUrls
    private RoomTypeResponseDto mapToDto(RoomType roomType) {
        RoomTypeResponseDto dto = new RoomTypeResponseDto();
        BeanUtils.copyProperties(roomType, dto);
        dto.setHotelId(roomType.getHotel().getId());

        // Ensure imageUrls is never null in the response
        if (dto.getImageUrls() == null) {
            dto.setImageUrls(new ArrayList<>());
        }

        return dto;
    }

}