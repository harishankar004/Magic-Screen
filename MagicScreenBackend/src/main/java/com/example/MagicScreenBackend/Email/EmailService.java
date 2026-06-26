package com.example.MagicScreenBackend.Email;

import com.example.MagicScreenBackend.Booking.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(booking.getCustomerEmail());
            helper.setSubject("Your Magic Screen Booking is Confirmed! " + booking.getTrackingCode());

            BigDecimal totalPrice = booking.getTotalPrice();
            BigDecimal advancePaid = totalPrice.divide(BigDecimal.valueOf(2), 2, RoundingMode.CEILING);
            BigDecimal balanceDue = totalPrice.subtract(advancePaid);

            String emailContent = String.format(
                    "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#0D0D0D;color:#fff;padding:32px;border-radius:16px'>" +
                            "<h1 style='color:#D4A017;margin-bottom:4px'>The Magic Screen</h1>" +
                            "<p style='color:#888;margin-top:0'>Private Cinema Experience · Bhadurpally, Hyderabad</p>" +
                            "<hr style='border-color:#333;margin:24px 0'/>" +
                            "<h2 style='color:#fff'>Booking Confirmed!</h2>" +
                            "<p style='color:#aaa'>Your private theater slot has been confirmed. Please find your booking details below.</p>" +
                            "<div style='background:#1A1A1A;border-radius:12px;padding:20px;margin:20px 0'>" +
                            "<p style='margin:8px 0;color:#888'>Tracking Code: <strong style='color:#D4A017;font-size:18px'>%s</strong></p>" +
                            "<p style='margin:8px 0;color:#888'>Theater Screen: <strong style='color:#fff'>%s</strong></p>" +
                            "<p style='margin:8px 0;color:#888'>Date: <strong style='color:#fff'>%s</strong></p>" +
                            "<p style='margin:8px 0;color:#888'>Time Slot: <strong style='color:#fff'>%s</strong></p>" +
                            "<p style='margin:8px 0;color:#888'>Total Guests: <strong style='color:#fff'>%d</strong></p>" +
                            "</div>" +
                            "<div style='background:#1A1A1A;border-radius:12px;padding:20px;margin:20px 0;border:1px solid #D4A017/30'>" +
                            "<h3 style='color:#D4A017;margin-top:0'>Payment Summary</h3>" +
                            "<p style='margin:8px 0;color:#888'>Total Booking Amount: <strong style='color:#fff'>₹%.2f</strong></p>" +
                            "<p style='margin:8px 0;color:#4CAF50'>Advance Paid (50%%): <strong style='color:#4CAF50'>₹%.2f</strong></p>" +
                            "<p style='margin:8px 0;color:#888'>Balance Due at Venue: <strong style='color:#D4A017'>₹%.2f</strong></p>" +
                            "</div>" +
                            "<p style='color:#555;font-size:12px;margin-top:24px'>Please present this email at the counter on arrival. The remaining balance is to be paid at the venue before your screening begins.</p>" +
                            "<hr style='border-color:#333;margin:24px 0'/>" +
                            "<p style='color:#555;font-size:12px'>The Magic Screen · 123 Cinema Lane · Bhadurpally, Hyderabad · themagicscreen18@gmail.com</p>" +
                            "</div>",
                    booking.getTrackingCode(),
                    booking.getSlot().getTheater().getName(),
                    booking.getSlot().getSlotDate().toString(),
                    booking.getSlot().getStartTime().toString(),
                    booking.getTotalGuests(),
                    totalPrice,
                    advancePaid,
                    balanceDue
            );

            helper.setText(emailContent, true);
            mailSender.send(message);
            System.out.println("Confirmation email sent to: " + booking.getCustomerEmail());

        } catch (MessagingException e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }
}