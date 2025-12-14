package org.example.model;

public class SmallSafetyDepositBox extends SafetyDepositBox {
    private double capacity;

    public SmallSafetyDepositBox(double id, double capacity) {
        super(id);
        this.capacity = capacity;
    }

    public SmallSafetyDepositBox(double id) {
        super(id);
        this.capacity = 10.0; // Default capacity
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }
}
