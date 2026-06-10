package com.example.MagicScreenBackend.Email;

import com.example.MagicScreenBackend.Booking.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendBookingConfirmation(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(booking.getCustomerEmail());
            helper.setSubject("🎬 Booking Confirmed! Your Ticket Details - " + booking.getTrackingCode());

            // Corrected booking.getSeatCount() to booking.getTotalGuests()
            String emailContent = String.format(
                    "<h2>Thank you for your reservation!</h2>" +
                            "<p>Your theater slot booking has been officially confirmed by management.</p>" +
                            "<hr/>" +
                            "<p><strong>Tracking Code:</strong> %s</p>" +
                            "<p><strong>Theater Screen:</strong> %s</p>" +
                            "<p><strong>Date:</strong> %s</p>" +
                            "<p><strong>Start Time:</strong> %s</p>" +
                            "<p><strong>Total Guests:</strong> %d</p>" +
                            "<p><strong>Amount Paid:</strong> ₹%.2f</p>" +
                            "<hr/>" +
                            "<p>Please present this email copy at the counter upon arrival. Enjoy your private screen experience!</p>",
                    booking.getTrackingCode(),
                    booking.getSlot().getTheater().getName(),
                    booking.getSlot().getSlotDate().toString(),
                    booking.getSlot().getStartTime().toString(),
                    booking.getTotalGuests(), // Fixed here
                    booking.getTotalPrice()
            );

            helper.setText(emailContent, true);

            mailSender.send(message);
            System.out.println("Confirmation email dispatched successfully to: " + booking.getCustomerEmail());

        } catch (MessagingException e) {
            System.err.println("Failed to dispatch confirmation email: " + e.getMessage());
        }
    }
}