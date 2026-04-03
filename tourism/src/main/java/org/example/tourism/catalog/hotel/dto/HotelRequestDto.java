package org.example.tourism.catalog.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HotelRequestDto {
    @NotBlank(message = "Hotel name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    private String address;

    @NotNull(message = "Star rating is required")
    private Integer starRating;
}