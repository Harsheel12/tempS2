package org.example.model;

public class CheckingAccount extends Account {

    private int nextCheckNumber = 1;

    public CheckingAccount() {
        super();
    }

    public int getNextCheckNumber() {
        int current = nextCheckNumber;
        nextCheckNumber++;   // Increment for the next check
        return current;
    }

    public void setNextCheckNumber(int nextCheckNumber) {
        this.nextCheckNumber = nextCheckNumber;
    }

    // Custom method to return check number without incrementing it
    public int peekNextCheckNumber() {
        return nextCheckNumber;
    }
}
