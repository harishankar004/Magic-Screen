package com.example.MagicScreenBackend.Slot;

import com.example.MagicScreenBackend.Theater.Theater;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "slots")
@Data
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "slot_name")
    private String name;   // e.g. "Matinee Bliss", "Evening Premium"

    // Direct relationship link to the screen
    @ManyToOne
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate; // e.g., 2026-06-10

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // e.g., 12:00:00

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // e.g., 15:00:00

    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, HELD, BOOKED

    @Column(name = "held_until")
    private LocalDateTime heldUntil; // For the 10-minute temporary cart lock
}