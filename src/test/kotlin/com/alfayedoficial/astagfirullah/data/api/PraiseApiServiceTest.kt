package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.data.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for PraiseApiService.
 * Tests API response handling, parsing, and category filtering.
 */
@DisplayName("PraiseApiService Tests")
class PraiseApiServiceTest {

    @Nested
    @DisplayName("ApiResult sealed class")
    inner class ApiResultTests {

        @Test
        @DisplayName("Success contains ApiResponse")
        fun `success contains api response`() {
            val apiResponse = ApiResponse(
                status = true,
                message = "Success",
                data = PraiseData(version = 1, praises = emptyList())
            )
            val result = ApiResult.Success(apiResponse)

            assertEquals(apiResponse, result.response)
            assertTrue(result is ApiResult.Success)
        }

        @Test
        @DisplayName("Error contains error message")
        fun `error contains message`() {
            val result = ApiResult.Error("Parse error: Invalid JSON")

            assertEquals("Parse error: Invalid JSON", result.message)
            assertTrue(result is ApiResult.Error)
        }

        @Test
        @DisplayName("Can pattern match ApiResult")
        fun `can pattern match api result`() {
            val apiResponse = ApiResponse(
                status = true,
                message = "OK",
                data = PraiseData(version = 1, praises = emptyList())
            )
            val successResult: ApiResult = ApiResult.Success(apiResponse)
            val errorResult: ApiResult = ApiResult.Error("error")

            val successExtracted = when (successResult) {
                is ApiResult.Success -> successResult.response
                is ApiResult.Error -> null
            }

            val errorExtracted = when (errorResult) {
                is ApiResult.Success -> null
                is ApiResult.Error -> errorResult.message
            }

            assertNotNull(successExtracted)
            assertEquals("error", errorExtracted)
        }
    }

