package com.example.MagicScreenBackend.Payment;

import com.example.MagicScreenBackend.Booking.Booking;
import com.example.MagicScreenBackend.Booking.BookingRepository;
import com.example.MagicScreenBackend.Booking.BookingService;
import com.example.MagicScreenBackend.Email.EmailService;
import com.example.MagicScreenBackend.Notification.TelegramService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final RazorpayService razorpayService;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final EmailService emailService;
    private final TelegramService telegramService;

    @PostMapping("/create-order/{bookingId}")
    public ResponseEntity<?> createOrder(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

            // Calculate 50% advance from the grand total (already patched by frontend)
            BigDecimal totalPrice = booking.getTotalPrice();
            if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid booking total price."));
            }

            BigDecimal advanceAmount = totalPrice.divide(BigDecimal.valueOf(2), 2, RoundingMode.CEILING);

            String razorpayOrderId = razorpayService.createOrder(advanceAmount);
            booking.setRazorpayOrderId(razorpayOrderId);
            booking.setAdvancePaid(advanceAmount);
            bookingRepository.save(booking);

            return ResponseEntity.ok(Map.of(
                    "razorpayOrderId", razorpayOrderId,
                    "amount", advanceAmount,
                    "keyId", razorpayService.getKeyId(),
                    "customerName", booking.getCustomerName(),
                    "customerEmail", booking.getCustomerEmail(),
                    "customerPhone", booking.getCustomerPhone()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (RazorpayException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Razorpay order creation failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Unexpected error: " + e.getMessage()));
        }
    }

    @PostMapping("/verify/{bookingId}")
    public ResponseEntity<?> verifyPayment(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> body) {

        try {
            String razorpayOrderId = body.get("razorpayOrderId");
            String razorpayPaymentId = body.get("razorpayPaymentId");
            String razorpaySignature = body.get("razorpaySignature");

            if (razorpayOrderId == null || razorpayPaymentId == null || razorpaySignature == null) {
                return ResponseEntity.status(400).body(Map.of("error", "Missing payment verification fields."));
            }

            boolean isValid = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

            if (!isValid) {
                return ResponseEntity.status(400).body(Map.of("error", "Payment signature verification failed."));
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            booking.setStatus("CONFIRMED");
            booking.setUtr(razorpayPaymentId);
            booking.getSlot().setStatus("BOOKED");
            booking.getSlot().setHeldUntil(null);

            Booking saved = bookingRepository.save(booking);

            // Fire-and-forget email and telegram — don't let these crash the response
            try { emailService.sendBookingConfirmation(saved); } catch (Exception e) {
                System.err.println("Email failed: " + e.getMessage());
            }
            try { telegramService.sendBookingAlert(saved); } catch (Exception e) {
                System.err.println("Telegram failed: " + e.getMessage());
            }

            return ResponseEntity.ok(Map.of(
                    "status", "CONFIRMED",
                    "trackingCode", saved.getTrackingCode(),
                    "message", "Payment verified and booking confirmed!"
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Verification failed: " + e.getMessage()));
        }
    }
}