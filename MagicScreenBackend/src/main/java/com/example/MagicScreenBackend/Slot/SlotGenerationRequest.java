package com.example.MagicScreenBackend.Slot;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SlotGenerationRequest {
    private LocalDate targetDate; // The specific day to generate slots for (e.g., "2026-06-15")
}