package org.example.tourism.catalog.hotel;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "hotel_reviews", indexes = {
        @Index(name = "idx_hotel_id", columnList = "hotel_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class HotelReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private Long userId;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String comment;

    @Column(length = 200)
    private String title;

    @Column(name = "stay_date")
    private LocalDate stayDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "verified_booking")
    private Boolean verifiedBooking = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}