package org.example.tourism.catalog;

import java.util.List;

public interface RoomTypeService {
    RoomTypeResponseDto createRoomType(RoomTypeRequestDto request);
    RoomTypeResponseDto getRoomType(Long id);
    List<RoomTypeResponseDto> getAllRoomTypes();
    List<RoomTypeResponseDto> getRoomTypesByHotel(Long hotelId);
    RoomTypeResponseDto updateRoomType(Long id, RoomTypeRequestDto request);
    void deleteRoomType(Long id);
}