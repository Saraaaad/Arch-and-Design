package org.example.tourism.catalog.hotel;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.tourism.catalog.roomtype.RoomType;
import org.example.tourism.catalog.hotel.HotelReview;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "hotels")
@Data
@NoArgsConstructor
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
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

    // Policies
    private Boolean childrenAllowed;
    private Boolean petsAllowed;
    private String petPolicy;
    private Boolean smokingAllowed;
    private String smokingPolicy;
    private String cancellationPolicy;
    private String paymentPolicy;
    private String checkInInstructions;

    // Features
    @ElementCollection
    @CollectionTable(name = "hotel_amenities", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity")
    private Set<String> amenities = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "hotel_images", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "hotel_languages", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "language")
    private Set<String> spokenLanguages = new HashSet<>();

    @Column(columnDefinition = "json")
    private String nearbyAttractions;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<RoomType> roomTypes = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<HotelReview> reviews = new ArrayList<>();


    // Stats
    private Double averageRating;
    private Integer reviewCount;
    private Integer wishlistCount;
    private Integer bookingCount;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Featured/Recommended
    private Boolean featured;
    private Integer featuredOrder;
    private String dealTag;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (averageRating == null) averageRating = 0.0;
        if (reviewCount == null) reviewCount = 0;
        if (wishlistCount == null) wishlistCount = 0;
        if (bookingCount == null) bookingCount = 0;
    }

    public int calculateTotalRooms() {
        return roomTypes.stream()
                .mapToInt(rt -> rt.getTotalRooms() != null ? rt.getTotalRooms() : 0)
                .sum();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Hotel(String name, String city, String country, String address, Integer starRating) {
        this.name = name;
        this.city = city;
        this.country = country;
        this.address = address;
        this.starRating = starRating;
    }
}