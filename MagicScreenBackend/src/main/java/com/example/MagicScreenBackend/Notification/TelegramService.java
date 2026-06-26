package com.example.MagicScreenBackend.Notification;

import com.example.MagicScreenBackend.Booking.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.owner.chatId}")
    private String ownerChatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendBookingAlert(Booking booking) {
        try {
            BigDecimal totalPrice = booking.getTotalPrice();
            BigDecimal advancePaid = booking.getAdvancePaid() != null
                    ? booking.getAdvancePaid()
                    : totalPrice.divide(BigDecimal.valueOf(2), 2, RoundingMode.CEILING);
            BigDecimal balanceDue = totalPrice.subtract(advancePaid);

            String text = String.format(
                    "🎬 *NEW BOOKING — The Magic Screen*\n\n" +
                            "━━━━━━━━━━━━━━━━━━━━\n" +
                            "📋 *Tracking Code:* `%s`\n" +
                            "👤 *Customer:* %s\n" +
                            "📞 *Phone:* %s\n" +
                            "📧 *Email:* %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━\n" +
                            "🏛️ *Screen:* %s\n" +
                            "📅 *Date:* %s\n" +
                            "⏰ *Time:* %s → %s\n" +
                            "🎉 *Occasion:* %s\n" +
                            "👥 *Guests:* %d\n" +
                            "━━━━━━━━━━━━━━━━━━━━\n" +
                            "💰 *Total Booking Amount:* ₹%.2f\n" +
                            "✅ *Advance Paid (50%%):* ₹%.2f\n" +
                            "🔜 *Balance Due at Venue:* ₹%.2f\n" +
                            "🔖 *Payment ID:* `%s`\n" +
                            "━━━━━━━━━━━━━━━━━━━━\n" +
                            "✅ *Payment verified and confirmed!*",
                    booking.getTrackingCode(),
                    booking.getCustomerName(),
                    booking.getCustomerPhone(),
                    booking.getCustomerEmail(),
                    booking.getSlot().getTheater().getName(),
                    booking.getSlot().getSlotDate().toString(),
                    booking.getSlot().getStartTime().toString(),
                    booking.getSlot().getEndTime().toString(),
                    booking.getOccasion().getName(),
                    booking.getTotalGuests(),
                    totalPrice,
                    advancePaid,
                    balanceDue,
                    booking.getUtr() != null ? booking.getUtr() : "N/A"
            );

            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", ownerChatId);
            body.put("text", text);
            body.put("parse_mode", "Markdown");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject(url, request, String.class);

            System.out.println("Telegram alert sent successfully.");
        } catch (Exception e) {
            System.err.println("Telegram notification failed: " + e.getMessage());
        }
    }
}