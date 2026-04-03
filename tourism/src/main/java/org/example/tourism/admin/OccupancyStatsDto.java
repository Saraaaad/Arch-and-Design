package org.example.tourism.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OccupancyStatsDto {
    private Double overallOccupancyRate;
    private Map<Long, Double> hotelOccupancyRates;
}
