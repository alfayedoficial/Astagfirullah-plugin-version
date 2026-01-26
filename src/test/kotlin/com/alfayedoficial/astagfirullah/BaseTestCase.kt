package com.alfayedoficial.astagfirullah

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.mockito.MockitoAnnotations

/**
 * Base test class providing common setup and utilities for all tests.
 * Extend this class to get automatic Mockito initialization and common test helpers.
 */
abstract class BaseTestCase {

    protected lateinit var testInfo: TestInfo

    @BeforeEach
    fun baseSetUp(testInfo: TestInfo) {
        this.testInfo = testInfo
        MockitoAnnotations.openMocks(this)
        setUp()
    }

    /**
     * Override this method in subclasses to perform additional setup.
     * Called after Mockito mocks are initialized.
     */
    protected open fun setUp() {
        // Default implementation does nothing
    }

    /**
     * Helper to get the current test name for logging/debugging.
     */
    protected fun getTestName(): String = testInfo.displayName
}