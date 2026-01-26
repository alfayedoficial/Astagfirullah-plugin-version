package com.alfayedoficial.astagfirullah.data.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Comprehensive unit tests for API data models in the Astagfirullah plugin.
 * Tests serialization/deserialization with GSON, null handling, edge cases, and default values.
 */
class ApiModelsTest {

    private lateinit var gson: Gson

    @BeforeEach
    fun setUp() {
        gson = GsonBuilder().create()
    }

    // ==================== ApiResponse Tests ====================

    @Nested
    @DisplayName("ApiResponse Tests")
    inner class ApiResponseTests {

        @Test
        @DisplayName("Should deserialize valid ApiResponse with data")
        fun testValidApiResponseWithData() {
            val json = """
                {
                    "status": true,
                    "message": "Success",
                    "data": {
                        "version": 1,
                        "praises": []
                    }
                }
            """.trimIndent()

            val response = gson.fromJson(json, ApiResponse::class.java)

            assertTrue(response.status)
            assertEquals("Success", response.message)
            assertNotNull(response.data)
            assertEquals(1, response.data?.version)
            assertTrue(response.data?.praises?.isEmpty() == true)
        }

        @Test
        @DisplayName("Should deserialize ApiResponse with null data")
        fun testApiResponseWithNullData() {
            val json = """
                {
                    "status": false,
                    "message": "Error occurred",
                    "data": null
                }
            """.trimIndent()

            val response = gson.fromJson(json, ApiResponse::class.java)

            assertFalse(response.status)
            assertEquals("Error occurred", response.message)
            assertNull(response.data)
        }

        @Test
        @DisplayName("Should handle missing data field as null")
        fun testApiResponseWithMissingDataField() {
            val json = """
                {
                    "status": true,
                    "message": "Partial response"
                }
            """.trimIndent()

            val response = gson.fromJson(json, ApiResponse::class.java)

            assertTrue(response.status)
            assertEquals("Partial response", response.message)
            assertNull(response.data)
        }

        @Test
        @DisplayName("Should serialize ApiResponse correctly")
        fun testApiResponseSerialization() {
            val praiseData = PraiseData(version = 5, praises = emptyList())
            val response = ApiResponse(status = true, message = "OK", data = praiseData)

            val json = gson.toJson(response)

            assertTrue(json.contains("\"status\":true"))
            assertTrue(json.contains("\"message\":\"OK\""))
            assertTrue(json.contains("\"version\":5"))
        }

        @Test
        @DisplayName("Should handle empty message")
        fun testApiResponseWithEmptyMessage() {
            val json = """
                {
                    "status": true,
                    "message": "",
                    "data": null
                }
            """.trimIndent()

            val response = gson.fromJson(json, ApiResponse::class.java)

            assertEquals("", response.message)
        }
    }

    // ==================== PraiseData Tests ====================

    @Nested
    @DisplayName("PraiseData Tests")
    inner class PraiseDataTests {

        @Test
        @DisplayName("Should deserialize PraiseData with version and empty praises")
        fun testPraiseDataWithEmptyPraises() {
            val json = """
                {
                    "version": 10,
                    "praises": []
                }
            """.trimIndent()

            val praiseData = gson.fromJson(json, PraiseData::class.java)

            assertEquals(10, praiseData.version)
            assertTrue(praiseData.praises.isEmpty())
        }

        @Test
        @DisplayName("Should deserialize PraiseData with multiple praises")
        fun testPraiseDataWithMultiplePraises() {
            val json = """
                {
                    "version": 3,
                    "praises": [
                        {
                            "id": 1,
                            "categories": [],
                            "praise_translations": []
                        },
                        {
                            "id": 2,
                            "categories": [],
                            "praise_translations": []
                        }
                    ]
                }
            """.trimIndent()

            val praiseData = gson.fromJson(json, PraiseData::class.java)

            assertEquals(3, praiseData.version)
            assertEquals(2, praiseData.praises.size)
            assertEquals(1, praiseData.praises[0].id)
            assertEquals(2, praiseData.praises[1].id)
        }

        @Test
        @DisplayName("Should handle version 0")
        fun testPraiseDataWithZeroVersion() {
            val json = """
                {
                    "version": 0,
                    "praises": []
                }
            """.trimIndent()

            val praiseData = gson.fromJson(json, PraiseData::class.java)

            assertEquals(0, praiseData.version)
        }

        @Test
        @DisplayName("Should handle large version numbers")
        fun testPraiseDataWithLargeVersion() {
            val json = """
                {
                    "version": 2147483647,
                    "praises": []
                }
            """.trimIndent()

            val praiseData = gson.fromJson(json, PraiseData::class.java)

            assertEquals(Int.MAX_VALUE, praiseData.version)
        }

        @Test
        @DisplayName("Should serialize PraiseData correctly")
        fun testPraiseDataSerialization() {
            val praise = Praise(id = 1, categories = emptyList(), translations = emptyList())
            val praiseData = PraiseData(version = 7, praises = listOf(praise))

            val json = gson.toJson(praiseData)

            assertTrue(json.contains("\"version\":7"))
            assertTrue(json.contains("\"praises\":["))
        }
    }

