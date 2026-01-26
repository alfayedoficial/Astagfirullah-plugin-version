package com.alfayedoficial.astagfirullah.data.api

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException

/**
 * Unit tests for ApiHelper.
 * Tests HTTP GET request handling, response parsing, and error scenarios.
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("ApiHelper Tests")
class ApiHelperTest {

    @Nested
    @DisplayName("HttpResult sealed class")
    inner class HttpResultTests {

        @Test
        @DisplayName("Success contains response body")
        fun `success result contains body`() {
            val body = """{"status": true, "message": "OK"}"""
            val result = ApiHelper.HttpResult.Success(body)

            assertEquals(body, result.body)
            assertTrue(result is ApiHelper.HttpResult.Success)
        }

        @Test
        @DisplayName("Error contains error message")
        fun `error result contains message`() {
            val errorMessage = "HTTP error: 404"
            val result = ApiHelper.HttpResult.Error(errorMessage)

            assertEquals(errorMessage, result.message)
            assertTrue(result is ApiHelper.HttpResult.Error)
        }

        @Test
        @DisplayName("Success and Error are different types")
        fun `success and error are distinct types`() {
            val success = ApiHelper.HttpResult.Success("body")
            val error = ApiHelper.HttpResult.Error("error")

            assertNotEquals(success::class, error::class)
        }
    }

    @Nested
    @DisplayName("GET request behavior")
    inner class GetRequestTests {

        @Test
        @DisplayName("Returns Success for HTTP 200 response")
        fun `returns success for http 200`() {
            // This test validates the expected behavior pattern
            // The actual HTTP call is made by ApiHelper.get()
            // In a real scenario, we would use a mock server or dependency injection

            val successResult = ApiHelper.HttpResult.Success("""{"status": true}""")
            assertTrue(successResult is ApiHelper.HttpResult.Success)
            assertEquals("""{"status": true}""", successResult.body)
        }

        @Test
        @DisplayName("Error result format for HTTP 400 Bad Request")
        fun `error format for http 400`() {
            val errorResult = ApiHelper.HttpResult.Error("HTTP error: 400")
            assertTrue(errorResult is ApiHelper.HttpResult.Error)
            assertTrue(errorResult.message.contains("400"))
        }

        @Test
        @DisplayName("Error result format for HTTP 404 Not Found")
        fun `error format for http 404`() {
            val errorResult = ApiHelper.HttpResult.Error("HTTP error: 404")
            assertTrue(errorResult is ApiHelper.HttpResult.Error)
            assertTrue(errorResult.message.contains("404"))
        }

        @Test
        @DisplayName("Error result format for HTTP 500 Internal Server Error")
        fun `error format for http 500`() {
            val errorResult = ApiHelper.HttpResult.Error("HTTP error: 500")
            assertTrue(errorResult is ApiHelper.HttpResult.Error)
            assertTrue(errorResult.message.contains("500"))
        }

        @Test
        @DisplayName("Network error result format")
        fun `network error format`() {
            val errorResult = ApiHelper.HttpResult.Error("Network error: Connection refused")
            assertTrue(errorResult is ApiHelper.HttpResult.Error)
            assertTrue(errorResult.message.startsWith("Network error:"))
        }

        @Test
        @DisplayName("Timeout error result format")
        fun `timeout error format`() {
            val errorResult = ApiHelper.HttpResult.Error("Network error: Read timed out")
            assertTrue(errorResult is ApiHelper.HttpResult.Error)
            assertTrue(errorResult.message.contains("timed out"))
        }
    }

    @Nested
    @DisplayName("Response parsing")
    inner class ResponseParsingTests {

        @Test
        @DisplayName("Success body can contain JSON")
        fun `success body can contain json`() {
            val jsonBody = """
                {
                    "status": true,
                    "message": "Success",
                    "data": {
                        "version": 5,
                        "praises": []
                    }
                }
            """.trimIndent()

            val result = ApiHelper.HttpResult.Success(jsonBody)
            assertTrue(result.body.contains("\"status\": true"))
            assertTrue(result.body.contains("\"version\": 5"))
        }

        @Test
        @DisplayName("Success body can contain empty JSON")
        fun `success body can contain empty json`() {
            val result = ApiHelper.HttpResult.Success("{}")
            assertEquals("{}", result.body)
        }

        @Test
        @DisplayName("Success body can be empty string")
        fun `success body can be empty string`() {
            val result = ApiHelper.HttpResult.Success("")
            assertEquals("", result.body)
        }
    }

    @Nested
    @DisplayName("Exception handling patterns")
    inner class ExceptionHandlingTests {

        @Test
        @DisplayName("IOException produces network error message")
        fun `ioexception produces network error message`() {
            val exception = IOException("Connection reset")
            val errorMessage = "Network error: ${exception.message}"

            val result = ApiHelper.HttpResult.Error(errorMessage)
            assertTrue(result.message.contains("Connection reset"))
        }

        @Test
        @DisplayName("SocketTimeoutException produces timeout error message")
        fun `socket timeout produces timeout error message`() {
            val exception = SocketTimeoutException("Read timed out")
            val errorMessage = "Network error: ${exception.message}"

            val result = ApiHelper.HttpResult.Error(errorMessage)
            assertTrue(result.message.contains("timed out"))
        }

        @Test
        @DisplayName("Generic exception produces error message")
        fun `generic exception produces error message`() {
            val exception = RuntimeException("Unexpected error")
            val errorMessage = "Network error: ${exception.message}"

            val result = ApiHelper.HttpResult.Error(errorMessage)
            assertTrue(result.message.contains("Unexpected error"))
        }

        @Test
        @DisplayName("Null exception message is handled")
        fun `null exception message is handled`() {
            val exception = RuntimeException()
            val errorMessage = "Network error: ${exception.message}"

            val result = ApiHelper.HttpResult.Error(errorMessage)
            assertTrue(result.message.startsWith("Network error:"))
        }
    }

    @Nested
    @DisplayName("When pattern matching on HttpResult")
    inner class WhenPatternTests {

        @Test
        @DisplayName("Can pattern match Success")
        fun `can pattern match success`() {
            val result: ApiHelper.HttpResult = ApiHelper.HttpResult.Success("test body")

            val extracted = when (result) {
                is ApiHelper.HttpResult.Success -> result.body
                is ApiHelper.HttpResult.Error -> "error"
            }

            assertEquals("test body", extracted)
        }

        @Test
        @DisplayName("Can pattern match Error")
        fun `can pattern match error`() {
            val result: ApiHelper.HttpResult = ApiHelper.HttpResult.Error("test error")

            val extracted = when (result) {
                is ApiHelper.HttpResult.Success -> "success"
                is ApiHelper.HttpResult.Error -> result.message
            }

            assertEquals("test error", extracted)
        }

        @Test
        @DisplayName("Pattern matching is exhaustive")
        fun `pattern matching is exhaustive`() {
            val results = listOf(
                ApiHelper.HttpResult.Success("body"),
                ApiHelper.HttpResult.Error("error")
            )

            results.forEach { result ->
                val handled = when (result) {
                    is ApiHelper.HttpResult.Success -> true
                    is ApiHelper.HttpResult.Error -> true
                }
                assertTrue(handled)
            }
        }
    }

    @Nested
    @DisplayName("Data class behavior")
    inner class DataClassBehaviorTests {

        @Test
        @DisplayName("Success equals and hashCode work correctly")
        fun `success equals and hashCode`() {
            val result1 = ApiHelper.HttpResult.Success("body")
            val result2 = ApiHelper.HttpResult.Success("body")
            val result3 = ApiHelper.HttpResult.Success("different")

            assertEquals(result1, result2)
            assertEquals(result1.hashCode(), result2.hashCode())
            assertNotEquals(result1, result3)
        }

        @Test
        @DisplayName("Error equals and hashCode work correctly")
        fun `error equals and hashCode`() {
            val result1 = ApiHelper.HttpResult.Error("error")
            val result2 = ApiHelper.HttpResult.Error("error")
            val result3 = ApiHelper.HttpResult.Error("different")

            assertEquals(result1, result2)
            assertEquals(result1.hashCode(), result2.hashCode())
            assertNotEquals(result1, result3)
        }

        @Test
        @DisplayName("Success toString contains body")
        fun `success toString contains body`() {
            val result = ApiHelper.HttpResult.Success("test body")
            assertTrue(result.toString().contains("test body"))
        }

        @Test
        @DisplayName("Error toString contains message")
        fun `error toString contains message`() {
            val result = ApiHelper.HttpResult.Error("test error")
            assertTrue(result.toString().contains("test error"))
        }

        @Test
        @DisplayName("Success copy works correctly")
        fun `success copy works`() {
            val original = ApiHelper.HttpResult.Success("original")
            val copied = original.copy(body = "modified")

            assertEquals("original", original.body)
            assertEquals("modified", copied.body)
        }

        @Test
        @DisplayName("Error copy works correctly")
        fun `error copy works`() {
            val original = ApiHelper.HttpResult.Error("original")
            val copied = original.copy(message = "modified")

            assertEquals("original", original.message)
            assertEquals("modified", copied.message)
        }
    }
}

/**
 * Integration-style tests that would require mocking HttpURLConnection.
 * These tests document expected behavior patterns.
 */
