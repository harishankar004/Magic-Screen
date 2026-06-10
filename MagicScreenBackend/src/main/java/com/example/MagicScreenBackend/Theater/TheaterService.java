package com.example.MagicScreenBackend.Theater;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    // Fetch all active screens for the public UI
    public List<Theater> getAllActiveTheaters() {
        return theaterRepository.findByIsActiveTrue();
    }

    // Fetch a single theater screen detail by ID
    public Optional<Theater> getTheaterById(Long id) {
        return theaterRepository.findById(id);
    }
}