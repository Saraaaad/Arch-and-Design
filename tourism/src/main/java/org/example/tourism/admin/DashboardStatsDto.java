package org.example.tourism.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDto {
    private TotalStatsDto totalStats;
    private Map<String, MonthlyStatsDto> monthlyStats;
    private List<TopHotelDto> topHotels;
    private OccupancyStatsDto occupancyStats;
}

