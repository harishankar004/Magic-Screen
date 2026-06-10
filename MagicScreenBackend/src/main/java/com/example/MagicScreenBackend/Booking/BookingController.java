package com.example.MagicScreenBackend.Booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

//    @PostMapping("/initiate")
//    public ResponseEntity<Booking> initiateBooking(@RequestBody BookingRequest request) {
//        return ResponseEntity.ok(bookingService.initiateBooking(request));
//    }


//    @PostMapping("/initiate")
//    public ResponseEntity<?> initiateBooking(@RequestBody BookingRequest request) {
//        System.out.println("DEBUG: Received request: " + request); // Watch IntelliJ console!
//
//        if (request.getSlotId() == null || request.getOccasionId() == null) {
//            System.out.println("DEBUG: Missing mandatory IDs!");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing slotId or occasionId");
//        }
//
//        try {
//            return ResponseEntity.ok(bookingService.initiateBooking(request));
//        } catch (Exception e) {
//            System.out.println("DEBUG: Service Error: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateBooking(@RequestBody BookingRequest request) {
        // This will print the raw request to your IntelliJ console
        System.out.println("DEBUG: Received Slot ID: " + request.getSlotId());
        System.out.println("DEBUG: Received Occasion ID: " + request.getOccasionId());

        // If these print "null" in IntelliJ, your frontend is not sending the data.
        return ResponseEntity.ok(bookingService.initiateBooking(request));
    }
    @GetMapping("/track/{code}")
    public ResponseEntity<Booking> trackBooking(@PathVariable String code) {
        return ResponseEntity.ok(bookingService.getBookingByTrackingCode(code));
    }

    @PatchMapping("/{id}/upi-payment")
    public ResponseEntity<Booking> submitPayment(@PathVariable Long id, @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(bookingService.submitUtrPayment(id, request.getUtr()));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
    }

    @GetMapping
    public ResponseEntity<Booking> getBookingByRef(@RequestParam("ref") String trackingCode) {
        return ResponseEntity.ok(bookingService.getBookingByTrackingCode(trackingCode));
    }


}