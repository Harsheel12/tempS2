package org.example.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Account {

    private static final Logger logger = LogManager.getLogger(SafetyDepositBox.class);

    private static long nextAccountId = 1_000;

    private final long ACCOUNT_ID;

    protected double balance = 0;

    public Account() {
        this.ACCOUNT_ID = nextAccountId;
        logger.debug("Created Account with ID: {}", nextAccountId);
        nextAccountId += 5; // Increment by 5 for every new account
    }

    public double withdraw(double amount) {
        this.balance -= amount;
        logger.info("Original balance is ${}, after withdrawing it became ${}", getBalance(), getBalance() - amount);
        return amount;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }

    public void correctBalance(double amount) {
        this.balance = amount;
    }

    public long getACCOUNT_ID() {
        return ACCOUNT_ID;
    }

    public double getBalance() {
        return balance;
    }
}
