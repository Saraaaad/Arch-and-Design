package org.example.tourism.catalog.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    List<Hotel> findByFeaturedTrue();

    @Query("SELECT h.city, h.country, COUNT(h) FROM Hotel h GROUP BY h.city, h.country ORDER BY COUNT(h) DESC")
    List<Object[]> countHotelsByCity();
}