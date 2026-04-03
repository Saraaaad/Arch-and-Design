package org.example.tourism.catalog.hotel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelReviewRepository extends JpaRepository<HotelReview, Long> {

    Page<HotelReview> findByHotelId(Long hotelId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM HotelReview r WHERE r.hotel.id = :hotelId")
    Double getAverageRatingForHotel(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(r) FROM HotelReview r WHERE r.hotel.id = :hotelId")
    Integer getReviewCountForHotel(@Param("hotelId") Long hotelId);


    boolean existsByHotelIdAndUserId(Long hotelId, Long userId);

    @Query("SELECT r.rating, COUNT(r) FROM HotelReview r WHERE r.hotel.id = :hotelId GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("hotelId") Long hotelId);

    List<HotelReview> findByHotelIdOrderByCreatedAtDesc(Long hotelId, Pageable pageable);

    List<HotelReview> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE HotelReview r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.id = :reviewId")
    void incrementHelpfulCount(@Param("reviewId") Long reviewId);


    @Modifying
    @Query("DELETE FROM HotelReview r WHERE r.hotel.id = :hotelId")
    void deleteByHotelId(@Param("hotelId") Long hotelId);

}