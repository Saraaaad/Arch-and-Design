package org.example.tourism.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyStatsDto {
    private String month;
    private Long bookingsCount;
    private BigDecimal revenue;
}
