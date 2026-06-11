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
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final OccasionRepository RuralOccasionRepository;
    private final EmailService emailService; // Injected email communication service

    @Transactional
    public Booking initiateBooking(BookingRequest request) {
        // 1. Lock and retrieve the slot row from the database to handle race conditions
        Slot slot = slotRepository.findAndLockById(request.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot not found with ID: " + request.getSlotId()));

        LocalDateTime now = LocalDateTime.now();

        // 2. Concurrency Check: Verify if slot is already occupied or held by someone else
        if (slot.getStatus().equals("BOOKED")) {
            throw new IllegalStateException("This slot is already fully booked.");
        }

        if (slot.getStatus().equals("HELD") && slot.getHeldUntil() != null && slot.getHeldUntil().isAfter(now)) {
            throw new IllegalStateException("This slot is temporarily held by another customer.");
        }

        // 3. Mark slot as HELD for a 10-minute temporary expiration window
        slot.setStatus("HELD");
        slot.setHeldUntil(now.plusMinutes(10));
        slotRepository.save(slot);

        // 4. Fetch the selected occasion
        Occasion occasion = RuralOccasionRepository.findById(request.getOccasionId())
                .orElseThrow(() -> new IllegalArgumentException("Occasion template not found with ID: " + request.getOccasionId()));

        // 5. Pricing Engine Calculation
        Theater theater = slot.getTheater();
        BigDecimal basePrice = theater.getBasePrice();
        BigDecimal priceModifier = occasion.getPriceModifier();

        // Calculate extra charges for additional guests beyond base room capacity
        BigDecimal extraCharges = BigDecimal.ZERO;
        if (request.getTotalGuests() > theater.getBaseCapacity()) {
            int extraGuests = request.getTotalGuests() - theater.getBaseCapacity();
            extraCharges = theater.getExtraPerHead().multiply(BigDecimal.valueOf(extraGuests));
        }

        BigDecimal totalCalculatedPrice = basePrice.add(priceModifier).add(extraCharges);

        // 6. Build and save the Booking instance
        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setOccasion(occasion);
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setTotalGuests(request.getTotalGuests());
        booking.setTotalPrice(totalCalculatedPrice);
        booking.setTrackingCode(generateTrackingCode());

        return bookingRepository.save(booking);
    }

    // Generates a random alphanumeric tracking reference: MSB-XXXXXX
    private String generateTrackingCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Removed ambiguous characters like 0, O, 1, I
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
        // 1. Add logic here to talk to your Payment Gateway API (Stripe/Razorpay)
        // to check if transactionId is actually valid.
        // 2. Return true if payment is found and successful.
        return true; // Simplified for now
    }

    public Booking save(Booking booking) {
        // Here you can add business logic (e.g., setting default status, calculating total)
        return bookingRepository.save(booking);
    }
}