package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SafetyDepositBox {

    private static final Logger logger = LogManager.getLogger(SafetyDepositBox.class);

    private double id;
    private boolean isAllotted;

    public SafetyDepositBox(double id) {
        this.id = id;
        this.isAllotted = false;
        logger.debug("Created SafetyDepositBox with ID: {}", id);
    }

    public boolean isAllotted() {
        return isAllotted;
    }

    public void setAllotted(boolean allotted) {
        logger.debug("Box ID {} allotment status changed from {} to {}",
                id, this.isAllotted, allotted);
        this.isAllotted = allotted;

        if (allotted) {
            logger.info("Box ID {} has been allocated", id);
        } else {
            logger.info("Box ID {} has been released", id);
        }
    }

    public double getId() {
        return id;
    }

    public void setId(double id) {
        logger.debug("Box ID changed from {} to {}", this.id, id);
        this.id = id;
    }
}
