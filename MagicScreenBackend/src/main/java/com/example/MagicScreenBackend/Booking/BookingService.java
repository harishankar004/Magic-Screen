package com.example.MagicScreenBackend.Booking;

import com.example.MagicScreenBackend.Email.EmailService;
import com.example.MagicScreenBackend.Occasion.Occasion;
import com.example.MagicScreenBackend.Occasion.OccasionRepository;
import com.example.MagicScreenBackend.Slot.Slot;
import com.example.MagicScreenBackend.Slot.SlotRepository;
import com.example.MagicScreenBackend.Theater.Theater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final OccasionRepository RuralOccasionRepository;
    private final EmailService emailService;

    @Transactional
    public Booking initiateBooking(BookingRequest request) {
        Slot slot = slotRepository.findAndLockById(request.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found with ID: " + request.getSlotId()));

        LocalDateTime now = LocalDateTime.now();

        // Hard block — slot is permanently booked (payment confirmed)
        if ("BOOKED".equals(slot.getStatus())) {
            throw new IllegalStateException("This slot is already booked. Please select another slot.");
        }

        // Slot is HELD within the 10-minute payment window
        if ("HELD".equals(slot.getStatus()) && slot.getHeldUntil() != null && slot.getHeldUntil().isAfter(now)) {

            // Same customer retrying payment — reuse existing booking, extend the hold
            Optional<Booking> existingBooking = bookingRepository
                    .findBySlotIdAndCustomerEmailAndStatus(slot.getId(), request.getCustomerEmail(), "PENDING");

            if (existingBooking.isPresent()) {
                slot.setHeldUntil(now.plusMinutes(10));
                slotRepository.save(slot);
                return existingBooking.get();
            }

            // Different customer — block them
            throw new IllegalStateException("This slot is temporarily held. Please try another slot or wait a few minutes.");
        }

        // AVAILABLE or HELD with expired hold — proceed to book
        slot.setStatus("HELD");
        slot.setHeldUntil(now.plusMinutes(10));
        slotRepository.save(slot);

        Occasion occasion = RuralOccasionRepository.findById(request.getOccasionId())
                .orElseThrow(() -> new IllegalArgumentException("Occasion not found with ID: " + request.getOccasionId()));

        Theater theater = slot.getTheater();
        BigDecimal basePrice = theater.getBasePrice();
        BigDecimal priceModifier = occasion.getPriceModifier() != null ? occasion.getPriceModifier() : BigDecimal.ZERO;

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setOccasion(occasion);
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setTotalGuests(request.getTotalGuests());
        booking.setTotalPrice(basePrice.add(priceModifier));
        booking.setStatus("PENDING");
        booking.setTrackingCode(generateTrackingCode());

        return bookingRepository.save(booking);
    }
    private String generateTrackingCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder("MSB-");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public Booking getBookingByTrackingCode(String trackingCode) {
        return bookingRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new IllegalArgumentException("No booking found with tracking code: " + trackingCode));
    }

    public Booking updateStatus(Long id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public boolean verifyPayment(Long id, String transactionId) {
        return true;
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }
}