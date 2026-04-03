package org.example.tourism.wishlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);

    Optional<Wishlist> findByUserIdAndHotelId(Long userId, Long hotelId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.userId = :userId AND w.hotelId = :hotelId")
    void deleteByUserIdAndHotelId(@Param("userId") Long userId, @Param("hotelId") Long hotelId);

    boolean existsByUserIdAndHotelId(Long userId, Long hotelId);

    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.hotelId = :hotelId")
    Long countByHotelId(@Param("hotelId") Long hotelId);
}