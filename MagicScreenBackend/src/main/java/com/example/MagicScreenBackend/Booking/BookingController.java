package com.example.MagicScreenBackend.Booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateBooking(@RequestBody BookingRequest request) {
        try {
            return ResponseEntity.ok(bookingService.initiateBooking(request));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/verify-payment")
    public ResponseEntity<?> verifyAndConfirmPayment(@PathVariable Long id, @RequestBody PaymentVerificationRequest request) {
        boolean isValid = bookingService.verifyPayment(id, request.getTransactionId());
        if (isValid) {
            bookingService.updateStatus(id, "CONFIRMED");
            return ResponseEntity.ok("Payment verified and booking confirmed.");
        }
        return ResponseEntity.status(400).body("Payment verification failed.");
    }

    @GetMapping("/track/{code}")
    public ResponseEntity<Booking> trackBooking(@PathVariable String code) {
        return ResponseEntity.ok(bookingService.getBookingByTrackingCode(code));
    }

    @GetMapping
    public ResponseEntity<Booking> getBookingByRef(@RequestParam("ref") String trackingCode) {
        return ResponseEntity.ok(bookingService.getBookingByTrackingCode(trackingCode));
    }

    // Called from frontend after booking is created — patches with the true frontend grand total
    @PatchMapping("/{id}/total")
    public ResponseEntity<?> updateTotal(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Booking booking = bookingRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
            Object grandTotalObj = body.get("grandTotal");
            if (grandTotalObj != null) {
                booking.setTotalPrice(new java.math.BigDecimal(grandTotalObj.toString()));
                bookingRepository.save(booking);
            }
            return ResponseEntity.ok(Map.of("updated", true));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}