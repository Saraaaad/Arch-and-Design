package org.example.tourism.catalog.roomtype;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.tourism.catalog.hotel.Hotel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_types")
@Data
@NoArgsConstructor
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private BigDecimal basePrice;

    private String bedType;

    private Integer roomSize;

    @ElementCollection
    @CollectionTable(name = "room_amenities", joinColumns = @JoinColumn(name = "room_type_id"))
    @Column(name = "amenity")
    private List<String> amenities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_type_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    private Integer totalRooms;

    private Boolean smokingAllowed;

    private String view;

    private String boardBasis;

    private Boolean refundable;

    private String cancellationPolicy;

    @Column(name = "quantity")
    private Integer quantity = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @ToString.Exclude
    private Hotel hotel;

    public int getAvailableRoomCount() {
        return quantity != null ? quantity : 1;
    }

}