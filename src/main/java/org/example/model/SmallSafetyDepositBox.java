package org.example.model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SmallSafetyDepositBox extends SafetyDepositBox {

    private static final Logger logger = LogManager.getLogger(SmallSafetyDepositBox.class);

    private double capacity;

    public SmallSafetyDepositBox(double id, double capacity) {
        super(id);
        this.capacity = capacity;
        logger.info("Created SmallSafetyDepositBox - ID: {}, Capacity: {}", id, capacity);
    }

    public SmallSafetyDepositBox(double id) {
        super(id);
        this.capacity = 10.0; // Default capacity
        logger.info("Created SmallSafetyDepositBox with default capacity - ID: {}, Capacity: {}",
                id, capacity);
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        if (capacity <= 0) {
            logger.warn("Attempted to set invalid capacity {} for box ID {}",
                    capacity, getId());
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }

        logger.debug("Box ID {} capacity changed from {} to {}",
                getId(), this.capacity, capacity);
        this.capacity = capacity;
    }
}