    // ==================== Praise Tests ====================

    @Nested
    @DisplayName("Praise Tests")
    inner class PraiseTests {

        @Test
        @DisplayName("Should deserialize complete Praise with categories and translations")
        fun testCompletePraise() {
            val json = """
                {
                    "id": 42,
                    "categories": [
                        {
                            "id": 1,
                            "cat_id": 1,
                            "praise_id": 42,
                            "count": 33
                        }
                    ],
                    "praise_translations": [
                        {
                            "id": 100,
                            "praise_id": 42,
                            "lang_id": 1,
                            "name": "Arabic text",
                            "sound": "sound_file.mp3"
                        },
                        {
                            "id": 101,
                            "praise_id": 42,
                            "lang_id": 2,
                            "name": "English text",
                            "sound": null
                        }
                    ]
                }
            """.trimIndent()

            val praise = gson.fromJson(json, Praise::class.java)

            assertEquals(42, praise.id)
            assertEquals(1, praise.categories.size)
            assertEquals(2, praise.translations.size)

            // Verify category
            val category = praise.categories[0]
            assertEquals(1, category.categoryId)
            assertEquals(33, category.count)

            // Verify translations
            val arabicTranslation = praise.translations[0]
            assertEquals(1, arabicTranslation.langId)
            assertEquals("Arabic text", arabicTranslation.name)
            assertEquals("sound_file.mp3", arabicTranslation.sound)

            val englishTranslation = praise.translations[1]
            assertEquals(2, englishTranslation.langId)
            assertEquals("English text", englishTranslation.name)
            assertNull(englishTranslation.sound)
        }

        @Test
        @DisplayName("Should handle Praise with empty categories and translations")
        fun testPraiseWithEmptyCollections() {
            val json = """
                {
                    "id": 1,
                    "categories": [],
                    "praise_translations": []
                }
            """.trimIndent()

            val praise = gson.fromJson(json, Praise::class.java)

            assertEquals(1, praise.id)
            assertTrue(praise.categories.isEmpty())
            assertTrue(praise.translations.isEmpty())
        }

        @Test
        @DisplayName("Should handle Praise with multiple categories")
        fun testPraiseWithMultipleCategories() {
            val json = """
                {
                    "id": 5,
                    "categories": [
                        {"id": 1, "cat_id": 1, "praise_id": 5, "count": 10},
                        {"id": 2, "cat_id": 2, "praise_id": 5, "count": 20}
                    ],
                    "praise_translations": []
                }
            """.trimIndent()

            val praise = gson.fromJson(json, Praise::class.java)

            assertEquals(2, praise.categories.size)
            assertEquals(1, praise.categories[0].categoryId)
            assertEquals(2, praise.categories[1].categoryId)
        }

        @Test
        @DisplayName("Should ignore unknown fields (like interpretation)")
        fun testPraiseIgnoresUnknownFields() {
            val json = """
                {
                    "id": 1,
                    "categories": [],
                    "praise_translations": [
                        {
                            "id": 1,
                            "praise_id": 1,
                            "lang_id": 1,
                            "name": "Test",
                            "sound": null,
                            "interpretation": {"some": "object"}
                        }
                    ],
                    "unknown_field": "should be ignored"
                }
            """.trimIndent()

            val praise = gson.fromJson(json, Praise::class.java)

            assertEquals(1, praise.id)
            assertEquals(1, praise.translations.size)
            assertEquals("Test", praise.translations[0].name)
        }

        @Test
        @DisplayName("Should serialize Praise with correct field names")
        fun testPraiseSerialization() {
            val category = PraiseCategory(id = 1, categoryId = 1, praiseId = 10, count = 5)
            val translation = PraiseTranslation(id = 1, praiseId = 10, langId = 1, name = "Test", sound = null)
            val praise = Praise(id = 10, categories = listOf(category), translations = listOf(translation))

            val json = gson.toJson(praise)

            assertTrue(json.contains("\"id\":10"))
            assertTrue(json.contains("\"categories\":["))
            assertTrue(json.contains("\"praise_translations\":["))
        }
    }

