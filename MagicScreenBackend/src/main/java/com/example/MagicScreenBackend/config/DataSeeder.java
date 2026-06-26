package com.example.MagicScreenBackend.config;

import com.example.MagicScreenBackend.Cake.Cake;
import com.example.MagicScreenBackend.Cake.CakeRepository;
import com.example.MagicScreenBackend.Occasion.Occasion;
import com.example.MagicScreenBackend.Occasion.OccasionRepository;
import com.example.MagicScreenBackend.Slot.Slot;
import com.example.MagicScreenBackend.Slot.SlotRepository;
import com.example.MagicScreenBackend.Theater.Theater;
import com.example.MagicScreenBackend.Theater.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(
            CakeRepository cakeRepo,
            OccasionRepository occasionRepo,
            TheaterRepository theaterRepo,
            SlotRepository slotRepo
    ) {
        return args -> {

            // ── CAKES ──────────────────────────────────────────────
            if (cakeRepo.count() == 0) {
                Cake c1 = new Cake(); c1.setName("Belgian Chocolate Truffle"); c1.setPrice(599);
                Cake c2 = new Cake(); c2.setName("Red Velvet Premium");        c2.setPrice(699);
                Cake c3 = new Cake(); c3.setName("Exotic Fresh Fruit");        c3.setPrice(749);
                Cake c4 = new Cake(); c4.setName("Irish Coffee Caramel");      c4.setPrice(649);
                cakeRepo.saveAll(List.of(c1, c2, c3, c4));
                System.out.println("✅ Seeded 4 cakes");
            }

            // ── OCCASIONS ─────────────────────────────────────────
            if (occasionRepo.count() == 0) {
                String[][] occasions = {
                        {"Birthday Celebration",  "0"},
                        {"Anniversary Special",   "500"},
                        {"Romantic Proposal",     "1000"},
                        {"Date Night",            "0"},
                        {"Live Match Screening",  "0"},
                        {"Custom Party",          "0"},
                };
                for (String[] o : occasions) {
                    Occasion occ = new Occasion();
                    occ.setName(o[0]);
                    occ.setPriceModifier(new BigDecimal(o[1]));
                    occ.setIsActive(true);
                    occasionRepo.save(occ);
                }
                System.out.println("✅ Seeded 6 occasions");
            }

            // ── THEATERS (all 4 matching the frontend) ────────────
            if (theaterRepo.count() == 0) {
                Object[][] theaters = {
                        {"Blue",          "Classic Cinema",   4,  10, "799",  "150", 3, "120 inch 4K screen. Up to 4 guests included."},
                        {"Gold",          "Gold Premium",     4,  10, "999",  "150", 3, "133 inch 4K screen. Most popular choice."},
                        {"Red Love",      "Romantic Suite",   2,  2,  "1199", "0",   3, "120 inch 4K screen. Couple only private suite."},
                        {"Jail Dark Cell","Dark Experience",  4,  10, "1399", "150", 3, "133 inch 4K immersive dark cell experience."},
                };
                for (Object[] t : theaters) {
                    Theater theater = new Theater();
                    theater.setName((String) t[0]);
                    theater.setTheme((String) t[1]);
                    theater.setBaseCapacity((Integer) t[2]);
                    theater.setMaxCapacity((Integer) t[3]);
                    theater.setBasePrice(new BigDecimal((String) t[4]));
                    theater.setExtraPerHead(new BigDecimal((String) t[5]));
                    theater.setDurationHours((Integer) t[6]);
                    theater.setDescription((String) t[7]);
                    theater.setIsActive(true);
                    theaterRepo.save(theater);
                }
                System.out.println("✅ Seeded 4 theaters");
            }

            // ── SLOTS for next 7 days ─────────────────────────────
            List<Theater> theaters = theaterRepo.findAll();
            LocalTime[] startTimes = {
                    LocalTime.of(9, 0),
                    LocalTime.of(13, 0),
                    LocalTime.of(17, 0),
                    LocalTime.of(21, 0)
            };
            String[] slotNames = {"Morning Bliss", "Afternoon Premium", "Evening Deluxe", "Night Luxe"};

            int slotCount = 0;
            for (Theater theater : theaters) {
                for (int dayOffset = 0; dayOffset <= 7; dayOffset++) {
                    LocalDate date = LocalDate.now().plusDays(dayOffset);
                    for (int i = 0; i < startTimes.length; i++) {
                        boolean exists = slotRepo.existsByTheaterIdAndSlotDateAndStartTime(
                                theater.getId(), date, startTimes[i]);
                        if (!exists) {
                            Slot slot = new Slot();
                            slot.setTheater(theater);
                            slot.setSlotDate(date);
                            slot.setStartTime(startTimes[i]);
                            slot.setEndTime(startTimes[i].plusHours(theater.getDurationHours()));
                            slot.setStatus("AVAILABLE");
                            slot.setName(slotNames[i]);
                            slotRepo.save(slot);
                            slotCount++;
                        }
                    }
                }
            }
            if (slotCount > 0) System.out.println("✅ Seeded " + slotCount + " slots");
        };
    }
}