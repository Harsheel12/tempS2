package org.example.service;

import org.example.model.SafetyDepositBox;
import org.example.model.SmallSafetyDepositBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SafetyDepositBoxService {

    private static SafetyDepositBoxService safetyDepositBoxService;
    private List<SafetyDepositBox> safetyDepositBoxes;

    // Maximum number of boxes that can be created
    private static int numberOfSafetyDepositBoxes;

    // Counter for generating unique box IDs
    private int boxIdCounter = 0;

    private SafetyDepositBoxService() {
        this.safetyDepositBoxes = new ArrayList<>();
    }

    public static synchronized SafetyDepositBoxService getInstance() {
        if (safetyDepositBoxService == null) {
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
        numberOfSafetyDepositBoxes = number;
        // Reset the singleton instance for clean state in tests
        safetyDepositBoxService = null;
    }

    /**
     * Gets the maximum number of safety deposit boxes allowed
     * @return Maximum number of boxes
     */
    public static int getNumberOfSafetyDepositBox() {
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
        SafetyDepositBox box = null;

        // Try to get a released (available) box from the pool
        Optional<SafetyDepositBox> releasedBox = getReleasedSafetyDepositBox();

        if (releasedBox.isPresent()) {
            // If box is available in pool then allocate it
            box = releasedBox.get();
            box.setAllotted(true);
        } else if (safetyDepositBoxes.size() < numberOfSafetyDepositBoxes) {
            // If noo box available but limit not reached then create new box
            box = createNewBox();
            box.setAllotted(true);
            safetyDepositBoxes.add(box);
        } else {
            // If no box is available and limit reached then wait for a box to be released
            try {
                while (getReleasedSafetyDepositBox().isEmpty()) {
                    wait(); // Wait until notified by releaseSafetyDepositBox
                }
                // After being notified, get the released box
                releasedBox = getReleasedSafetyDepositBox();
                if (releasedBox.isPresent()) {
                    box = releasedBox.get();
                    box.setAllotted(true);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting for safety deposit box", e);
            }
        }

        return box;
    }

    /**
     * Releases a safety deposit box back to the pool
     * Notifies waiting threads that a box is now available
     *
     * @param box The box to release
     */
    public synchronized void releaseSafetyDepositBox(SafetyDepositBox box) {
        if (box != null) {
            box.setAllotted(false);
            // Notify all waiting threads that a box has been released
            notifyAll();
        }
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
                return Optional.of(box);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the list of all safety deposit boxes in the pool
     * @return List of safety deposit boxes
     */
    public synchronized List<SafetyDepositBox> getSafetyDepositBoxes() {
        return new ArrayList<>(safetyDepositBoxes);
    }

    /**
     * Creates a new safety deposit box with a unique ID
     * Private helper method
     *
     * @return A new SmallSafetyDepositBox instance
     */
    private SafetyDepositBox createNewBox() {
        return new SmallSafetyDepositBox(++boxIdCounter);
    }
}
