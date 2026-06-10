package com.example.MagicScreenBackend.Occasion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OccasionService {

    private final OccasionRepository occasionRepository;

    // Fetch all active occasion templates
    public List<Occasion> getAllActiveOccasions() {
        return occasionRepository.findByIsActiveTrue();
    }
}