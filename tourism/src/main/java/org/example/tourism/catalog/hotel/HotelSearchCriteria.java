package org.example.tourism.catalog.hotel;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

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
    private String cancellationPolicy;  // "FREE_CANCELLATION", "NON_REFUNDABLE"

    // Amenity filters
    private Set<String> amenities;  // ["WiFi", "Pool", "Parking", "Spa"]

    //  Room type filters
    private String roomTypeName;    // "Deluxe Suite"
    private String bedType;          // "King", "Queen", "Twin"
    private Integer minCapacity;     // Minimum room capacity

    // Hotel features
    private Boolean featured;        // Featured hotels only
    private String dealTag;          // "SPRING_SALE", "WEEKEND_DEAL"

    // Sorting
    private String sortBy;
}