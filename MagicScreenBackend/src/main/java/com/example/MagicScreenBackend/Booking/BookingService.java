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

    @Transactional
    public Booking submitUtrPayment(Long bookingId, String utr) {
        // 1. Verify the UTR is exactly 12 digits long
        if (utr == null || !utr.matches("\\d{12}")) {
            throw new IllegalArgumentException("Invalid UPI UTR! Must be a 12-digit numeric reference number.");
        }

        // 2. Locate the booking record
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking details not found for ID: " + bookingId));

        // 3. Attach the UTR proof
        booking.setUtr(utr);

        // Note: The status stays "PENDING" here until an admin confirms the receipt manually inside their dashboard
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking confirmBooking(Long bookingId) {
        // 1. Locate the pending booking record
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking record not found for ID: " + bookingId));

        // 2. Ensure we aren't trying to confirm a booking that was already cleaned up/expired
        if ("EXPIRED".equals(booking.getStatus())) {
            throw new IllegalStateException("Cannot confirm this booking; the 10-minute hold window has already expired.");
        }

        // 3. Update the booking status to permanent success
        booking.setStatus("CONFIRMED");
        // Optional safety check: Ensure payment reference exists before confirmation
        if (booking.getUtr() == null || booking.getUtr().isBlank()) {
            throw new IllegalStateException("Cannot confirm booking: No payment UTR has been submitted yet.");
        }

        // 4. Finalize the associated slot so it is fully locked out from the public wizard layout
        Slot slot = booking.getSlot();
        slot.setStatus("BOOKED");
        slot.setHeldUntil(null); // Clear out the temporary hold timestamp anchor completely

        slotRepository.save(slot);
        Booking savedBooking = bookingRepository.save(booking);

        // 5. Asynchronously hand over the saved database entity state to trigger the email notification
        emailService.sendBookingConfirmation(savedBooking);

        return savedBooking;
    }

    public Booking getBookingByTrackingCode(String trackingCode) {
        return bookingRepository.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new IllegalArgumentException("No booking found with tracking code: " + trackingCode));
    }

    public Booking save(Booking booking) {
        // Here you can add business logic (e.g., setting default status, calculating total)
        return bookingRepository.save(booking);
    }
}