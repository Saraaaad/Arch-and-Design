package org.example.tourism.catalog.hotel.dto;

import lombok.Data;
import lombok.Builder;
import org.example.tourism.catalog.roomtype.dto.RoomTypeSummaryDto;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class HotelDetailResponseDto {
    private Long id;
    private String name;
    private String description;
    private String city;
    private String country;
    private String address;
    private String zipCode;
    private Double latitude;
    private Double longitude;
    private Integer starRating;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String checkInTime;
    private String checkOutTime;
    private Integer totalRooms;

    // POLICIES
    private Boolean childrenAllowed;
    private Boolean petsAllowed;
    private String petPolicy;
    private Boolean smokingAllowed;
    private String smokingPolicy;
    private String cancellationPolicy;
    private String paymentPolicy;
    private String checkInInstructions;

    // Features
    private List<String> amenities;
    private List<String> imageUrls;
    private Set<String> spokenLanguages;
    private String nearbyAttractions;

    // Room Types
    private List<RoomTypeSummaryDto> roomTypes;

    // Stats
    private Double averageRating;
    private Integer reviewCount;
    private Boolean featured;
    private Integer featuredOrder;
    private String dealTag;

    private Long wishlistCount;

}