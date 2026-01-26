package com.alfayedoficial.astagfirullah

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit 5 extension for testing coroutines.
 * Sets up a test dispatcher and replaces the Main dispatcher.
 *
 * Usage:
 * ```
 * @ExtendWith(CoroutineTestExtension::class)
 * class MyTest {
 *     @Test
 *     fun `test coroutine`() = runTest {
 *         // your test code
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineTestExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }
}