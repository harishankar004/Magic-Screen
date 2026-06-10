package com.example.MagicScreenBackend.Admin;

import com.example.MagicScreenBackend.Booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final BookingRepository bookingRepository;

    public AdminStatsResponse getDashboardStatistics() {
        // Fetch revenue (handle null case if no bookings are confirmed yet)
        BigDecimal totalRevenue = bookingRepository.sumTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        // Fetch lifecycle counters
        long confirmed = bookingRepository.countByStatus("CONFIRMED");
        long pending = bookingRepository.countByStatus("PENDING");
        long expired = bookingRepository.countByStatus("EXPIRED");

        // Parse theater performance list into a clean map
        List<Object[]> theaterData = bookingRepository.countBookingsPerTheater();
        Map<String, Long> bookingsPerTheater = new HashMap<>();
        for (Object[] row : theaterData) {
            bookingsPerTheater.put((String) row[0], (Long) row[1]);
        }

        return new AdminStatsResponse(
                totalRevenue,
                confirmed,
                pending,
                expired,
                bookingsPerTheater
        );
    }
}