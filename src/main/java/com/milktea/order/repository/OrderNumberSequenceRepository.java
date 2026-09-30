package com.milktea.order.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderNumberSequenceRepository {
    private final JdbcTemplate jdbc;
    public OrderNumberSequenceRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long next(int year) {
        Long value = jdbc.queryForObject("INSERT INTO order_sequence(nam,last_value) VALUES (?,1) " +
                "ON CONFLICT (nam) DO UPDATE SET last_value=order_sequence.last_value+1 RETURNING last_value", Long.class, year);
        return value == null ? 1 : value;
    }
}
