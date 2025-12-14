package org.example.model;

public abstract class SafetyDepositBox {

    private double id;
    private boolean isAllotted;

    public SafetyDepositBox(double id) {
        this.id = id;
        this.isAllotted = false;
    }

    public boolean isAllotted() {
        return isAllotted;
    }

    public void setAllotted(boolean allotted) {
        this.isAllotted = allotted;
    }

    public double getId() {
        return id;
    }

    public void setId(double id) {
        this.id = id;
    }
}
