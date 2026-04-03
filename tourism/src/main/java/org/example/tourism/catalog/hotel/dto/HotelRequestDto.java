package org.example.tourism.catalog.hotel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class HotelRequestDto {
    @NotBlank(message = "Hotel name is required")
    private String name;

    private String description;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Country is required")
    private String country;

    private String address;
    private String zipCode;
    private Double latitude;
    private Double longitude;

    @NotNull(message = "Star rating is required")
    private Integer starRating;

    @Email(message = "Invalid email format")
    private String contactEmail;

    private String contactPhone;
    private String website;
    private String checkInTime = "15:00";
    private String checkOutTime = "11:00";
    private Integer totalRooms = 100;

    // Policies
    private Boolean childrenAllowed = true;
    private Boolean petsAllowed = false;
    private String petPolicy = "No pets allowed";
    private Boolean smokingAllowed = false;
    private String smokingPolicy = "Non-smoking hotel";
    private String cancellationPolicy = "Free cancellation up to 24 hours before check-in";
    private String paymentPolicy = "Credit card required for booking";
    private String checkInInstructions = "Please present ID and credit card at check-in";

    // Features
    private List<String> amenities = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private Set<String> spokenLanguages = new HashSet<>();
    private String nearbyAttractions = "[]";

    // Featured
    private Boolean featured = false;
    private Integer featuredOrder = 0;
    private String dealTag;
}