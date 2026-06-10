package com.example.MagicScreenBackend.Admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class AdminStatsResponse {
    private BigDecimal totalRevenue;
    private long totalConfirmedBookings;
    private long totalPendingBookings;
    private long totalExpiredBookings;
    private Map<String, Long> bookingsPerTheater;
}