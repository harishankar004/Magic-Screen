package com.example.MagicScreenBackend.Booking;

import lombok.Data;

@Data
public class PaymentVerificationRequest {
    private String transactionId;
    private String gatewayResponse;
}