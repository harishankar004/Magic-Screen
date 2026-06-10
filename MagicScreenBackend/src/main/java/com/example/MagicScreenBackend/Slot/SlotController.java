package com.example.MagicScreenBackend.Slot;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;

    // Endpoint: GET http://localhost:8080/api/slots?theaterId=1&date=2026-06-10
    @GetMapping
    public ResponseEntity<List<Slot>> getAvailableSlots(
            @RequestParam Long theaterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Slot> slots = slotService.getSlotsByTheaterAndDate(theaterId, date);
        return ResponseEntity.ok(slots);
    }
}