    // ==================== PraiseCategory Tests ====================

    @Nested
    @DisplayName("PraiseCategory Tests")
    inner class PraiseCategoryTests {

        @Test
        @DisplayName("Should deserialize PraiseCategory with all fields")
        fun testCompletePraiseCategory() {
            val json = """
                {
                    "id": 1,
                    "cat_id": 2,
                    "praise_id": 100,
                    "count": 33
                }
            """.trimIndent()

            val category = gson.fromJson(json, PraiseCategory::class.java)

            assertEquals(1, category.id)
            assertEquals(2, category.categoryId)
            assertEquals(100, category.praiseId)
            assertEquals(33, category.count)
        }

        @Test
        @DisplayName("Should handle zero count")
        fun testPraiseCategoryWithZeroCount() {
            val json = """
                {
                    "id": 1,
                    "cat_id": 1,
                    "praise_id": 1,
                    "count": 0
                }
            """.trimIndent()

            val category = gson.fromJson(json, PraiseCategory::class.java)

            assertEquals(0, category.count)
        }

        @Test
        @DisplayName("Should handle high count values")
        fun testPraiseCategoryWithHighCount() {
            val json = """
                {
                    "id": 1,
                    "cat_id": 1,
                    "praise_id": 1,
                    "count": 1000000
                }
            """.trimIndent()

            val category = gson.fromJson(json, PraiseCategory::class.java)

            assertEquals(1000000, category.count)
        }

        @Test
        @DisplayName("Should serialize PraiseCategory with correct SerializedName")
        fun testPraiseCategorySerialization() {
            val category = PraiseCategory(id = 5, categoryId = 3, praiseId = 10, count = 7)

            val json = gson.toJson(category)

            assertTrue(json.contains("\"id\":5"))
            assertTrue(json.contains("\"cat_id\":3"))
            assertTrue(json.contains("\"praise_id\":10"))
            assertTrue(json.contains("\"count\":7"))
        }

        @Test
        @DisplayName("Should match CategoryIds constants")
        fun testCategoryIdsConstants() {
            assertEquals(1, CategoryIds.GENERAL)
            assertEquals(2, CategoryIds.AFTER_PRAYER)
        }
    }

    // ==================== PraiseTranslation Tests ====================

