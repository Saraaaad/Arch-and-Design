package org.example.tourism.catalog.roomtype;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.tourism.catalog.hotel.Hotel;

import java.math.BigDecimal;

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

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private BigDecimal basePrice;

    private String amenities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @ToString.Exclude
    private Hotel hotel;

    public RoomType(String name, Integer capacity, BigDecimal basePrice, String amenities) {
        this.name = name;
        this.capacity = capacity;
        this.basePrice = basePrice;
        this.amenities = amenities;
    }
}