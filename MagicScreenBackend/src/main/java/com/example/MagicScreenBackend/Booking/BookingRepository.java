package com.example.MagicScreenBackend.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByTrackingCode(String trackingCode);

    // Restoring the cleanup sweep query for your SlotCleanupScheduler
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.utr IS NULL AND b.slot.heldUntil < :now")
    List<Booking> findExpiredBookingsWithNoPayment(@Param("now") LocalDateTime now);

    // Analytics Dashboard Queries
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = 'CONFIRMED'")
    BigDecimal sumTotalRevenue();

    long countByStatus(String status);

    @Query("SELECT b.slot.theater.name, COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED' GROUP BY b.slot.theater.name")
    List<Object[]> countBookingsPerTheater();
}