    @Nested
    @DisplayName("PraiseTranslation Tests")
    inner class PraiseTranslationTests {

        @Test
        @DisplayName("Should deserialize PraiseTranslation with all fields")
        fun testCompletePraiseTranslation() {
            val json = """
                {
                    "id": 1,
                    "praise_id": 10,
                    "lang_id": 1,
                    "name": "Arabic text here",
                    "sound": "audio.mp3"
                }
            """.trimIndent()

            val translation = gson.fromJson(json, PraiseTranslation::class.java)

            assertEquals(1, translation.id)
            assertEquals(10, translation.praiseId)
            assertEquals(1, translation.langId)
            assertEquals("Arabic text here", translation.name)
            assertEquals("audio.mp3", translation.sound)
        }

        @Test
        @DisplayName("Should handle null sound field")
        fun testPraiseTranslationWithNullSound() {
            val json = """
                {
                    "id": 1,
                    "praise_id": 10,
                    "lang_id": 2,
                    "name": "English text",
                    "sound": null
                }
            """.trimIndent()

            val translation = gson.fromJson(json, PraiseTranslation::class.java)

            assertEquals("English text", translation.name)
            assertNull(translation.sound)
        }

        @Test
        @DisplayName("Should handle missing sound field as null")
        fun testPraiseTranslationWithMissingSound() {
            val json = """
                {
                    "id": 1,
                    "praise_id": 10,
                    "lang_id": 2,
                    "name": "English text"
                }
            """.trimIndent()

            val translation = gson.fromJson(json, PraiseTranslation::class.java)

            assertNull(translation.sound)
        }

        @Test
        @DisplayName("Should handle Arabic language ID")
        fun testPraiseTranslationArabic() {
            val json = """
                {
                    "id": 1,
                    "praise_id": 1,
                    "lang_id": 1,
                    "name": "Test",
                    "sound": null
                }
            """.trimIndent()

            val translation = gson.fromJson(json, PraiseTranslation::class.java)

            assertEquals(LanguageIds.ARABIC, translation.langId)
        }

        @Test
        @DisplayName("Should handle English language ID")
        fun testPraiseTranslationEnglish() {
            val json = """
                {
                    "id": 1,
                    "praise_id": 1,
                    "lang_id": 2,
                    "name": "Test",
                    "sound": null
                }
            """.trimIndent()

            val translation = gson.fromJson(json, PraiseTranslation::class.java)

            assertEquals(LanguageIds.ENGLISH, translation.langId)
        }

        @Test
        @DisplayName("Should handle Unicode text in name")
        fun testPraiseTranslationWithUnicode() {
            val json = """
                {
                    "id": 1,
                    "praise_id": 1,
                    "lang_id": 1,
                    "name": "سبحان الله",
                    "sound": null
                }
            """.trimIndent()

            val translation = gson.fromJson(json, PraiseTranslation::class.java)

            assertEquals("سبحان الله", translation.name)
        }

        @Test
        @DisplayName("Should handle empty name")
        fun testPraiseTranslationWithEmptyName() {
            val json = """
                {
                    "id": 1,
                    "praise_id": 1,
                    "lang_id": 1,
                    "name": "",
                    "sound": null
                }
            """.trimIndent()

            val translation = gson.fromJson(json, PraiseTranslation::class.java)

            assertEquals("", translation.name)
        }

        @Test
        @DisplayName("Should match LanguageIds constants")
        fun testLanguageIdsConstants() {
            assertEquals(1, LanguageIds.ARABIC)
            assertEquals(2, LanguageIds.ENGLISH)
        }

        @Test
        @DisplayName("Should serialize PraiseTranslation correctly")
        fun testPraiseTranslationSerialization() {
            val translation = PraiseTranslation(
                id = 5,
                praiseId = 10,
                langId = 1,
                name = "Test name",
                sound = "test.mp3"
            )

            val json = gson.toJson(translation)

            assertTrue(json.contains("\"id\":5"))
            assertTrue(json.contains("\"praise_id\":10"))
            assertTrue(json.contains("\"lang_id\":1"))
            assertTrue(json.contains("\"name\":\"Test name\""))
            assertTrue(json.contains("\"sound\":\"test.mp3\""))
        }
    }

    // ==================== CachedPraise Tests ====================

