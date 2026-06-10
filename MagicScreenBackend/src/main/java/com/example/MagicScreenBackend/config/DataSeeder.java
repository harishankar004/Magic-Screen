package com.example.MagicScreenBackend.config;

import com.example.MagicScreenBackend.Cake.Cake;
import com.example.MagicScreenBackend.Cake.CakeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(CakeRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                Cake c1 = new Cake(); c1.setName("Belgian Chocolate Truffle"); c1.setPrice(599);
                Cake c2 = new Cake(); c2.setName("Red Velvet Premium"); c2.setPrice(699);
                Cake c3 = new Cake(); c3.setName("Exotic Fresh Fruit"); c3.setPrice(749);
                Cake c4 = new Cake(); c4.setName("Irish Coffee Caramel"); c4.setPrice(649);

                repository.saveAll(List.of(c1, c2, c3, c4));
                System.out.println("Database seeded with 4 premium cakes!");
            }
        };
    }
}