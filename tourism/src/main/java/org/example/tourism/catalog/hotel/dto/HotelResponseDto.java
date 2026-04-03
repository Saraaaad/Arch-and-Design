package org.example.tourism.catalog.hotel.dto;

import lombok.Data;

@Data
public class HotelResponseDto {
    private Long id;
    private String name;
    private String city;
    private String address;
    private Integer starRating;
}