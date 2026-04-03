package org.example.tourism.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    List<Booking> findByHotelId(Long hotelId);

    boolean existsByHotelIdAndUserIdAndStatus(Long hotelId, Long userId, BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.roomTypeId = :roomTypeId " +
            "AND b.status <> 'CANCELLED' " +
            "AND b.checkInDate < :checkOut AND b.checkOutDate > :checkIn")
    int countOverlappingBookings(@Param("roomTypeId") Long roomTypeId,
                                 @Param("checkIn") LocalDate checkIn,
                                 @Param("checkOut") LocalDate checkOut);


    @Query("SELECT b FROM Booking b WHERE b.roomTypeId = :roomTypeId " +
            "AND b.status <> 'CANCELLED' " +
            "AND b.checkInDate < :checkOut AND b.checkOutDate > :checkIn")
    List<Booking> findOverlappingBookings(@Param("roomTypeId") Long roomTypeId,
                                          @Param("checkIn") LocalDate checkIn,
                                          @Param("checkOut") LocalDate checkOut);


}