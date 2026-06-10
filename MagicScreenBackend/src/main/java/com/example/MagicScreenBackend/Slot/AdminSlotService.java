package com.example.MagicScreenBackend.Slot;

import com.example.MagicScreenBackend.Theater.Theater;
import com.example.MagicScreenBackend.Theater.TheaterRepository; // Assuming this exists
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSlotService {

    private final SlotRepository slotRepository;
    private final TheaterRepository theaterRepository;

    @Transactional
    public int generateStandardSlotsForDate(LocalDate date) {
        // 1. Fetch all active private theater screens
        List<Theater> activeTheaters = theaterRepository.findAll(); // Or findByIsActiveTrue() if implemented

        // 2. Define the fixed operational time blocks for a standard day
        // Adjust these start times based on your theater timings!
        LocalTime[] slotStartTimes = {
                LocalTime.of(9, 0),   // Slot 1: 09:00 AM
                LocalTime.of(13, 0),  // Slot 2: 01:00 PM
                LocalTime.of(17, 0),  // Slot 3: 05:00 PM
                LocalTime.of(21, 0)   // Slot 4: 09:00 PM
        };

        int slotsCreatedCount = 0;

        // 3. Loop through every theater screen and create available slots
        for (Theater theater : activeTheaters) {
            for (LocalTime startTime : slotStartTimes) {

                // Check if this slot already exists to prevent duplication crashes
                boolean exists = slotRepository.existsByTheaterIdAndSlotDateAndStartTime(
                        theater.getId(), date, startTime
                );

                if (!exists) {
                    Slot slot = new Slot();
                    slot.setTheater(theater);
                    slot.setSlotDate(date);
                    slot.setStartTime(startTime);

                    // Automatically calculate end time based on theater's duration capacity configuration
                    slot.setEndTime(startTime.plusHours(theater.getDurationHours()));

                    // Set default status to ready for customer reservation lookup
                    slot.setStatus("AVAILABLE");
                    slot.setHeldUntil(null);

                    slotRepository.save(slot);
                    slotsCreatedCount++;
                }
            }
        }

        return slotsCreatedCount;
    }
}