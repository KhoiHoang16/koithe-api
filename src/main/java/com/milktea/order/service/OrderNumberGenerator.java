package com.milktea.order.service;

import com.milktea.order.repository.OrderNumberSequenceRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {
    private final OrderNumberSequenceRepository sequence;
    public OrderNumberGenerator(OrderNumberSequenceRepository sequence) { this.sequence = sequence; }
    public String next() {
        int year = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).getYear();
        return "DH-%d-%03d".formatted(year, sequence.next(year));
    }
}