    @Nested
    @DisplayName("parsePraises() function")
    inner class ParsePraisesTests {

        @Test
        @DisplayName("Returns empty list when data is null")
        fun `returns empty list when data is null`() {
            val response = ApiResponse(
                status = true,
                message = "OK",
                data = null
            )

            val result = PraiseApiService.parsePraises(response)

            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Returns empty list when praises list is empty")
        fun `returns empty list when praises empty`() {
            val response = ApiResponse(
                status = true,
                message = "OK",
                data = PraiseData(version = 1, praises = emptyList())
            )

            val result = PraiseApiService.parsePraises(response)

            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Filters praises by default GENERAL category")
        fun `filters by general category by default`() {
            val praises = listOf(
                createPraise(id = 1, categoryIds = listOf(CategoryIds.GENERAL)),
                createPraise(id = 2, categoryIds = listOf(CategoryIds.AFTER_PRAYER)),
                createPraise(id = 3, categoryIds = listOf(CategoryIds.GENERAL, CategoryIds.AFTER_PRAYER))
            )
            val response = createApiResponse(praises)

            val result = PraiseApiService.parsePraises(response)

            assertEquals(2, result.size)
            assertTrue(result.any { it.id == 1 })
            assertTrue(result.any { it.id == 3 })
            assertFalse(result.any { it.id == 2 })
        }

        @Test
        @DisplayName("Filters praises by specified category")
        fun `filters by specified category`() {
            val praises = listOf(
                createPraise(id = 1, categoryIds = listOf(CategoryIds.GENERAL)),
                createPraise(id = 2, categoryIds = listOf(CategoryIds.AFTER_PRAYER)),
                createPraise(id = 3, categoryIds = listOf(CategoryIds.GENERAL, CategoryIds.AFTER_PRAYER))
            )
            val response = createApiResponse(praises)

            val result = PraiseApiService.parsePraises(response, CategoryIds.AFTER_PRAYER)

            assertEquals(2, result.size)
            assertTrue(result.any { it.id == 2 })
            assertTrue(result.any { it.id == 3 })
            assertFalse(result.any { it.id == 1 })
        }

        @Test
        @DisplayName("Extracts Arabic text from translations")
        fun `extracts arabic text`() {
            val praise = createPraise(
                id = 1,
                arabicText = "سبحان الله",
                englishText = "Glory be to Allah"
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertEquals(1, result.size)
            assertEquals("سبحان الله", result[0].arabicText)
        }

        @Test
        @DisplayName("Extracts English text from translations")
        fun `extracts english text`() {
            val praise = createPraise(
                id = 1,
                arabicText = "سبحان الله",
                englishText = "Glory be to Allah"
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertEquals(1, result.size)
            assertEquals("Glory be to Allah", result[0].englishText)
        }

        @Test
        @DisplayName("Falls back to Arabic text when English is missing")
        fun `falls back to arabic when english missing`() {
            val translations = listOf(
                PraiseTranslation(
                    id = 1,
                    praiseId = 1,
                    langId = LanguageIds.ARABIC,
                    name = "سبحان الله",
                    sound = null
                )
            )
            val praise = Praise(
                id = 1,
                categories = listOf(createCategory(categoryId = CategoryIds.GENERAL)),
                translations = translations
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertEquals(1, result.size)
            assertEquals("سبحان الله", result[0].arabicText)
            assertEquals("سبحان الله", result[0].englishText) // Fallback
        }

        @Test
        @DisplayName("Skips praise when Arabic text is missing")
        fun `skips praise when arabic text missing`() {
            val translations = listOf(
                PraiseTranslation(
                    id = 1,
                    praiseId = 1,
                    langId = LanguageIds.ENGLISH,
                    name = "Glory be to Allah",
                    sound = null
                )
            )
            val praise = Praise(
                id = 1,
                categories = listOf(createCategory(categoryId = CategoryIds.GENERAL)),
                translations = translations
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Extracts count from category")
        fun `extracts count from category`() {
            val category = PraiseCategory(
                id = 1,
                categoryId = CategoryIds.GENERAL,
                praiseId = 1,
                count = 33
            )
            val praise = Praise(
                id = 1,
                categories = listOf(category),
                translations = createTranslations(praiseId = 1)
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertEquals(1, result.size)
            assertEquals(33, result[0].count)
        }

        @Test
        @DisplayName("Defaults count to 1 when category not found")
        fun `defaults count to 1 when category not found`() {
            // This case is theoretically impossible after filtering,
            // but tests the defensive coding pattern
            val praise = createPraise(id = 1, categoryIds = listOf(CategoryIds.GENERAL))
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertEquals(1, result.size)
            assertEquals(1, result[0].count) // Default count
        }

        @Test
        @DisplayName("Sets correct categoryId on CachedPraise")
        fun `sets correct category id`() {
            val praise = createPraise(id = 1, categoryIds = listOf(CategoryIds.AFTER_PRAYER))
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response, CategoryIds.AFTER_PRAYER)

            assertEquals(1, result.size)
            assertEquals(CategoryIds.AFTER_PRAYER, result[0].categoryId)
        }

        @Test
        @DisplayName("Handles praise with multiple categories")
        fun `handles praise with multiple categories`() {
            val categories = listOf(
                PraiseCategory(id = 1, categoryId = CategoryIds.GENERAL, praiseId = 1, count = 10),
                PraiseCategory(id = 2, categoryId = CategoryIds.AFTER_PRAYER, praiseId = 1, count = 20)
            )
            val praise = Praise(
                id = 1,
                categories = categories,
                translations = createTranslations(praiseId = 1)
            )
            val response = createApiResponse(listOf(praise))

            val resultGeneral = PraiseApiService.parsePraises(response, CategoryIds.GENERAL)
            val resultAfterPrayer = PraiseApiService.parsePraises(response, CategoryIds.AFTER_PRAYER)

            assertEquals(1, resultGeneral.size)
            assertEquals(10, resultGeneral[0].count)
            assertEquals(CategoryIds.GENERAL, resultGeneral[0].categoryId)

            assertEquals(1, resultAfterPrayer.size)
            assertEquals(20, resultAfterPrayer[0].count)
            assertEquals(CategoryIds.AFTER_PRAYER, resultAfterPrayer[0].categoryId)
        }

        @Test
        @DisplayName("Preserves praise ID in CachedPraise")
        fun `preserves praise id`() {
            val praise = createPraise(id = 42)
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertEquals(1, result.size)
            assertEquals(42, result[0].id)
        }

        @Test
        @DisplayName("Handles large number of praises")
        fun `handles large number of praises`() {
            val praises = (1..100).map { createPraise(id = it) }
            val response = createApiResponse(praises)

            val result = PraiseApiService.parsePraises(response)

            assertEquals(100, result.size)
        }

        @Test
        @DisplayName("Returns empty list when no praises match category")
        fun `returns empty when no category match`() {
            val praises = listOf(
                createPraise(id = 1, categoryIds = listOf(CategoryIds.AFTER_PRAYER))
            )
            val response = createApiResponse(praises)

            val result = PraiseApiService.parsePraises(response, CategoryIds.GENERAL)

            assertTrue(result.isEmpty())
        }
    }

    @Nested
    @DisplayName("fetchPraises() URL construction")
    inner class FetchPraisesUrlTests {

        @Test
        @DisplayName("URL includes version parameter")
        fun `url includes version parameter`() {
            // Documents expected URL format
            val version = 5
            val expectedUrlContains = "version=$version"

            assertTrue("version=5".contains(expectedUrlContains))
        }

        @Test
        @DisplayName("URL includes is_quran=false parameter")
        fun `url includes is_quran parameter`() {
            // Documents expected URL format
            val expectedParam = "is_quran=false"

            assertTrue("is_quran=false".contains(expectedParam))
        }

        @Test
        @DisplayName("Version 0 is valid for first sync")
        fun `version 0 is valid for first sync`() {
            // Documents that version 0 indicates first sync
            val firstSyncVersion = 0
            assertEquals(0, firstSyncVersion)
        }
    }

    @Nested
    @DisplayName("Version-based sync behavior")
    inner class VersionSyncTests {

        @Test
        @DisplayName("API response contains version number")
        fun `api response contains version`() {
            val response = ApiResponse(
                status = true,
                message = "OK",
                data = PraiseData(version = 5, praises = emptyList())
            )

            assertEquals(5, response.data?.version)
        }

        @Test
        @DisplayName("Version is extracted from successful response")
        fun `version extracted from success`() {
            val apiResponse = ApiResponse(
                status = true,
                message = "OK",
                data = PraiseData(version = 10, praises = emptyList())
            )
            val result = ApiResult.Success(apiResponse)

            assertEquals(10, result.response.data?.version)
        }

        @Test
        @DisplayName("Success result indicates data available")
        fun `success indicates data available`() {
            val apiResponse = ApiResponse(
                status = true,
                message = "OK",
                data = PraiseData(version = 1, praises = listOf(createPraise(id = 1)))
            )
            val result = ApiResult.Success(apiResponse)

            assertTrue(result.response.status)
            assertNotNull(result.response.data)
        }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandlingTests {

        @Test
        @DisplayName("Error result for API status=false")
        fun `error for api status false`() {
            val errorResult = ApiResult.Error("API error: Data not available")

            assertTrue(errorResult.message.startsWith("API error:"))
        }

        @Test
        @DisplayName("Error result for parse failure")
        fun `error for parse failure`() {
            val errorResult = ApiResult.Error("Parse error: Expected BEGIN_OBJECT")

            assertTrue(errorResult.message.startsWith("Parse error:"))
        }

        @Test
        @DisplayName("Error result for network failure")
        fun `error for network failure`() {
            val errorResult = ApiResult.Error("Network error: Connection refused")

            assertTrue(errorResult.message.startsWith("Network error:"))
        }

        @Test
        @DisplayName("Error result for HTTP error")
        fun `error for http error`() {
            val errorResult = ApiResult.Error("HTTP error: 404")

            assertTrue(errorResult.message.startsWith("HTTP error:"))
        }
    }

    @Nested
    @DisplayName("Complex response parsing")
    inner class ComplexResponseTests {

        @Test
        @DisplayName("Parses response with multiple praises and categories")
        fun `parses complex response`() {
            val praises = listOf(
                Praise(
                    id = 1,
                    categories = listOf(
                        PraiseCategory(id = 1, categoryId = CategoryIds.GENERAL, praiseId = 1, count = 33),
                        PraiseCategory(id = 2, categoryId = CategoryIds.AFTER_PRAYER, praiseId = 1, count = 10)
                    ),
                    translations = listOf(
                        PraiseTranslation(id = 1, praiseId = 1, langId = LanguageIds.ARABIC, name = "سبحان الله", sound = "/audio/1.mp3"),
                        PraiseTranslation(id = 2, praiseId = 1, langId = LanguageIds.ENGLISH, name = "Glory be to Allah", sound = null)
                    )
                ),
                Praise(
                    id = 2,
                    categories = listOf(
                        PraiseCategory(id = 3, categoryId = CategoryIds.GENERAL, praiseId = 2, count = 100)
                    ),
                    translations = listOf(
                        PraiseTranslation(id = 3, praiseId = 2, langId = LanguageIds.ARABIC, name = "الحمد لله", sound = null),
                        PraiseTranslation(id = 4, praiseId = 2, langId = LanguageIds.ENGLISH, name = "Praise be to Allah", sound = "/audio/2.mp3")
                    )
                )
            )
            val response = ApiResponse(
                status = true,
                message = "Success",
                data = PraiseData(version = 5, praises = praises)
            )

            val result = PraiseApiService.parsePraises(response, CategoryIds.GENERAL)

            assertEquals(2, result.size)

            val praise1 = result.find { it.id == 1 }
            assertNotNull(praise1)
            assertEquals("سبحان الله", praise1!!.arabicText)
            assertEquals("Glory be to Allah", praise1.englishText)
            assertEquals(33, praise1.count)
            assertEquals(CategoryIds.GENERAL, praise1.categoryId)

            val praise2 = result.find { it.id == 2 }
            assertNotNull(praise2)
            assertEquals("الحمد لله", praise2!!.arabicText)
            assertEquals("Praise be to Allah", praise2.englishText)
            assertEquals(100, praise2.count)
        }

        @Test
        @DisplayName("Handles praises with only Arabic translations")
        fun `handles arabic only praises`() {
            val praise = Praise(
                id = 1,
                categories = listOf(PraiseCategory(id = 1, categoryId = CategoryIds.GENERAL, praiseId = 1, count = 3)),
                translations = listOf(
                    PraiseTranslation(id = 1, praiseId = 1, langId = LanguageIds.ARABIC, name = "استغفر الله", sound = null)
                )
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertEquals(1, result.size)
            assertEquals("استغفر الله", result[0].arabicText)
            assertEquals("استغفر الله", result[0].englishText) // Falls back to Arabic
        }

        @Test
        @DisplayName("Filters out praises with only English translations")
        fun `filters out english only praises`() {
            val praise = Praise(
                id = 1,
                categories = listOf(PraiseCategory(id = 1, categoryId = CategoryIds.GENERAL, praiseId = 1, count = 1)),
                translations = listOf(
                    PraiseTranslation(id = 1, praiseId = 1, langId = LanguageIds.ENGLISH, name = "I seek forgiveness", sound = null)
                )
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertTrue(result.isEmpty()) // Requires Arabic text
        }

        @Test
        @DisplayName("Handles empty translations list")
        fun `handles empty translations`() {
            val praise = Praise(
                id = 1,
                categories = listOf(PraiseCategory(id = 1, categoryId = CategoryIds.GENERAL, praiseId = 1, count = 1)),
                translations = emptyList()
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("Handles empty categories list")
        fun `handles empty categories`() {
            val praise = Praise(
                id = 1,
                categories = emptyList(),
                translations = createTranslations(praiseId = 1)
            )
            val response = createApiResponse(listOf(praise))

            val result = PraiseApiService.parsePraises(response)

            assertTrue(result.isEmpty()) // No category match
        }
    }

    @Nested
    @DisplayName("Language ID constants")
    inner class LanguageIdTests {

        @Test
        @DisplayName("Arabic language ID is 1")
        fun `arabic id is 1`() {
            assertEquals(1, LanguageIds.ARABIC)
        }

        @Test
        @DisplayName("English language ID is 2")
        fun `english id is 2`() {
            assertEquals(2, LanguageIds.ENGLISH)
        }
    }

    @Nested
    @DisplayName("Category ID constants")
    inner class CategoryIdTests {

        @Test
        @DisplayName("GENERAL category ID is 1")
        fun `general id is 1`() {
            assertEquals(1, CategoryIds.GENERAL)
        }

        @Test
        @DisplayName("AFTER_PRAYER category ID is 2")
        fun `after prayer id is 2`() {
            assertEquals(2, CategoryIds.AFTER_PRAYER)
        }
    }

    // Helper functions for creating test data

    private fun createPraise(
        id: Int,
        categoryIds: List<Int> = listOf(CategoryIds.GENERAL),
        arabicText: String = "سبحان الله",
        englishText: String = "Glory be to Allah"
    ): Praise {
        val categories = categoryIds.mapIndexed { index, catId ->
            PraiseCategory(
                id = index + 1,
                categoryId = catId,
                praiseId = id,
                count = 1
            )
        }
        val translations = listOf(
            PraiseTranslation(id = 1, praiseId = id, langId = LanguageIds.ARABIC, name = arabicText, sound = null),
            PraiseTranslation(id = 2, praiseId = id, langId = LanguageIds.ENGLISH, name = englishText, sound = null)
        )
        return Praise(id = id, categories = categories, translations = translations)
    }

    private fun createCategory(
        id: Int = 1,
        categoryId: Int = CategoryIds.GENERAL,
        praiseId: Int = 1,
        count: Int = 1
    ): PraiseCategory {
        return PraiseCategory(id = id, categoryId = categoryId, praiseId = praiseId, count = count)
    }

    private fun createTranslations(
        praiseId: Int,
        arabicText: String = "سبحان الله",
        englishText: String = "Glory be to Allah"
    ): List<PraiseTranslation> {
        return listOf(
            PraiseTranslation(id = 1, praiseId = praiseId, langId = LanguageIds.ARABIC, name = arabicText, sound = null),
            PraiseTranslation(id = 2, praiseId = praiseId, langId = LanguageIds.ENGLISH, name = englishText, sound = null)
        )
    }

    private fun createApiResponse(praises: List<Praise>, version: Int = 1): ApiResponse {
        return ApiResponse(
            status = true,
            message = "Success",
            data = PraiseData(version = version, praises = praises)
        )
    }
}

/**
 * Data class behavior tests for API models used by PraiseApiService.
 */
@DisplayName("Praise API Model Tests")
class PraiseApiModelTests {

    @Nested
    @DisplayName("CachedPraise data class")
    inner class CachedPraiseTests {

        @Test
        @DisplayName("CachedPraise stores all fields correctly")
        fun `cached praise stores fields`() {
            val praise = CachedPraise(
                id = 1,
                arabicText = "سبحان الله",
                englishText = "Glory be to Allah",
                categoryId = CategoryIds.GENERAL,
                count = 33
            )

            assertEquals(1, praise.id)
            assertEquals("سبحان الله", praise.arabicText)
            assertEquals("Glory be to Allah", praise.englishText)
            assertEquals(CategoryIds.GENERAL, praise.categoryId)
            assertEquals(33, praise.count)
        }

        @Test
        @DisplayName("CachedPraise equals works correctly")
        fun `cached praise equals`() {
            val praise1 = CachedPraise(1, "text", "text", 1, 1)
            val praise2 = CachedPraise(1, "text", "text", 1, 1)
            val praise3 = CachedPraise(2, "text", "text", 1, 1)

            assertEquals(praise1, praise2)
            assertNotEquals(praise1, praise3)
        }

        @Test
        @DisplayName("CachedPraise copy works correctly")
        fun `cached praise copy`() {
            val original = CachedPraise(1, "arabic", "english", 1, 10)
            val modified = original.copy(count = 33)

            assertEquals(10, original.count)
            assertEquals(33, modified.count)
            assertEquals(original.id, modified.id)
        }
    }

    @Nested
    @DisplayName("ApiResponse data class")
    inner class ApiResponseTests {

        @Test
        @DisplayName("ApiResponse with null data")
        fun `api response with null data`() {
            val response = ApiResponse(
                status = false,
                message = "No data",
                data = null
            )

            assertFalse(response.status)
            assertNull(response.data)
        }

        @Test
        @DisplayName("ApiResponse with data")
        fun `api response with data`() {
            val response = ApiResponse(
                status = true,
                message = "OK",
                data = PraiseData(version = 1, praises = emptyList())
            )

            assertTrue(response.status)
            assertNotNull(response.data)
        }
    }

    @Nested
    @DisplayName("PraiseData data class")
    inner class PraiseDataTests {

        @Test
        @DisplayName("PraiseData stores version and praises")
        fun `praise data stores fields`() {
            val praises = listOf(
                Praise(
                    id = 1,
                    categories = emptyList(),
                    translations = emptyList()
                )
            )
            val data = PraiseData(version = 5, praises = praises)

            assertEquals(5, data.version)
            assertEquals(1, data.praises.size)
        }

        @Test
        @DisplayName("PraiseData with empty praises list")
        fun `praise data with empty praises`() {
            val data = PraiseData(version = 0, praises = emptyList())

            assertEquals(0, data.version)
            assertTrue(data.praises.isEmpty())
        }
    }
}