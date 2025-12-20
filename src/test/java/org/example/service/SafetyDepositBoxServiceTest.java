package org.example.service;

import org.example.model.SafetyDepositBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class SafetyDepositBoxServiceTest {

    private SafetyDepositBoxService service;

    @BeforeEach
    public void setUp() {
        // Reset the singleton instance before each test
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(2);
        service = SafetyDepositBoxService.getInstance();
    }

    @AfterEach
    public void tearDown() {
        // Clean up after each test
        service = null;
    }

    /**
     * Test 1: Verify that when number of threads equals maximum boxes,
     * no thread is kept waiting
     */
    @Test
    public void testAllocateSafetyDepositBox_WhenThreadsEqualMaxBoxes_NoThreadWaits()
            throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(2);

        Runnable task = () -> {
            try {
                startLatch.await();
                SafetyDepositBox box = service.allocateSafetyDepositBox();
                assertNotNull(box, "Thread should receive a box");

                Thread.sleep(5000); // Hold box for 5 seconds

                service.releaseSafetyDepositBox(box);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completionLatch.countDown();
            }
        };

        Thread thread1 = new Thread(task, "Thread-1");
        Thread thread2 = new Thread(task, "Thread-2");

        thread1.start();
        thread2.start();

        startLatch.countDown(); // Release all threads to start simultaneously

        completionLatch.await(); // Wait for all threads to complete

        // Assert - No thread should have waited
        assertFalse(service.isWaiting(),
                "No thread should be kept waiting when thread count equals max boxes");
    }

    /**
     * Test 2: Verify that when number of threads exceeds maximum boxes,
     * at least one thread is kept waiting
     */
    @Test
    public void testAllocateSafetyDepositBox_WhenThreadsExceedMaxBoxes_ThreadWaits()
            throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch allocationLatch = new CountDownLatch(2); // First 2 threads allocated
        CountDownLatch completionLatch = new CountDownLatch(3);
        AtomicBoolean threadWaited = new AtomicBoolean(false);

        Runnable task = () -> {
            try {
                startLatch.await();
                SafetyDepositBox box = service.allocateSafetyDepositBox();
                assertNotNull(box, "Thread should eventually receive a box");

                allocationLatch.countDown();
                Thread.sleep(5000); // Hold box for 5 seconds

                service.releaseSafetyDepositBox(box);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completionLatch.countDown();
            }
        };

        Thread thread1 = new Thread(task, "Thread-1");
        Thread thread2 = new Thread(task, "Thread-2");
        Thread thread3 = new Thread(task, "Thread-3");

        thread1.start();
        thread2.start();
        thread3.start();

        // Release all threads to start simultaneously
        startLatch.countDown();

        // Wait for first 2 threads to get boxes
        allocationLatch.await();

        // Give a brief moment for thread 3 to enter wait state
        Thread.sleep(100);

        // Check if the waiting flag is set (meaning thread 3 is waiting)
        if (service.isWaiting()) {
            threadWaited.set(true);
        }

        // Wait for all threads to complete
        completionLatch.await();

        // At least one thread should have waited
        assertTrue(threadWaited.get(),
                "At least one thread should be kept waiting when thread count exceeds max boxes");
    }

    /**
     * Test singleton pattern - getInstance should return same instance
     */
    @Test
    public void testGetInstance_ReturnsSameInstance() {
        SafetyDepositBoxService instance1 = SafetyDepositBoxService.getInstance();
        SafetyDepositBoxService instance2 = SafetyDepositBoxService.getInstance();

        assertSame(instance1, instance2);
    }

    /**
     * Test allocating a box when pool is empty but limit not reached
     */
    @Test
    public void testAllocateSafetyDepositBox_WhenPoolEmptyAndLimitNotReached_CreatesNewBox() {
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(3);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();

        SafetyDepositBox box = testService.allocateSafetyDepositBox();

        assertNotNull(box, "Should create and return a new box");
        assertTrue(box.isAllotted(), "Allocated box should be marked as allotted");
    }

    /**
     * Test getting number of available boxes
     */
    @Test
    public void testGetNumberOfAvailableSafetyDepositBoxes_ReturnsCorrectCount() {
        // Initially no boxes created
        assertEquals(0, service.getNumberOfAvailableSafetyDepositBoxes(),
                "Initially should have 0 available boxes");

        // Allocate one box
        SafetyDepositBox box1 = service.allocateSafetyDepositBox();
        assertEquals(0, service.getNumberOfAvailableSafetyDepositBoxes(),
                "After allocating 1 box, should have 0 available");

        // Release the box
        service.releaseSafetyDepositBox(box1);
        assertEquals(1, service.getNumberOfAvailableSafetyDepositBoxes(),
                "After releasing 1 box, should have 1 available");
    }

    /**
     * Test releasing a safety deposit box returns it to pool
     */
    @Test
    public void testReleaseSafetyDepositBox_ReturnsBoxToPool() {
        SafetyDepositBox box = service.allocateSafetyDepositBox();

        service.releaseSafetyDepositBox(box);

        assertFalse(box.isAllotted(), "Released box should not be allotted");
        assertEquals(1, service.getNumberOfAvailableSafetyDepositBoxes(),
                "Should have 1 available box after release");
    }

    /**
     * Test that getReleasedSafetyDepositBox returns empty when no boxes available
     */
    @Test
    public void testGetReleasedSafetyDepositBox_WhenNoBoxesAvailable_ReturnsEmpty() {
        // Only 1 box available this time
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(1);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();

        testService.allocateSafetyDepositBox(); // Allocate the only box

        var result = testService.getReleasedSafetyDepositBox();

        assertFalse(result.isPresent(),
                "Should return empty Optional when no boxes available");
    }

    /**
     * Test that getReleasedSafetyDepositBox returns a box when available
     */
    @Test
    public void testGetReleasedSafetyDepositBox_WhenBoxAvailable_ReturnsBox() {
        SafetyDepositBox box = service.allocateSafetyDepositBox();
        service.releaseSafetyDepositBox(box);

        var result = service.getReleasedSafetyDepositBox();

        assertTrue(result.isPresent(),
                "Should return a box when one is available");
        assertFalse(result.get().isAllotted(),
                "Returned box should not be allotted");
    }

    /**
     * Test setting and getting number of safety deposit boxes
     */
    @Test
    public void testSetAndGetNumberOfSafetyDepositBoxes() {
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(5);

        assertEquals(5, SafetyDepositBoxService.getNumberOfSafetyDepositBox(),
                "Should return the set number of safety deposit boxes");
    }

    @Test
    public void testReleaseSafetyDepositBox_WhenBoxIsNull_HandlesGracefully() {
        assertDoesNotThrow(() -> service.releaseSafetyDepositBox(null),
                "Releasing null box should be handled gracefully");
    }

    @Test
    public void testSetNumberOfSafetyDepositBoxes_WhenNegative_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(-1),
                "Should throw exception for negative number");
    }

    @Test
    public void testSetNumberOfSafetyDepositBoxes_WhenZero_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(0),
                "Should throw exception for zero");
    }

    @Test
    public void testGetSafetyDepositBoxes_ReturnsDefaultSize() {
        var boxes1 = service.getSafetyDepositBoxes();
        int originalSize = boxes1.size();

        assertEquals(0, originalSize,
                "Original size should be 0");
    }
}
