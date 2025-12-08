package org.example.service;

public class FeeCalculatorServiceImpl implements FeeCalculatorService {
    @Override
    public double calculateFee(double balance) {
        if (balance <= 100) {
            return 20.0;
        } else if (balance <= 500) {
            return 15.0;
        } else if (balance <= 1000) {
            return 10.0;
        } else if (balance <= 2000) {
            return 5.0;
        } else {
            return 0.0;
        }
    }
}
