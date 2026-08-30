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
            if (booking == null) {
                System.err.println("❌ Email skipped — booking is null");
                return;
            }
            if (booking.getCustomerEmail() == null || booking.getCustomerEmail().isBlank()) {
                System.err.println("❌ Email skipped — customer email is empty");
                return;
            }

            System.out.println("📧 Sending email to: " + booking.getCustomerEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("themagicscreen18@gmail.com");
            helper.setTo(booking.getCustomerEmail());
            helper.setSubject("✅ Booking Confirmed — " + booking.getTrackingCode() + " | The Magic Screen");

            // Safely extract slot/theater/occasion — all EAGER loaded now
            String theaterName  = "—";
            String slotDate     = "—";
            String slotTime     = "—";
            String slotEndTime  = "—";
            String occasionName = "—";

            if (booking.getSlot() != null) {
                if (booking.getSlot().getTheater() != null) {
                    theaterName = booking.getSlot().getTheater().getName();
                }
                if (booking.getSlot().getSlotDate() != null) {
                    slotDate = booking.getSlot().getSlotDate().toString();
                }
                if (booking.getSlot().getStartTime() != null) {
                    slotTime = booking.getSlot().getStartTime().toString();
                }
                if (booking.getSlot().getEndTime() != null) {
                    slotEndTime = booking.getSlot().getEndTime().toString();
                }
            }

            if (booking.getOccasion() != null) {
                occasionName = booking.getOccasion().getName();
            }

            BigDecimal totalPrice  = booking.getTotalPrice() != null ? booking.getTotalPrice() : BigDecimal.ZERO;
            BigDecimal advancePaid = booking.getAdvancePaid() != null
                    ? booking.getAdvancePaid()
                    : totalPrice.divide(BigDecimal.valueOf(2), 2, RoundingMode.CEILING);
            BigDecimal balanceDue  = totalPrice.subtract(advancePaid);

            String html =
                    "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#0D0D0D;color:#ffffff;padding:32px;border-radius:16px;border:1px solid #222'>" +

                            // Header
                            "<div style='text-align:center;padding-bottom:24px;border-bottom:1px solid #222;margin-bottom:24px'>" +
                            "<h1 style='color:#D4A017;margin:0;font-size:26px;letter-spacing:1px'>🎬 THE MAGIC SCREEN</h1>" +
                            "<p style='color:#888;margin:6px 0 0;font-size:13px'>Private Cinema Experience · Bhadurpally, Hyderabad</p>" +
                            "</div>" +

                            // Confirmed banner
                            "<div style='background:#D4A017;border-radius:10px;padding:16px 20px;margin-bottom:24px;text-align:center'>" +
                            "<h2 style='color:#000;margin:0;font-size:20px'>✅ Booking Confirmed!</h2>" +
                            "<p style='color:#333;margin:4px 0 0;font-size:13px'>Your private theater experience is all set.</p>" +
                            "</div>" +

                            // Tracking code
                            "<div style='text-align:center;margin-bottom:24px'>" +
                            "<p style='color:#888;font-size:12px;margin:0'>TRACKING CODE</p>" +
                            "<p style='color:#D4A017;font-size:28px;font-weight:bold;font-family:monospace;margin:4px 0'>" + booking.getTrackingCode() + "</p>" +
                            "<p style='color:#555;font-size:11px;margin:0'>Keep this safe — you will need it at the venue</p>" +
                            "</div>" +

                            // Booking details
                            "<div style='background:#1A1A1A;border-radius:12px;padding:20px;margin-bottom:16px'>" +
                            "<h3 style='color:#D4A017;margin:0 0 14px;font-size:14px;text-transform:uppercase;letter-spacing:1px'>📋 Booking Details</h3>" +
                            row("Theater Screen", theaterName) +
                            row("Date", slotDate) +
                            row("Time Slot", slotTime + " → " + slotEndTime) +
                            row("Occasion", occasionName) +
                            row("Total Guests", String.valueOf(booking.getTotalGuests())) +
                            row("Customer Name", booking.getCustomerName()) +
                            row("Phone", booking.getCustomerPhone()) +
                            "</div>" +

                            // Payment
                            "<div style='background:#1A1A1A;border-radius:12px;padding:20px;margin-bottom:16px'>" +
                            "<h3 style='color:#D4A017;margin:0 0 14px;font-size:14px;text-transform:uppercase;letter-spacing:1px'>💳 Payment Summary</h3>" +
                            row("Total Booking Amount", "₹" + String.format("%.2f", totalPrice)) +
                            rowColored("✅ Advance Paid (50%)", "₹" + String.format("%.2f", advancePaid), "#4CAF50") +
                            rowColored("⏳ Balance Due at Venue", "₹" + String.format("%.2f", balanceDue), "#D4A017") +
                            "</div>" +

                            // Note
                            "<div style='background:#111;border-radius:10px;padding:14px 16px;border-left:3px solid #D4A017;margin-bottom:24px'>" +
                            "<p style='color:#888;font-size:12px;margin:0;line-height:1.7'>" +
                            "📌 Please show this email at the reception upon arrival. " +
                            "The remaining balance of <strong style='color:#D4A017'>₹" + String.format("%.2f", balanceDue) + "</strong> " +
                            "is to be paid at the venue before your screening begins." +
                            "</p>" +
                            "</div>" +

                            // Footer
                            "<div style='text-align:center;border-top:1px solid #222;padding-top:20px'>" +
                            "<p style='color:#555;font-size:11px;margin:0'>The Magic Screen · Bhadurpally, Hyderabad</p>" +
                            "<p style='color:#555;font-size:11px;margin:4px 0 0'>📧 themagicscreen18@gmail.com</p>" +
                            "</div>" +

                            "</div>";

            helper.setText(html, true);
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + booking.getCustomerEmail());

        } catch (MessagingException e) {
            System.err.println("❌ MessagingException sending email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String row(String label, String value) {
        return "<div style='display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #222'>" +
                "<span style='color:#888;font-size:13px'>" + label + "</span>" +
                "<span style='color:#fff;font-size:13px;font-weight:bold'>" + (value != null ? value : "—") + "</span>" +
                "</div>";
    }

    private String rowColored(String label, String value, String color) {
        return "<div style='display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px solid #222'>" +
                "<span style='color:" + color + ";font-size:13px'>" + label + "</span>" +
                "<span style='color:" + color + ";font-size:13px;font-weight:bold'>" + (value != null ? value : "—") + "</span>" +
                "</div>";
    }
}