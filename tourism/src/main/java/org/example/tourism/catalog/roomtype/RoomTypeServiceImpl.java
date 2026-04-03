package org.example.tourism.catalog.roomtype;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourism.catalog.hotel.Hotel;
import org.example.tourism.catalog.hotel.HotelRepository;
import org.example.tourism.catalog.roomtype.dto.RoomTypeRequestDto;
import org.example.tourism.catalog.roomtype.dto.RoomTypeResponseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomTypeServiceImpl implements RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;

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

        // Verify hotel exists
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

        // If hotelId is being changed, verify the new hotel exists
        if (!roomType.getHotel().getId().equals(request.getHotelId())) {
            Hotel newHotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + request.getHotelId()));
            roomType.setHotel(newHotel);
        }

        // Update fields (except hotel which we handled above)
        roomType.setName(request.getName());
        roomType.setCapacity(request.getCapacity());
        roomType.setBasePrice(request.getBasePrice());
        roomType.setAmenities(request.getAmenities());

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

        // Check if there are any active bookings for this room type

        roomTypeRepository.deleteById(id);
        log.info("Room type deleted successfully: {}", id);
    }

    private RoomTypeResponseDto mapToDto(RoomType roomType) {
        RoomTypeResponseDto dto = new RoomTypeResponseDto();
        BeanUtils.copyProperties(roomType, dto);
        dto.setHotelId(roomType.getHotel().getId());
        return dto;
    }
}