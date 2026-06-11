package com.example.MagicScreenBackend.Booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // 1. Customer initiates the booking
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateBooking(@RequestBody BookingRequest request) {
        // We trust the service layer to handle the DB logic
        return ResponseEntity.ok(bookingService.initiateBooking(request));
    }

    // 2. Automated Payment Verification (The "Fully Automated" Workflow)
    // This is called by your Payment Gateway Webhook or your verification service
    @PostMapping("/{id}/verify-payment")
    public ResponseEntity<?> verifyAndConfirmPayment(@PathVariable Long id, @RequestBody PaymentVerificationRequest request) {
        // Logic: Verify gateway signature here to ensure the payment is genuine
        boolean isValid = bookingService.verifyPayment(id, request.getTransactionId());

        if (isValid) {
            bookingService.updateStatus(id, "CONFIRMED");
            return ResponseEntity.ok("Payment verified and booking confirmed.");
        }
        return ResponseEntity.status(400).body("Payment verification failed.");
    }


    // 4. Tracking and fetching existing bookings
    @GetMapping("/track/{code}")
    public ResponseEntity<Booking> trackBooking(@PathVariable String code) {
        return ResponseEntity.ok(bookingService.getBookingByTrackingCode(code));
    }

    @GetMapping
    public ResponseEntity<Booking> getBookingByRef(@RequestParam("ref") String trackingCode) {
        return ResponseEntity.ok(bookingService.getBookingByTrackingCode(trackingCode));
    }
}