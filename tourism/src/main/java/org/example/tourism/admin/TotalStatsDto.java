package org.example.tourism.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TotalStatsDto {
    private Long totalUsers;
    private Long totalBookings;
    private Long totalHotels;
    private Long totalRevenue;
    private Long pendingBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
}
