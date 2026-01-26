package com.alfayedoficial.astagfirullah.core

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Unit tests for [VersionUtils] object.
 * Tests version comparison logic and update availability checking.
 */
@DisplayName("VersionUtils")
class VersionUtilsTest {

    @Nested
    @DisplayName("compareVersions")
    inner class CompareVersionsTest {

        @Nested
        @DisplayName("Equal Versions")
        inner class EqualVersions {

            @Test
            @DisplayName("should return 0 for identical versions")
            fun identicalVersions() {
                assertEquals(0, VersionUtils.compareVersions("2.0.0", "2.0.0"))
            }

            @Test
            @DisplayName("should return 0 for identical two-part versions")
            fun identicalTwoPartVersions() {
                assertEquals(0, VersionUtils.compareVersions("2.0", "2.0"))
            }

            @Test
            @DisplayName("should return 0 for identical single-part versions")
            fun identicalSinglePartVersions() {
                assertEquals(0, VersionUtils.compareVersions("2", "2"))
            }

            @Test
            @DisplayName("should return 0 for semantically equal versions with different lengths")
            fun semanticallyEqualDifferentLengths() {
                assertEquals(0, VersionUtils.compareVersions("2.0", "2.0.0"))
                assertEquals(0, VersionUtils.compareVersions("2.0.0", "2.0"))
                assertEquals(0, VersionUtils.compareVersions("2", "2.0.0"))
                assertEquals(0, VersionUtils.compareVersions("2.0.0.0", "2.0.0"))
            }
        }

        @Nested
        @DisplayName("Greater Versions")
        inner class GreaterVersions {

            @Test
            @DisplayName("should return positive when v1 major is greater")
            fun greaterMajorVersion() {
                assertTrue(VersionUtils.compareVersions("3.0.0", "2.0.0") > 0)
            }

            @Test
            @DisplayName("should return positive when v1 minor is greater")
            fun greaterMinorVersion() {
                assertTrue(VersionUtils.compareVersions("2.1.0", "2.0.0") > 0)
            }

            @Test
            @DisplayName("should return positive when v1 patch is greater")
            fun greaterPatchVersion() {
                assertTrue(VersionUtils.compareVersions("2.0.1", "2.0.0") > 0)
            }

            @Test
            @DisplayName("should return positive for multi-digit version parts")
            fun multiDigitVersionParts() {
                assertTrue(VersionUtils.compareVersions("2.10.0", "2.9.0") > 0)
                assertTrue(VersionUtils.compareVersions("10.0.0", "9.0.0") > 0)
            }
        }

        @Nested
        @DisplayName("Lesser Versions")
        inner class LesserVersions {

            @Test
            @DisplayName("should return negative when v1 major is lesser")
            fun lesserMajorVersion() {
                assertTrue(VersionUtils.compareVersions("1.9.0", "2.0.0") < 0)
            }

            @Test
            @DisplayName("should return negative when v1 minor is lesser")
            fun lesserMinorVersion() {
                assertTrue(VersionUtils.compareVersions("2.0.0", "2.1.0") < 0)
            }

            @Test
            @DisplayName("should return negative when v1 patch is lesser")
            fun lesserPatchVersion() {
                assertTrue(VersionUtils.compareVersions("2.0.0", "2.0.1") < 0)
            }

            @Test
            @DisplayName("should return negative for multi-digit version parts")
            fun multiDigitVersionParts() {
                assertTrue(VersionUtils.compareVersions("2.9.0", "2.10.0") < 0)
                assertTrue(VersionUtils.compareVersions("9.0.0", "10.0.0") < 0)
            }
        }

        @Nested
        @DisplayName("Different Length Versions")
        inner class DifferentLengthVersions {

            @Test
            @DisplayName("should handle shorter v1 with trailing zeros assumed")
            fun shorterV1() {
                assertTrue(VersionUtils.compareVersions("2.0", "2.0.1") < 0)
            }

            @Test
            @DisplayName("should handle shorter v2 with trailing zeros assumed")
            fun shorterV2() {
                assertTrue(VersionUtils.compareVersions("2.0.1", "2.0") > 0)
            }

            @Test
            @DisplayName("should compare versions with significantly different lengths")
            fun significantlyDifferentLengths() {
                assertTrue(VersionUtils.compareVersions("1.0.0.0.1", "1.0.0.0") > 0)
                assertTrue(VersionUtils.compareVersions("1", "1.0.0.1") < 0)
            }
        }

        @Nested
        @DisplayName("Edge Cases")
        inner class EdgeCases {

            @Test
            @DisplayName("should handle empty strings as version 0")
            fun emptyStrings() {
                assertEquals(0, VersionUtils.compareVersions("", ""))
                assertTrue(VersionUtils.compareVersions("1.0.0", "") > 0)
                assertTrue(VersionUtils.compareVersions("", "1.0.0") < 0)
            }

            @Test
            @DisplayName("should handle malformed version parts as 0")
            fun malformedVersionParts() {
                // Non-numeric parts should be treated as 0
                assertEquals(0, VersionUtils.compareVersions("abc", "def"))
                assertTrue(VersionUtils.compareVersions("1.0.0", "abc") > 0)
                assertTrue(VersionUtils.compareVersions("abc", "1.0.0") < 0)
            }

            @Test
            @DisplayName("should handle mixed valid and invalid parts")
            fun mixedValidInvalidParts() {
                // "1.abc.2" should be parsed as [1, 0, 2]
                assertTrue(VersionUtils.compareVersions("1.abc.2", "1.0.1") > 0)
                assertEquals(0, VersionUtils.compareVersions("1.abc.2", "1.0.2"))
            }

            @Test
            @DisplayName("should handle versions with leading zeros in parts")
            fun leadingZerosInParts() {
                // "01" should be parsed as 1
                assertEquals(0, VersionUtils.compareVersions("01.02.03", "1.2.3"))
            }

            @Test
            @DisplayName("should handle very large version numbers")
            fun largeVersionNumbers() {
                assertTrue(VersionUtils.compareVersions("100.200.300", "100.200.299") > 0)
                assertEquals(0, VersionUtils.compareVersions("999.999.999", "999.999.999"))
            }
        }

        @ParameterizedTest(name = "compareVersions(\"{0}\", \"{1}\") should be {2}")
        @CsvSource(
            "2.0.0, 2.0.0, 0",
            "2.1.0, 2.0.0, 1",
            "2.0.0, 2.1.0, -1",
            "3.0.0, 2.9.9, 1",
            "1.9.9, 2.0.0, -1",
            "2.0, 2.0.0, 0",
            "2.0.1, 2.0, 1"
        )
        @DisplayName("Parameterized version comparison tests")
        fun parameterizedCompareVersions(v1: String, v2: String, expectedSign: Int) {
            val result = VersionUtils.compareVersions(v1, v2)
            when {
                expectedSign > 0 -> assertTrue(result > 0, "Expected positive, got $result")
                expectedSign < 0 -> assertTrue(result < 0, "Expected negative, got $result")
                else -> assertEquals(0, result)
            }
        }
    }