    @Nested
    @DisplayName("CachedPraise Tests")
    inner class CachedPraiseTests {

        @Test
        @DisplayName("Should deserialize CachedPraise with all fields")
        fun testCompleteCachedPraise() {
            val json = """
                {
                    "id": 1,
                    "arabicText": "سبحان الله",
                    "englishText": "Glory be to Allah",
                    "categoryId": 1,
                    "count": 33
                }
            """.trimIndent()

            val cached = gson.fromJson(json, CachedPraise::class.java)

            assertEquals(1, cached.id)
            assertEquals("سبحان الله", cached.arabicText)
            assertEquals("Glory be to Allah", cached.englishText)
            assertEquals(1, cached.categoryId)
            assertEquals(33, cached.count)
        }

        @Test
        @DisplayName("Should serialize CachedPraise correctly")
        fun testCachedPraiseSerialization() {
            val cached = CachedPraise(
                id = 5,
                arabicText = "الحمد لله",
                englishText = "Praise be to Allah",
                categoryId = 1,
                count = 100
            )

            val json = gson.toJson(cached)
            val deserialized = gson.fromJson(json, CachedPraise::class.java)

            assertEquals(cached.id, deserialized.id)
            assertEquals(cached.arabicText, deserialized.arabicText)
            assertEquals(cached.englishText, deserialized.englishText)
            assertEquals(cached.categoryId, deserialized.categoryId)
            assertEquals(cached.count, deserialized.count)
        }

        @Test
        @DisplayName("Should handle empty text fields")
        fun testCachedPraiseWithEmptyText() {
            val cached = CachedPraise(
                id = 1,
                arabicText = "",
                englishText = "",
                categoryId = 1,
                count = 1
            )

            val json = gson.toJson(cached)
            val deserialized = gson.fromJson(json, CachedPraise::class.java)

            assertEquals("", deserialized.arabicText)
            assertEquals("", deserialized.englishText)
        }

        @Test
        @DisplayName("Should handle special characters in text")
        fun testCachedPraiseWithSpecialCharacters() {
            val cached = CachedPraise(
                id = 1,
                arabicText = "لا إله إلا الله",
                englishText = "There is no god but Allah",
                categoryId = 1,
                count = 100
            )

            val json = gson.toJson(cached)
            val deserialized = gson.fromJson(json, CachedPraise::class.java)

            assertEquals("لا إله إلا الله", deserialized.arabicText)
            assertEquals("There is no god but Allah", deserialized.englishText)
        }

        @Test
        @DisplayName("Should preserve data through serialization round-trip")
        fun testCachedPraiseRoundTrip() {
            val original = CachedPraise(
                id = 42,
                arabicText = "أستغفر الله",
                englishText = "I seek forgiveness from Allah",
                categoryId = 2,
                count = 70
            )

            val json = gson.toJson(original)
            val restored = gson.fromJson(json, CachedPraise::class.java)

            assertEquals(original, restored)
        }
    }

    // ==================== SettingsApiResponse Tests ====================

    @Nested
    @DisplayName("SettingsApiResponse Tests")
    inner class SettingsApiResponseTests {

        @Test
        @DisplayName("Should deserialize valid SettingsApiResponse")
        fun testValidSettingsApiResponse() {
            val json = """
                {
                    "status": true,
                    "message": "Settings retrieved",
                    "data": {
                        "id": 1,
                        "app_type": "PLUGIN",
                        "version_code": 200,
                        "version_name": "2.0.0",
                        "praise_version": 5,
                        "update_type": "NORMAL",
                        "update_url": "https://plugins.jetbrains.com/plugin/12345",
                        "is_active": true
                    }
                }
            """.trimIndent()

            val response = gson.fromJson(json, SettingsApiResponse::class.java)

            assertTrue(response.status)
            assertEquals("Settings retrieved", response.message)
            assertNotNull(response.data)
            assertEquals(200, response.data?.versionCode)
            assertEquals("2.0.0", response.data?.versionName)
        }

        @Test
        @DisplayName("Should handle null data in SettingsApiResponse")
        fun testSettingsApiResponseWithNullData() {
            val json = """
                {
                    "status": false,
                    "message": "Not found",
                    "data": null
                }
            """.trimIndent()

            val response = gson.fromJson(json, SettingsApiResponse::class.java)

            assertFalse(response.status)
            assertNull(response.data)
        }

        @Test
        @DisplayName("Should handle missing data field")
        fun testSettingsApiResponseWithMissingData() {
            val json = """
                {
                    "status": true,
                    "message": "OK"
                }
            """.trimIndent()

            val response = gson.fromJson(json, SettingsApiResponse::class.java)

            assertNull(response.data)
        }

        @Test
        @DisplayName("Should serialize SettingsApiResponse correctly")
        fun testSettingsApiResponseSerialization() {
            val settingsData = SettingsData(
                id = 1,
                appType = "PLUGIN",
                versionCode = 100,
                versionName = "1.0.0",
                praiseVersion = 1,
                updateType = UpdateType.NORMAL,
                updateUrl = null,
                isActive = true
            )
            val response = SettingsApiResponse(status = true, message = "OK", data = settingsData)

            val json = gson.toJson(response)

            assertTrue(json.contains("\"status\":true"))
            assertTrue(json.contains("\"message\":\"OK\""))
            assertTrue(json.contains("\"version_code\":100"))
        }
    }

