package org.example.tourism.catalog.hotel;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.tourism.catalog.hotel.dto.HotelRequestDto;
import org.example.tourism.catalog.hotel.dto.HotelResponseDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
@Tag(name = "Hotels", description = "Hotel management")
public class HotelController {
    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping
    public HotelResponseDto createHotel(@Valid @RequestBody HotelRequestDto request) {
        return hotelService.createHotel(request);
    }

    @GetMapping("/{id}")
    public HotelResponseDto getHotel(@PathVariable Long id) {
        return hotelService.getHotel(id);
    }

    @GetMapping
    public Page<HotelResponseDto> getAllHotels(
            @RequestParam(required = false) String city,
            Pageable pageable) {
        return hotelService.getAllHotels(city, pageable);
    }

    @PutMapping("/{id}")
    public HotelResponseDto updateHotel(@PathVariable Long id, @Valid @RequestBody HotelRequestDto request) {
        return hotelService.updateHotel(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
    }
}