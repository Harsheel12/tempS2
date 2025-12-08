package org.example.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeeCalculatorServiceImplTest {
    private final FeeCalculatorService service = new FeeCalculatorServiceImpl();

    @Test
    void testFeeForBalanceUpTo100() {
        assertEquals(20.0, service.calculateFee(0));
        assertEquals(20.0, service.calculateFee(100));
    }

    @Test
    void testFeeForBalance101To500() {
        assertEquals(15.0, service.calculateFee(150));
        assertEquals(15.0, service.calculateFee(500));
    }

    @Test
    void testFeeForBalance501To1000() {
        assertEquals(10.0, service.calculateFee(750));
        assertEquals(10.0, service.calculateFee(1000));
    }

    @Test
    void testFeeForBalance1001To2000() {
        assertEquals(5.0, service.calculateFee(1500));
        assertEquals(5.0, service.calculateFee(2000));
    }

    @Test
    void testFeeAbove2000() {
        assertEquals(0.0, service.calculateFee(2500));
        assertEquals(0.0, service.calculateFee(10000));
    }
}
