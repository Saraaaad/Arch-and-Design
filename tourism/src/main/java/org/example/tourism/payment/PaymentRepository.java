package org.example.tourism.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.bookingId IN (SELECT b.id FROM Booking b WHERE b.userId = :userId)")
    List<Payment> findPaymentsByUserId(@Param("userId") Long userId);


    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    Long sumAmountByStatus(@Param("status") PaymentStatus status);
}