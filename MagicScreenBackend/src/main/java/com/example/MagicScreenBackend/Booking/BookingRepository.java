package com.example.MagicScreenBackend.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByTrackingCode(String trackingCode);

    Optional<Booking> findBySlotIdAndCustomerEmailAndStatus(Long slotId, String customerEmail, String status);

    // Only expire bookings that are STILL PENDING and were created MORE than 10 minutes ago
    // This gives customers a full 10-minute payment window before their slot is released
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :threshold")
    List<Booking> findExpiredPendingBookings(@Param("threshold") LocalDateTime threshold);
}