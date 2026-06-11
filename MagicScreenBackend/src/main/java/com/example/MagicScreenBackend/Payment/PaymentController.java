package com.example.MagicScreenBackend.Payment;

import com.example.MagicScreenBackend.Booking.Booking;
import com.example.MagicScreenBackend.Booking.BookingRepository;
import com.example.MagicScreenBackend.Booking.BookingService;
import com.example.MagicScreenBackend.Email.EmailService;
import com.example.MagicScreenBackend.Notification.TelegramService;   // ← changed
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final RazorpayService razorpayService;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final EmailService emailService;
    private final TelegramService telegramService;   // ← changed

    @PostMapping("/create-order/{bookingId}")
    public ResponseEntity<?> createOrder(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        try {
            String razorpayOrderId = razorpayService.createOrder(booking.getTotalPrice());
            booking.setRazorpayOrderId(razorpayOrderId);
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of(
                    "razorpayOrderId", razorpayOrderId,
                    "amount", booking.getTotalPrice(),
                    "keyId", razorpayService.getKeyId(),
                    "customerName", booking.getCustomerName(),
                    "customerEmail", booking.getCustomerEmail(),
                    "customerPhone", booking.getCustomerPhone()
            ));
        } catch (RazorpayException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create payment order: " + e.getMessage()));
        }
    }

    @PostMapping("/verify/{bookingId}")
    public ResponseEntity<?> verifyPayment(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> body) {

        String razorpayOrderId   = body.get("razorpayOrderId");
        String razorpayPaymentId = body.get("razorpayPaymentId");
        String razorpaySignature = body.get("razorpaySignature");

        boolean isValid = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (!isValid) {
            return ResponseEntity.status(400).body(
                    Map.of("error", "Payment signature verification failed.")
            );
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.setStatus("CONFIRMED");
        booking.setUtr(razorpayPaymentId);
        booking.getSlot().setStatus("BOOKED");
        booking.getSlot().setHeldUntil(null);

        Booking saved = bookingRepository.save(booking);

        // Send confirmation email to customer
        emailService.sendBookingConfirmation(saved);

        // Send Telegram alert to theater owner
        telegramService.sendBookingAlert(saved);   // ← changed

        return ResponseEntity.ok(Map.of(
                "status", "CONFIRMED",
                "trackingCode", saved.getTrackingCode(),
                "message", "Payment verified and booking confirmed!"
        ));
    }
}