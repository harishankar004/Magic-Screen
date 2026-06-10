package com.example.MagicScreenBackend.Theater;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "theaters")
@Data
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // e.g., "Screen 1 (Grand Luxe)", "Screen 2 (Couple Cozy)"

    private String theme;

    @Column(name = "base_capacity", nullable = false)
    private Integer baseCapacity = 2;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity = 10;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "extra_per_head", nullable = false)
    private BigDecimal extraPerHead = BigDecimal.ZERO;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours = 3;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
