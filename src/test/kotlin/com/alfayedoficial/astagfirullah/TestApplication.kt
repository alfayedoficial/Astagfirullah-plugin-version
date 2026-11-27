package com.alfayedoficial.astagfirullah

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * A mocked IntelliJ [Application] for plain JUnit tests, so `XService.getInstance()` can be
 * pointed at a test double.
 *
 * Why not `mockStatic(XService::class.java)`: every service's `getInstance()` lives in a
 * Kotlin `companion object`. Even with `@JvmStatic`, Kotlin call sites compile it as an
 * instance call on `XService.Companion`, so a static mock never intercepts it -- that was
 * the cause of the MissingMethodInvocationException / WrongTypeOfReturnValue failures.
 * Every `getInstance()` ends in `ApplicationManager.getApplication().getService(...)`,
 * a genuine Java static, so mocking that one seam works for all of them.
 *
 * Use in `@BeforeEach` and [close] it in `@AfterEach`.
 */
class TestApplication : AutoCloseable {

    val application: Application = mock()

    private val applicationManager: MockedStatic<ApplicationManager> =
        mockStatic(ApplicationManager::class.java).also {
            it.`when`<Application> { ApplicationManager.getApplication() }.thenReturn(application)
        }

    /** Makes `type`'s `getInstance()` return [instance]. */
    fun <T : Any> register(type: Class<T>, instance: T): T {
        whenever(application.getService(type)).thenReturn(instance)
        return instance
    }

    override fun close() = applicationManager.close()
}