    @Nested
    @DisplayName("isUpdateAvailable")
    inner class IsUpdateAvailableTest {

        @Test
        @DisplayName("should return true when server version is newer")
        fun serverVersionNewer() {
            assertTrue(VersionUtils.isUpdateAvailable("2.0.0", "2.1.0"))
            assertTrue(VersionUtils.isUpdateAvailable("2.0.0", "3.0.0"))
            assertTrue(VersionUtils.isUpdateAvailable("2.0.0", "2.0.1"))
        }

        @Test
        @DisplayName("should return false when versions are equal")
        fun versionsEqual() {
            assertFalse(VersionUtils.isUpdateAvailable("2.0.0", "2.0.0"))
        }

        @Test
        @DisplayName("should return false when current version is newer")
        fun currentVersionNewer() {
            assertFalse(VersionUtils.isUpdateAvailable("2.1.0", "2.0.0"))
            assertFalse(VersionUtils.isUpdateAvailable("3.0.0", "2.0.0"))
            assertFalse(VersionUtils.isUpdateAvailable("2.0.1", "2.0.0"))
        }

        @Test
        @DisplayName("should handle different length versions correctly")
        fun differentLengthVersions() {
            assertTrue(VersionUtils.isUpdateAvailable("2.0", "2.0.1"))
            assertFalse(VersionUtils.isUpdateAvailable("2.0.1", "2.0"))
            assertFalse(VersionUtils.isUpdateAvailable("2.0", "2.0.0"))
        }

        @Test
        @DisplayName("should handle edge cases gracefully")
        fun edgeCases() {
            // Empty current version - any server version should be an update
            assertTrue(VersionUtils.isUpdateAvailable("", "1.0.0"))

            // Empty server version - should not be an update
            assertFalse(VersionUtils.isUpdateAvailable("1.0.0", ""))

            // Both empty - should not be an update
            assertFalse(VersionUtils.isUpdateAvailable("", ""))
        }

        @ParameterizedTest(name = "isUpdateAvailable(current=\"{0}\", server=\"{1}\") should be {2}")
        @CsvSource(
            "2.0.0, 2.1.0, true",
            "2.0.0, 2.0.0, false",
            "2.1.0, 2.0.0, false",
            "1.0.0, 2.0.0, true",
            "2.0.0, 1.0.0, false"
        )
        @DisplayName("Parameterized isUpdateAvailable tests")
        fun parameterizedIsUpdateAvailable(current: String, server: String, expected: Boolean) {
            assertEquals(expected, VersionUtils.isUpdateAvailable(current, server))
        }
    }
}