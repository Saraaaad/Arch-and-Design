package org.example.tourism.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TopHotelDto {
    private Long hotelId;
    private String hotelName;
    private Long bookingCount;
    private BigDecimal totalRevenue;
}
