package com.example.MagicScreenBackend.Slot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;

    // Fetch timings for a specific screen on a specific calendar date
    public List<Slot> getSlotsByTheaterAndDate(Long theaterId, LocalDate date) {
        return slotRepository.findByTheaterIdAndSlotDate(theaterId, date);
    }
}