package org.example.tourism.security.payment;

import org.example.tourism.payment.Payment;
import org.example.tourism.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<org.example.tourism.payment.Payment, Long> {
    Optional<org.example.tourism.payment.Payment> findByBookingId(Long bookingId);

    @Query("SELECT p FROM Payment p WHERE p.bookingId IN (SELECT b.id FROM Booking b WHERE b.userId = :userId)")
    List<org.example.tourism.payment.Payment> findPaymentsByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED'")
    Double getTotalRevenue();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'COMPLETED' AND p.createdAt BETWEEN :startDate AND :endDate")
    Double getRevenueBetweenDates(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime date);
}