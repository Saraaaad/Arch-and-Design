package org.example.tourism.catalog.shared;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
public class PriceBreakdownDto {
    private BigDecimal subtotal;
    private BigDecimal taxes;
    private BigDecimal total;
    private String currency;
}