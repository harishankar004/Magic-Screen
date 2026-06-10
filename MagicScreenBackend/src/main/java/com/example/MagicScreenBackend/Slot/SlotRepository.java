package com.example.MagicScreenBackend.Slot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    // Used by the public frontend wizard to display available timings for a screen on a specific date
    List<Slot> findByTheaterIdAndSlotDate(Long theaterId, LocalDate slotDate);

    // CRITICAL: Locks this specific slot row in MySQL during checkout to prevent double-booking race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :id")
    Optional<Slot> findAndLockById(@Param("id") Long id);

    boolean existsByTheaterIdAndSlotDateAndStartTime(Long theaterId, LocalDate slotDate, LocalTime startTime);
}