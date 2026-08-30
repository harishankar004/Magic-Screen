package com.example.MagicScreenBackend.Booking;

import com.example.MagicScreenBackend.Occasion.Occasion;
import com.example.MagicScreenBackend.Slot.Slot;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "bookings"})
    private Slot slot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "occasion_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Occasion occasion;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "total_guests", nullable = false)
    private Integer totalGuests;

    @Column(name = "tracking_code", nullable = false, unique = true)
    private String trackingCode;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "payment_Id", length = 50)
    private String utr;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "advance_paid")
    private BigDecimal advancePaid;
}