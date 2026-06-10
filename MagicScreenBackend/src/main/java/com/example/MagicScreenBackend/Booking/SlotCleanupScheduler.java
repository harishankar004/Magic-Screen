package com.example.MagicScreenBackend.Booking;

import com.example.MagicScreenBackend.Slot.Slot;
import com.example.MagicScreenBackend.Slot.SlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j // Provides clean log outputs directly to your IntelliJ terminal window
public class SlotCleanupScheduler {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;

    // Runs automatically every 60,000 milliseconds (1 minute)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredTheaterSlots() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Fetch all bookings that have run out of time without a UTR
        List<Booking> expiredCheckouts = bookingRepository.findExpiredBookingsWithNoPayment(now);

        if (!expiredCheckouts.isEmpty()) {
            log.info("Found {} abandoned checkouts. Commencing cache release...", expiredCheckouts.size());

            for (Booking booking : expiredCheckouts) {
                // 2. Mark the Booking record as EXPIRED
                booking.setStatus("EXPIRED");

                // 3. Re-open the slot for the public view
                Slot slot = booking.getSlot();
                slot.setStatus("AVAILABLE");
                slot.setHeldUntil(null); // Clear the expiration lock anchor

                slotRepository.save(slot);
                bookingRepository.save(booking);
            }

            log.info("Successfully cleaned up expired hold registers.");
        }
    }
}