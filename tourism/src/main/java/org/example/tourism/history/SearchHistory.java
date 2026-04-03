package org.example.tourism.history;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "search_history", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_searched_at", columnList = "searchedAt")
})
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String city;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal maxPrice;
    private Integer guests;
    private Integer starRating;
    private LocalDateTime searchedAt;

    @PrePersist
    protected void onCreate() {
        searchedAt = LocalDateTime.now();
    }
}