    // ==================== SettingsData Tests ====================

    @Nested
    @DisplayName("SettingsData Tests")
    inner class SettingsDataTests {

        @Test
        @DisplayName("Should deserialize SettingsData with NORMAL update type")
        fun testSettingsDataNormalUpdate() {
            val json = """
                {
                    "id": 1,
                    "app_type": "PLUGIN",
                    "version_code": 200,
                    "version_name": "2.0.0",
                    "praise_version": 10,
                    "update_type": "NORMAL",
                    "update_url": "https://example.com/update",
                    "is_active": true
                }
            """.trimIndent()

            val settings = gson.fromJson(json, SettingsData::class.java)

            assertEquals(1, settings.id)
            assertEquals("PLUGIN", settings.appType)
            assertEquals(200, settings.versionCode)
            assertEquals("2.0.0", settings.versionName)
            assertEquals(10, settings.praiseVersion)
            assertEquals(UpdateType.NORMAL, settings.updateType)
            assertEquals("https://example.com/update", settings.updateUrl)
            assertTrue(settings.isActive)
        }

        @Test
        @DisplayName("Should deserialize SettingsData with EMERGENCY update type")
        fun testSettingsDataEmergencyUpdate() {
            val json = """
                {
                    "id": 1,
                    "app_type": "PLUGIN",
                    "version_code": 201,
                    "version_name": "2.0.1",
                    "praise_version": 10,
                    "update_type": "EMERGENCY",
                    "update_url": "https://example.com/emergency-update",
                    "is_active": true
                }
            """.trimIndent()

            val settings = gson.fromJson(json, SettingsData::class.java)

            assertEquals(UpdateType.EMERGENCY, settings.updateType)
        }

        @Test
        @DisplayName("Should handle null update_url")
        fun testSettingsDataWithNullUpdateUrl() {
            val json = """
                {
                    "id": 1,
                    "app_type": "PLUGIN",
                    "version_code": 200,
                    "version_name": "2.0.0",
                    "praise_version": 5,
                    "update_type": "NORMAL",
                    "update_url": null,
                    "is_active": true
                }
            """.trimIndent()

            val settings = gson.fromJson(json, SettingsData::class.java)

            assertNull(settings.updateUrl)
        }

        @Test
        @DisplayName("Should handle missing update_url field")
        fun testSettingsDataWithMissingUpdateUrl() {
            val json = """
                {
                    "id": 1,
                    "app_type": "PLUGIN",
                    "version_code": 200,
                    "version_name": "2.0.0",
                    "praise_version": 5,
                    "update_type": "NORMAL",
                    "is_active": true
                }
            """.trimIndent()

            val settings = gson.fromJson(json, SettingsData::class.java)

            assertNull(settings.updateUrl)
        }

        @Test
        @DisplayName("Should handle inactive plugin")
        fun testSettingsDataInactive() {
            val json = """
                {
                    "id": 1,
                    "app_type": "PLUGIN",
                    "version_code": 200,
                    "version_name": "2.0.0",
                    "praise_version": 5,
                    "update_type": "NORMAL",
                    "update_url": null,
                    "is_active": false
                }
            """.trimIndent()

            val settings = gson.fromJson(json, SettingsData::class.java)

            assertFalse(settings.isActive)
        }

        @Test
        @DisplayName("Should serialize SettingsData with correct field names")
        fun testSettingsDataSerialization() {
            val settings = SettingsData(
                id = 1,
                appType = "PLUGIN",
                versionCode = 200,
                versionName = "2.0.0",
                praiseVersion = 5,
                updateType = UpdateType.EMERGENCY,
                updateUrl = "https://example.com",
                isActive = true
            )

            val json = gson.toJson(settings)

            assertTrue(json.contains("\"id\":1"))
            assertTrue(json.contains("\"app_type\":\"PLUGIN\""))
            assertTrue(json.contains("\"version_code\":200"))
            assertTrue(json.contains("\"version_name\":\"2.0.0\""))
            assertTrue(json.contains("\"praise_version\":5"))
            assertTrue(json.contains("\"update_type\":\"EMERGENCY\""))
            assertTrue(json.contains("\"update_url\":\"https://example.com\""))
            assertTrue(json.contains("\"is_active\":true"))
        }

        @Test
        @DisplayName("Should verify UpdateType constants")
        fun testUpdateTypeConstants() {
            assertEquals("NORMAL", UpdateType.NORMAL)
            assertEquals("EMERGENCY", UpdateType.EMERGENCY)
        }

        @Test
        @DisplayName("Should handle version comparison scenario")
        fun testSettingsDataVersionComparison() {
            val json = """
                {
                    "id": 1,
                    "app_type": "PLUGIN",
                    "version_code": 201,
                    "version_name": "2.0.1",
                    "praise_version": 15,
                    "update_type": "NORMAL",
                    "update_url": null,
                    "is_active": true
                }
            """.trimIndent()

            val settings = gson.fromJson(json, SettingsData::class.java)
            val currentVersionCode = 200
            val currentPraiseVersion = 10

            assertTrue(settings.versionCode > currentVersionCode)
            assertTrue(settings.praiseVersion > currentPraiseVersion)
        }
    }

