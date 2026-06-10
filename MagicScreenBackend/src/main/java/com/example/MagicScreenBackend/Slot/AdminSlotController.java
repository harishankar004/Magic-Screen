package com.example.MagicScreenBackend.Slot;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/slots")
@RequiredArgsConstructor
public class AdminSlotController {

    private final AdminSlotService adminSlotService;

    // Endpoint: POST http://localhost:8080/api/admin/slots/generate
    @PostMapping("/generate")
    public ResponseEntity<?> bulkGenerateSlots(@RequestBody SlotGenerationRequest request) {
        if (request.getTargetDate() == null) {
            throw new IllegalArgumentException("Target date parameter cannot be null.");
        }

        int createdCount = adminSlotService.generateStandardSlotsForDate(request.getTargetDate());

        return ResponseEntity.ok(Map.of(
                "message", "Slot generation complete successfully.",
                "date", request.getTargetDate().toString(),
                "slotsCreated", createdCount
        ));
    }
}