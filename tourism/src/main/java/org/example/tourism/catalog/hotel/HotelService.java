package org.example.tourism.catalog.hotel;

import org.example.tourism.catalog.hotel.dto.HotelRequestDto;
import org.example.tourism.catalog.hotel.dto.HotelResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HotelService {
    HotelResponseDto createHotel(HotelRequestDto hotelRequestDto);
    HotelResponseDto getHotel(Long id);
    HotelResponseDto updateHotel(Long id, HotelRequestDto hotelRequestDto);
    Page<HotelResponseDto> getAllHotels(String city, Pageable pageable);
    void deleteHotel(Long id);
}