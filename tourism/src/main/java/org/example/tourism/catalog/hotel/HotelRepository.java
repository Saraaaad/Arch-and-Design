package org.example.tourism.catalog.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    List<Hotel> findByFeaturedTrue();

    @Query("SELECT h.city, h.country, COUNT(h) FROM Hotel h GROUP BY h.city, h.country ORDER BY COUNT(h) DESC")
    List<Object[]> countHotelsByCity();

    @Query("SELECT DISTINCT h FROM Hotel h LEFT JOIN FETCH h.roomTypes WHERE h.id = :id")
    Optional<Hotel> findByIdWithRoomTypes(@Param("id") Long id);
}