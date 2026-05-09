package org.example.tourism.catalog.hotel;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * DESIGN PATTERN: BUILDER (Enhanced)
 *
 * Uses Lombok @Builder to create a fluent API for building search criteria.
 * The Builder pattern is ideal here because:
 * 1. Many optional parameters (10+ search filters)
 * 2. Immutable object once built
 * 3. Fluent API improves readability
 * 4. Easy to add new search parameters without breaking existing code
 *
 * Usage example:
 * HotelSearchCriteria criteria = HotelSearchCriteria.builder()
 *     .city("Paris")
 *     .starRating(4)
 *     .checkIn(LocalDate.now())
 *     .maxPrice(new BigDecimal("200"))
 *     .build();
 */
@Data
@Builder
public class HotelSearchCriteria {
    // Basic filters
    private String name;
    private String city;
    private Integer starRating;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // Date filters
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guests;

    // Policy
    private Boolean childrenAllowed;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private String cancellationPolicy;

    // Amenity filters
    private Set<String> amenities;

    // Room type filters
    private String roomTypeName;
    private String bedType;
    private Integer minCapacity;

    // Hotel features
    private Boolean featured;
    private String dealTag;

    // Sorting
    private String sortBy;

    /**
     * Validates that the search criteria is logically consistent
     * @throws IllegalArgumentException if criteria is invalid
     */
    public void validate() {
        if (checkIn != null && checkOut != null && checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        }
        if (guests != null && guests < 1) {
            throw new IllegalArgumentException("Guests must be at least 1");
        }
    }
}