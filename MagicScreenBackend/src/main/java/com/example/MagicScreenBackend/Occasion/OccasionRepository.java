package com.example.MagicScreenBackend.Occasion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OccasionRepository extends JpaRepository<Occasion,Long> {
    List<Occasion> findByIsActiveTrue();

}
