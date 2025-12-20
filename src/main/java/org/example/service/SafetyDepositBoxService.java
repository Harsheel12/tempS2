package org.example.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.example.model.SafetyDepositBox;
import org.example.model.SmallSafetyDepositBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SafetyDepositBoxService {

    private static final Logger logger = LogManager.getLogger(SafetyDepositBoxService.class);

    private static SafetyDepositBoxService safetyDepositBoxService;
    private List<SafetyDepositBox> safetyDepositBoxes;

    // Maximum number of boxes that can be created
    private static int numberOfSafetyDepositBoxes;

    // Wait flag used for testing purposes
    private volatile boolean isWaiting = false;

    // Counter for generating unique box IDs
    private int boxIdCounter = 0;

    private SafetyDepositBoxService() {
        this.safetyDepositBoxes = new ArrayList<>();
        logger.info("SafetyDepositBoxService instance created");
    }

    public static synchronized SafetyDepositBoxService getInstance() {
        if (safetyDepositBoxService == null) {
            logger.info("Creating new SafetyDepositBoxService singleton instance");
            safetyDepositBoxService = new SafetyDepositBoxService();
        }
        return safetyDepositBoxService;
    }

    /**
     * Sets the maximum number of safety deposit boxes allowed
     * Also resets the singleton instance to ensure clean state
     * @param number Maximum number of boxes
     */
    public static synchronized void setNumberOfSafetyDepositBoxes(int number) {
        if (number <= 0) {
            logger.error("Attempted to set invalid number of safety deposit boxes: {}", number);
            throw new IllegalArgumentException("Number of safety deposit boxes must be greater than 0");
        }

        logger.info("Setting maximum number of safety deposit boxes to: {}", number);
        numberOfSafetyDepositBoxes = number;

        if (safetyDepositBoxService != null) {
            logger.warn("Resetting SafetyDepositBoxService singleton instance");
        }
        // Reset the singleton instance for clean state in tests
        safetyDepositBoxService = null;
    }

    /**
     * Gets the maximum number of safety deposit boxes allowed
     * @return Maximum number of boxes
     */
    public static int getNumberOfSafetyDepositBox() {
        logger.debug("Retrieved maximum number of safety deposit boxes: {}", numberOfSafetyDepositBoxes);
        return numberOfSafetyDepositBoxes;
    }

    /**
     * Allocates a safety deposit box to a client
     * This method is synchronized to ensure thread safety
     *
     * Behavior:
     * 1. If a box is available in the pool, return it
     * 2. If no box is available but limit not reached, create and return a new box
     * 3. If no box is available and limit reached, wait until a box is released
     *
     * @return An allocated SafetyDepositBox
     */
    public synchronized SafetyDepositBox allocateSafetyDepositBox() {
        logger.info("Thread {} requesting safety deposit box allocation",
                Thread.currentThread().getName());

        SafetyDepositBox box = null;

        // Try to get a released (available) box from the pool
        Optional<SafetyDepositBox> releasedBox = getReleasedSafetyDepositBox();

        if (releasedBox.isPresent()) {
            // If box is available in pool then allocate it
            box = releasedBox.get();
            box.setAllotted(true);
            logger.info("Thread {} allocated existing box ID {} from pool",
                    Thread.currentThread().getName(), box.getId());
        } else if (safetyDepositBoxes.size() < numberOfSafetyDepositBoxes) {
            // If no box available but limit not reached then create new box
            logger.debug("No box available, creating new box. Current count: {}, Max: {}",
                    safetyDepositBoxes.size(), numberOfSafetyDepositBoxes);
            box = createNewBox();
            box.setAllotted(true);
            safetyDepositBoxes.add(box);
            logger.info("Thread {} allocated newly created box ID {}",
                    Thread.currentThread().getName(), box.getId());
        } else {
            // If no box is available and limit reached then wait for a box to be released
            logger.warn("Thread {} waiting - No boxes available and maximum limit ({}) reached",
                    Thread.currentThread().getName(), numberOfSafetyDepositBoxes);
            try {
                isWaiting = true;
                while (getReleasedSafetyDepositBox().isEmpty()) {
                    logger.debug("Thread {} entering wait state", Thread.currentThread().getName());
                    wait(); // Wait until notified by releaseSafetyDepositBox
                    logger.debug("Thread {} woke up from wait state", Thread.currentThread().getName());
                }
                isWaiting = false;

                // After being notified, get the released box
                releasedBox = getReleasedSafetyDepositBox();
                if (releasedBox.isPresent()) {
                    box = releasedBox.get();
                    box.setAllotted(true);
                    logger.info("Thread {} allocated box ID {} after waiting",
                            Thread.currentThread().getName(), box.getId());
                } else {
                    logger.error("Thread {} woke up but no box available - this should not happen",
                            Thread.currentThread().getName());
                }
            } catch (InterruptedException e) {
                isWaiting = false;
                logger.error("Thread {} interrupted while waiting for safety deposit box",
                        Thread.currentThread().getName(), e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting for safety deposit box", e);
            }
        }

        logger.info("Thread {} successfully allocated box ID {}. Available boxes: {}/{}",
                Thread.currentThread().getName(),
                box != null ? box.getId() : "null",
                getNumberOfAvailableSafetyDepositBoxes(),
                safetyDepositBoxes.size());

        return box;
    }

    /**
     * Releases a safety deposit box back to the pool
     * Notifies waiting threads that a box is now available
     *
     * @param box The box to release
     */
    public synchronized void releaseSafetyDepositBox(SafetyDepositBox box) {
        if (box == null) {
            logger.warn("Attempted to release null safety deposit box");
            return;
        }

        logger.info("Thread {} releasing box ID {}",
                Thread.currentThread().getName(), box.getId());

        box.setAllotted(false);

        // Notify all waiting threads that a box has been released
        notifyAll();
        logger.debug("Notified all waiting threads that box ID {} is now available", box.getId());

        logger.info("Box ID {} released. Available boxes: {}/{}",
                box.getId(),
                getNumberOfAvailableSafetyDepositBoxes(),
                safetyDepositBoxes.size());
    }

    /**
     * Gets the number of available (not allotted) safety deposit boxes
     * This method is synchronized to ensure thread safety
     *
     * @return Number of available boxes
     */
    public synchronized int getNumberOfAvailableSafetyDepositBoxes() {
        int count = 0;
        for (SafetyDepositBox box : safetyDepositBoxes) {
            if (!box.isAllotted()) {
                count++;
            }
        }
        logger.debug("Current available boxes count: {}", count);
        return count;
    }

    /**
     * Searches for and returns the first available (not allotted) safety deposit box
     * This method is synchronized to ensure thread safety
     *
     * @return Optional containing the first available box, or empty if none available
     */
    public synchronized Optional<SafetyDepositBox> getReleasedSafetyDepositBox() {
        for (SafetyDepositBox box : safetyDepositBoxes) {
            if (!box.isAllotted()) {
                logger.debug("Found available box ID {}", box.getId());
                return Optional.of(box);
            }
        }
        logger.debug("No available boxes found in pool");
        return Optional.empty();
    }

    /**
     * Gets the list of all safety deposit boxes in the pool
     * @return List of safety deposit boxes
     */
    public synchronized List<SafetyDepositBox> getSafetyDepositBoxes() {
        logger.debug("Retrieved list of all safety deposit boxes. Total count: {}",
                safetyDepositBoxes.size());
        return new ArrayList<>(safetyDepositBoxes);
    }

    /**
     * Creates a new safety deposit box with a unique ID
     * Private helper method
     *
     * @return A new SmallSafetyDepositBox instance
     */
    private SafetyDepositBox createNewBox() {
        boxIdCounter++;
        logger.debug("Creating new box with ID: {}", boxIdCounter);
        return new SmallSafetyDepositBox(boxIdCounter);
    }

    public synchronized boolean isWaiting() {
        return isWaiting;
    }
}