    // ==================== Edge Cases and Integration Tests ====================

    @Nested
    @DisplayName("Edge Cases and Integration Tests")
    inner class EdgeCasesTests {

        @Test
        @DisplayName("Should handle complete API response with nested data")
        fun testCompleteApiResponseWithNestedData() {
            val json = """
                {
                    "status": true,
                    "message": "Success",
                    "data": {
                        "version": 5,
                        "praises": [
                            {
                                "id": 1,
                                "categories": [
                                    {"id": 1, "cat_id": 1, "praise_id": 1, "count": 33}
                                ],
                                "praise_translations": [
                                    {"id": 1, "praise_id": 1, "lang_id": 1, "name": "سبحان الله", "sound": "sub.mp3"},
                                    {"id": 2, "praise_id": 1, "lang_id": 2, "name": "Glory be to Allah", "sound": null}
                                ]
                            },
                            {
                                "id": 2,
                                "categories": [
                                    {"id": 2, "cat_id": 1, "praise_id": 2, "count": 33},
                                    {"id": 3, "cat_id": 2, "praise_id": 2, "count": 10}
                                ],
                                "praise_translations": [
                                    {"id": 3, "praise_id": 2, "lang_id": 1, "name": "الحمد لله", "sound": "ham.mp3"},
                                    {"id": 4, "praise_id": 2, "lang_id": 2, "name": "Praise be to Allah", "sound": null}
                                ]
                            }
                        ]
                    }
                }
            """.trimIndent()

            val response = gson.fromJson(json, ApiResponse::class.java)

            assertTrue(response.status)
            assertNotNull(response.data)
            assertEquals(5, response.data?.version)
            assertEquals(2, response.data?.praises?.size)

            // Verify first praise
            val praise1 = response.data?.praises?.get(0)
            assertEquals(1, praise1?.id)
            assertEquals(1, praise1?.categories?.size)
            assertEquals(2, praise1?.translations?.size)

            // Verify second praise has multiple categories
            val praise2 = response.data?.praises?.get(1)
            assertEquals(2, praise2?.categories?.size)
        }

        @Test
        @DisplayName("Should handle extra unknown fields in JSON")
        fun testExtraFieldsInJson() {
            val json = """
                {
                    "status": true,
                    "message": "OK",
                    "data": null,
                    "extra_field": "should be ignored",
                    "another_field": 123
                }
            """.trimIndent()

            val response = gson.fromJson(json, ApiResponse::class.java)

            assertTrue(response.status)
            assertEquals("OK", response.message)
        }

        @Test
        @DisplayName("Should handle whitespace in JSON")
        fun testJsonWithWhitespace() {
            val json = """

                {
                    "status"   :   true   ,
                    "message"  :  "   spaces   "  ,
                    "data"     :   null
                }

            """.trimIndent()

            val response = gson.fromJson(json, ApiResponse::class.java)

            assertTrue(response.status)
            assertEquals("   spaces   ", response.message)
        }

        @Test
        @DisplayName("Should handle data class equality")
        fun testDataClassEquality() {
            val praise1 = CachedPraise(1, "Arabic", "English", 1, 33)
            val praise2 = CachedPraise(1, "Arabic", "English", 1, 33)
            val praise3 = CachedPraise(1, "Arabic", "English", 1, 34)

            assertEquals(praise1, praise2)
            assertNotEquals(praise1, praise3)
            assertEquals(praise1.hashCode(), praise2.hashCode())
        }

        @Test
        @DisplayName("Should handle data class copy")
        fun testDataClassCopy() {
            val original = CachedPraise(1, "Arabic", "English", 1, 33)
            val copied = original.copy(count = 100)

            assertEquals(original.id, copied.id)
            assertEquals(original.arabicText, copied.arabicText)
            assertEquals(100, copied.count)
            assertNotEquals(original.count, copied.count)
        }

        @Test
        @DisplayName("Should convert API models to cache format")
        fun testApiToCacheConversion() {
            val translation1 = PraiseTranslation(1, 1, LanguageIds.ARABIC, "سبحان الله", null)
            val translation2 = PraiseTranslation(2, 1, LanguageIds.ENGLISH, "Glory be to Allah", null)
            val category = PraiseCategory(1, CategoryIds.GENERAL, 1, 33)
            val praise = Praise(1, listOf(category), listOf(translation1, translation2))

            // Simulate conversion logic
            val arabicText = praise.translations.find { it.langId == LanguageIds.ARABIC }?.name ?: ""
            val englishText = praise.translations.find { it.langId == LanguageIds.ENGLISH }?.name ?: ""
            val generalCategory = praise.categories.find { it.categoryId == CategoryIds.GENERAL }

            val cached = CachedPraise(
                id = praise.id,
                arabicText = arabicText,
                englishText = englishText,
                categoryId = generalCategory?.categoryId ?: 0,
                count = generalCategory?.count ?: 0
            )

            assertEquals(1, cached.id)
            assertEquals("سبحان الله", cached.arabicText)
            assertEquals("Glory be to Allah", cached.englishText)
            assertEquals(CategoryIds.GENERAL, cached.categoryId)
            assertEquals(33, cached.count)
        }

        @Test
        @DisplayName("Should handle list serialization round-trip")
        fun testListSerializationRoundTrip() {
            val praises = listOf(
                CachedPraise(1, "Arabic1", "English1", 1, 10),
                CachedPraise(2, "Arabic2", "English2", 1, 20),
                CachedPraise(3, "Arabic3", "English3", 2, 30)
            )

            val json = gson.toJson(praises)
            val restored: List<CachedPraise> = gson.fromJson(
                json,
                object : com.google.gson.reflect.TypeToken<List<CachedPraise>>() {}.type
            )

            assertEquals(praises.size, restored.size)
            assertEquals(praises, restored)
        }
    }
}