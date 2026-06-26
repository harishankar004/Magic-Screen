package com.example.MagicScreenBackend.Slot;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAvailableSlots(
            @RequestParam Long theaterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Slot> slots = slotService.getSlotsByTheaterAndDate(theaterId, date);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Slot s : slots) {
            String startStr  = s.getStartTime() != null ? s.getStartTime().format(TIME_FMT) : "";
            String endStr    = s.getEndTime()   != null ? s.getEndTime().format(TIME_FMT)   : "";
            String timeRange = startStr + " - " + endStr;
            String slotName  = (s.getName() != null && !s.getName().isBlank()) ? s.getName() : timeRange;
            String heldUntil = s.getHeldUntil() != null ? s.getHeldUntil().toString() : "";

            Map<String, Object> dto = new HashMap<>();
            dto.put("id",         s.getId());
            dto.put("name",       slotName);
            dto.put("start_time", startStr);
            dto.put("end_time",   endStr);
            dto.put("time_range", timeRange);
            dto.put("status",     s.getStatus());
            dto.put("held_until", heldUntil);
            result.add(dto);
        }

        return ResponseEntity.ok(result);
    }
}