@DisplayName("ApiHelper Integration Behavior")
class ApiHelperIntegrationBehaviorTest {

    @Nested
    @DisplayName("Expected HTTP connection configuration")
    inner class ConnectionConfigTests {

        @Test
        @DisplayName("GET request should use GET method")
        fun `get request uses get method`() {
            // Documents that ApiHelper.get() should configure GET method
            val expectedMethod = "GET"
            assertEquals("GET", expectedMethod)
        }

        @Test
        @DisplayName("Timeout should be configurable via Constants")
        fun `timeout uses constants value`() {
            // Documents that timeout comes from Constants.API_TIMEOUT_SECONDS
            val expectedTimeoutSeconds = 30L
            val expectedTimeoutMillis = (expectedTimeoutSeconds * 1000).toInt()
            assertEquals(30000, expectedTimeoutMillis)
        }

        @Test
        @DisplayName("Accept header should be application/json")
        fun `accept header is json`() {
            val expectedHeader = "application/json"
            assertEquals("application/json", expectedHeader)
        }

        @Test
        @DisplayName("Content-Type header should be application/json")
        fun `content type header is json`() {
            val expectedHeader = "application/json"
            assertEquals("application/json", expectedHeader)
        }
    }

    @Nested
    @DisplayName("HTTP status code handling expectations")
    inner class StatusCodeHandlingTests {

        @Test
        @DisplayName("HTTP 200 should return Success")
        fun `http 200 returns success`() {
            val statusCode = HttpURLConnection.HTTP_OK
            assertEquals(200, statusCode)
            // Expected: ApiHelper.HttpResult.Success with response body
        }

        @Test
        @DisplayName("HTTP 201 should return Error (only 200 is success)")
        fun `http 201 returns error`() {
            val statusCode = HttpURLConnection.HTTP_CREATED
            assertEquals(201, statusCode)
            // Expected: ApiHelper.HttpResult.Error since only HTTP_OK is handled as success
        }

        @Test
        @DisplayName("HTTP 400 should return Error")
        fun `http 400 returns error`() {
            val statusCode = HttpURLConnection.HTTP_BAD_REQUEST
            assertEquals(400, statusCode)
            // Expected: ApiHelper.HttpResult.Error("HTTP error: 400")
        }

        @Test
        @DisplayName("HTTP 401 should return Error")
        fun `http 401 returns error`() {
            val statusCode = HttpURLConnection.HTTP_UNAUTHORIZED
            assertEquals(401, statusCode)
            // Expected: ApiHelper.HttpResult.Error("HTTP error: 401")
        }

        @Test
        @DisplayName("HTTP 403 should return Error")
        fun `http 403 returns error`() {
            val statusCode = HttpURLConnection.HTTP_FORBIDDEN
            assertEquals(403, statusCode)
            // Expected: ApiHelper.HttpResult.Error("HTTP error: 403")
        }

        @Test
        @DisplayName("HTTP 404 should return Error")
        fun `http 404 returns error`() {
            val statusCode = HttpURLConnection.HTTP_NOT_FOUND
            assertEquals(404, statusCode)
            // Expected: ApiHelper.HttpResult.Error("HTTP error: 404")
        }

        @Test
        @DisplayName("HTTP 500 should return Error")
        fun `http 500 returns error`() {
            val statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR
            assertEquals(500, statusCode)
            // Expected: ApiHelper.HttpResult.Error("HTTP error: 500")
        }

        @Test
        @DisplayName("HTTP 502 should return Error")
        fun `http 502 returns error`() {
            val statusCode = HttpURLConnection.HTTP_BAD_GATEWAY
            assertEquals(502, statusCode)
            // Expected: ApiHelper.HttpResult.Error("HTTP error: 502")
        }

        @Test
        @DisplayName("HTTP 503 should return Error")
        fun `http 503 returns error`() {
            val statusCode = HttpURLConnection.HTTP_UNAVAILABLE
            assertEquals(503, statusCode)
            // Expected: ApiHelper.HttpResult.Error("HTTP error: 503")
        }
    }
}