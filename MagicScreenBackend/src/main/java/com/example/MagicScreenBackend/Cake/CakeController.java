package com.example.MagicScreenBackend.Cake;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addons")
public class CakeController {

    @Autowired
    private CakeRepository cakeRepository;

    @GetMapping("/cakes")
    public ResponseEntity<List<Cake>> getAllCakes() {
        List<Cake> cakes = cakeRepository.findAll();
        return ResponseEntity.ok(cakes); // Explicitly returning the List
    }
}