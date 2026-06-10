package com.example.MagicScreenBackend.Booking;

import lombok.Data;

@Data
public class BookingRequest {
    private Long slotId;
    private Long occasionId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Integer totalGuests;
}