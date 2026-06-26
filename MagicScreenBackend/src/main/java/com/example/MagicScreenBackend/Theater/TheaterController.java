package com.example.MagicScreenBackend.Theater;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    private Map<String, Object> toDto(Theater t) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id",            t.getId());
        dto.put("name",          t.getName() != null ? t.getName() : "");
        dto.put("theme",         t.getTheme() != null ? t.getTheme() : "");
        dto.put("base_price",    t.getBasePrice());
        dto.put("base_capacity", t.getBaseCapacity());
        dto.put("max_capacity",  t.getMaxCapacity());
        dto.put("extra_per_head",t.getExtraPerHead());
        dto.put("duration_hours",t.getDurationHours());
        dto.put("description",   t.getDescription() != null ? t.getDescription() : "");
        dto.put("is_active",     t.getIsActive());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllTheaters() {
        List<Theater> theaters = theaterService.getAllActiveTheaters();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Theater t : theaters) result.add(toDto(t));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTheaterById(@PathVariable Long id) {
        return theaterService.getTheaterById(id)
                .map(t -> ResponseEntity.ok(toDto(t)))
                .orElse(ResponseEntity.notFound().build());
    }
}