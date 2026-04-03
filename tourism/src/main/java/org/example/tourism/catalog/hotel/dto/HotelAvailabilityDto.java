package org.example.tourism.catalog.hotel.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.Builder;
import org.example.tourism.catalog.shared.PriceBreakdownDto;
import org.example.tourism.catalog.roomtype.dto.RoomTypeAvailabilityDto;


import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@JsonPropertyOrder({
        "hotelId",
        "hotelName",
        "hotelAddress",
        "hotelStarRating",
        "hotelImage",
        "checkIn",
        "checkOut",
        "nights",
        "guests",
        "available",
        "availableRooms",
        "priceBreakdown"
})
public class HotelAvailabilityDto {
    private Long hotelId;
    private String hotelName;
    private String hotelAddress;
    private Integer hotelStarRating;
    private String hotelImage;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer nights;
    private Integer guests;
    private Boolean available;
    private List<RoomTypeAvailabilityDto> availableRooms;
    private PriceBreakdownDto priceBreakdown;
}