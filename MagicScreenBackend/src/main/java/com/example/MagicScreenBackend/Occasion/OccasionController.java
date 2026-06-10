package com.example.MagicScreenBackend.Occasion;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/occasions")
@RequiredArgsConstructor
public class OccasionController {

    private final OccasionService occasionService;

    // Endpoint: GET http://localhost:8080/api/occasions
    @GetMapping
    public ResponseEntity<List<Occasion>> getAllOccasions() {
        List<Occasion> occasions = occasionService.getAllActiveOccasions();
        return ResponseEntity.ok(occasions);
    }
}