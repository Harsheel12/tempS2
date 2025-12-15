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
        // Arrange
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(2);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(2);
        AtomicBoolean anyThreadWaited = new AtomicBoolean(false);

        // Act - Create two threads
        Thread thread1 = new Thread(() -> {
            try {
                startLatch.await(); // Wait for all threads to be ready
                SafetyDepositBox box = testService.allocateSafetyDepositBox();
                assertNotNull(box, "Thread 1 should receive a box");

                Thread.sleep(5000); // Hold box for 5 seconds

                testService.releaseSafetyDepositBox(box);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completionLatch.countDown();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                startLatch.await(); // Wait for all threads to be ready
                SafetyDepositBox box = testService.allocateSafetyDepositBox();
                assertNotNull(box, "Thread 2 should receive a box");

                Thread.sleep(5000); // Hold box for 5 seconds

                testService.releaseSafetyDepositBox(box);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completionLatch.countDown();
            }
        });

        thread1.start();
        thread2.start();

        startLatch.countDown(); // Release all threads to start simultaneously
        completionLatch.await(); // Wait for all threads to complete

        // Assert - No thread should have waited
        assertFalse(anyThreadWaited.get(),
                "No thread should be kept waiting when thread count equals max boxes");
    }

    /**
     * Test 2: Verify that when number of threads exceeds maximum boxes,
     * at least one thread is kept waiting
     */
    @Test
    public void testAllocateSafetyDepositBox_WhenThreadsExceedMaxBoxes_ThreadWaits()
            throws InterruptedException {
        // Arrange
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(2);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(3);
        AtomicBoolean threadWaited = new AtomicBoolean(false);
        AtomicInteger allocatedCount = new AtomicInteger(0);

        // Act - Create three threads
        Runnable task = () -> {
            try {
                startLatch.await(); // Wait for all threads to be ready

                long startTime = System.currentTimeMillis();
                SafetyDepositBox box = testService.allocateSafetyDepositBox();
                long endTime = System.currentTimeMillis();

                assertNotNull(box, "Thread should eventually receive a box");

                int currentAlloc = allocatedCount.incrementAndGet();

                // If this is the third thread and it took more than 1 second to allocate,
                // it likely waited
                if (currentAlloc == 3 && (endTime - startTime) > 1000) {
                    threadWaited.set(true);
                }

                Thread.sleep(5000); // Hold box for 5 seconds

                testService.releaseSafetyDepositBox(box);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completionLatch.countDown();
            }
        };

        Thread thread1 = new Thread(task);
        Thread thread2 = new Thread(task);
        Thread thread3 = new Thread(task);

        thread1.start();
        thread2.start();
        thread3.start();

        startLatch.countDown(); // Release all threads to start simultaneously
        completionLatch.await(); // Wait for all threads to complete

        // Assert - At least one thread should have waited
        assertTrue(threadWaited.get(),
                "At least one thread should be kept waiting when thread count exceeds max boxes");
    }

    /**
     * Test singleton pattern - getInstance should return same instance
     */
    @Test
    public void testGetInstance_ReturnsSameInstance() {
        // Act
        SafetyDepositBoxService instance1 = SafetyDepositBoxService.getInstance();
        SafetyDepositBoxService instance2 = SafetyDepositBoxService.getInstance();

        // Assert
        assertSame(instance1, instance2);
    }

    /**
     * Test allocating a box when pool is empty but limit not reached
     */
    @Test
    public void testAllocateSafetyDepositBox_WhenPoolEmptyAndLimitNotReached_CreatesNewBox() {
        // Arrange
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(3);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();

        // Act
        SafetyDepositBox box = testService.allocateSafetyDepositBox();

        // Assert
        assertNotNull(box, "Should create and return a new box");
        assertTrue(box.isAllotted(), "Allocated box should be marked as allotted");
    }

    /**
     * Test getting number of available boxes
     */
    @Test
    public void testGetNumberOfAvailableSafetyDepositBoxes_ReturnsCorrectCount() {
        // Arrange
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(3);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();

        // Act & Assert - Initially no boxes created
        assertEquals(0, testService.getNumberOfAvailableSafetyDepositBoxes(),
                "Initially should have 0 available boxes");

        // Allocate one box
        SafetyDepositBox box1 = testService.allocateSafetyDepositBox();
        assertEquals(0, testService.getNumberOfAvailableSafetyDepositBoxes(),
                "After allocating 1 box, should have 0 available");

        // Release the box
        testService.releaseSafetyDepositBox(box1);
        assertEquals(1, testService.getNumberOfAvailableSafetyDepositBoxes(),
                "After releasing 1 box, should have 1 available");
    }

    /**
     * Test releasing a safety deposit box returns it to pool
     */
    @Test
    public void testReleaseSafetyDepositBox_ReturnsBoxToPool() {
        // Arrange
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(2);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();
        SafetyDepositBox box = testService.allocateSafetyDepositBox();

        // Act
        testService.releaseSafetyDepositBox(box);

        // Assert
        assertFalse(box.isAllotted(), "Released box should not be allotted");
        assertEquals(1, testService.getNumberOfAvailableSafetyDepositBoxes(),
                "Should have 1 available box after release");
    }

    /**
     * Test that getReleasedSafetyDepositBox returns empty when no boxes available
     */
    @Test
    public void testGetReleasedSafetyDepositBox_WhenNoBoxesAvailable_ReturnsEmpty() {
        // Arrange
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(1);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();
        testService.allocateSafetyDepositBox(); // Allocate the only box

        // Act
        var result = testService.getReleasedSafetyDepositBox();

        // Assert
        assertFalse(result.isPresent(),
                "Should return empty Optional when no boxes available");
    }

    /**
     * Test that getReleasedSafetyDepositBox returns a box when available
     */
    @Test
    public void testGetReleasedSafetyDepositBox_WhenBoxAvailable_ReturnsBox() {
        // Arrange
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(2);
        SafetyDepositBoxService testService = SafetyDepositBoxService.getInstance();
        SafetyDepositBox box = testService.allocateSafetyDepositBox();
        testService.releaseSafetyDepositBox(box);

        // Act
        var result = testService.getReleasedSafetyDepositBox();

        // Assert
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
        // Act
        SafetyDepositBoxService.setNumberOfSafetyDepositBoxes(5);

        // Assert
        assertEquals(5, SafetyDepositBoxService.getNumberOfSafetyDepositBox(),
                "Should return the set number of safety deposit boxes");
    }

}
