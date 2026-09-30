package com.milktea.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.milktea.order.repository.OrderNumberSequenceRepository;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class OrderNumberGeneratorTest {
    @Test void concurrentCallsProduceUniqueSequentialCodes() throws Exception {
        OrderNumberSequenceRepository sequence = org.mockito.Mockito.mock(OrderNumberSequenceRepository.class);
        AtomicLong value = new AtomicLong();
        when(sequence.next(anyInt())).thenAnswer(call -> value.incrementAndGet());
        OrderNumberGenerator generator = new OrderNumberGenerator(sequence);
        Set<String> generated = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(12);
        try {
            var futures = IntStream.range(0, 600).mapToObj(i -> pool.submit(() -> generated.add(generator.next()))).toList();
            for (Future<Boolean> future : futures) future.get();
        } finally { pool.shutdownNow(); }
        assertEquals(600, generated.size());
        assertEquals(600L, value.get());
    }
}
