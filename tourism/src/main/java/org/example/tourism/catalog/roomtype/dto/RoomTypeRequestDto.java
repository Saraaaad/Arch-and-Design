package org.example.tourism.catalog.roomtype.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomTypeRequestDto {
    private String name;
    private String description;
    private Long hotelId;
    private Integer capacity;
    private BigDecimal basePrice;
    private String bedType;
    private Integer roomSize;
    private List<String> amenities;
    private List<String> imageUrls;
    private Integer totalRooms;
    private Boolean smokingAllowed;
    private String view;
    private String boardBasis;
    private Boolean refundable;
    private String cancellationPolicy;
}