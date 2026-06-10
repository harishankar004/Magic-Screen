package com.example.MagicScreenBackend.Cake;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cakes")
@Data
public class Cake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private double price; // Ensure this is a double/double primitive

    @Column(name = "image_url")
    private String imageUrl;
}
