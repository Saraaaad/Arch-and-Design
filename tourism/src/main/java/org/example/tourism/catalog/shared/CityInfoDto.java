package org.example.tourism.catalog.shared;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CityInfoDto {
    private String city;
    private String country;
    private Integer hotelCount;
}