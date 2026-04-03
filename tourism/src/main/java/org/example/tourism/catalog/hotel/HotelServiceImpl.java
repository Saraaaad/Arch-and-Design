package org.example.tourism.catalog.hotel;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.tourism.catalog.hotel.dto.HotelRequestDto;
import org.example.tourism.catalog.hotel.dto.HotelResponseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    @Override
    @Transactional
    public HotelResponseDto createHotel(HotelRequestDto hotelRequestDto) {
        Hotel hotel = new Hotel();
        BeanUtils.copyProperties(hotelRequestDto, hotel);
        Hotel savedHotel = hotelRepository.save(hotel);
        return mapToDto(savedHotel);
    }

    @Override
    public HotelResponseDto getHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + id));
        return mapToDto(hotel);
    }

    @Override
    @Transactional
    public HotelResponseDto updateHotel(Long id, HotelRequestDto hotelRequestDto) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + id));

        BeanUtils.copyProperties(hotelRequestDto, hotel);
        Hotel updatedHotel = hotelRepository.save(hotel);
        return mapToDto(updatedHotel);
    }

    @Override
    public Page<HotelResponseDto> getAllHotels(String city, Pageable pageable) {
        Page<Hotel> hotels;
        if (city != null && !city.isEmpty()) {
            hotels = hotelRepository.findByCityContainingIgnoreCase(city, pageable);
        } else {
            hotels = hotelRepository.findAll(pageable);
        }
        return hotels.map(this::mapToDto);
    }

    @Override
    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new EntityNotFoundException("Hotel not found with id: " + id);
        }
        hotelRepository.deleteById(id);
    }

    private HotelResponseDto mapToDto(Hotel hotel) {
        HotelResponseDto dto = new HotelResponseDto();
        BeanUtils.copyProperties(hotel, dto);
        return dto;
    }
}