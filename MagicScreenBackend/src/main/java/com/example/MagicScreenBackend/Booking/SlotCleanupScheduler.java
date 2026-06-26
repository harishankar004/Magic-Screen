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
@Slf4j
public class SlotCleanupScheduler {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;

    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredTheaterSlots() {

        // Only expire bookings created MORE THAN 10 minutes ago with no payment
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        List<Booking> expiredCheckouts = bookingRepository.findExpiredPendingBookings(tenMinutesAgo);

        if (!expiredCheckouts.isEmpty()) {
            log.info("Found {} abandoned checkouts past 10-min window. Releasing slots...", expiredCheckouts.size());

            for (Booking booking : expiredCheckouts) {
                booking.setStatus("EXPIRED");

                Slot slot = booking.getSlot();
                // Only release if still HELD — never touch a BOOKED slot
                if ("HELD".equals(slot.getStatus())) {
                    slot.setStatus("AVAILABLE");
                    slot.setHeldUntil(null);
                    slotRepository.save(slot);
                    log.info("Released slot ID {} back to AVAILABLE", slot.getId());
                }

                bookingRepository.save(booking);
            }

            log.info("Cleanup complete — {} slots released.", expiredCheckouts.size());
        }
    }
}