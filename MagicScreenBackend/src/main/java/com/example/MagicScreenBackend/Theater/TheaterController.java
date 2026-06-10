package com.example.MagicScreenBackend.Theater;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    // Endpoint: GET http://localhost:8080/api/theaters
    @GetMapping
    public ResponseEntity<List<Theater>> getAllTheaters() {
        List<Theater> theaters = theaterService.getAllActiveTheaters();
        return ResponseEntity.ok(theaters);
    }

    // Endpoint: GET http://localhost:8080/api/theaters/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Theater> getTheaterById(@PathVariable Long id) {
        return theaterService.getTheaterById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}