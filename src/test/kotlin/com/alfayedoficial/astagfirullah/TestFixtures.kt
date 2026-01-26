package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.data.model.*

/**
 * Test fixtures providing sample data for unit tests.
 * Use these factory methods to create consistent test data across test classes.
 */
object TestFixtures {

    // ==================== Praise Models ====================

    fun createPraiseTranslation(
        id: Int = 1,
        praiseId: Int = 1,
        langId: Int = LanguageIds.ARABIC,
        name: String = "سبحان الله",
        sound: String? = null
    ) = PraiseTranslation(
        id = id,
        praiseId = praiseId,
        langId = langId,
        name = name,
        sound = sound
    )

    fun createPraiseCategory(
        id: Int = 1,
        categoryId: Int = CategoryIds.GENERAL,
        praiseId: Int = 1,
        count: Int = 33
    ) = PraiseCategory(
        id = id,
        categoryId = categoryId,
        praiseId = praiseId,
        count = count
    )

    fun createPraise(
        id: Int = 1,
        categories: List<PraiseCategory> = listOf(createPraiseCategory()),
        translations: List<PraiseTranslation> = listOf(
            createPraiseTranslation(langId = LanguageIds.ARABIC, name = "سبحان الله"),
            createPraiseTranslation(id = 2, langId = LanguageIds.ENGLISH, name = "Glory be to Allah")
        )
    ) = Praise(
        id = id,
        categories = categories,
        translations = translations
    )

    fun createPraiseData(
        version: Int = 1,
        praises: List<Praise> = listOf(createPraise())
    ) = PraiseData(
        version = version,
        praises = praises
    )

    fun createApiResponse(
        status: Boolean = true,
        message: String = "Success",
        data: PraiseData? = createPraiseData()
    ) = ApiResponse(
        status = status,
        message = message,
        data = data
    )

    fun createCachedPraise(
        id: Int = 1,
        arabicText: String = "سبحان الله",
        englishText: String = "Glory be to Allah",
        categoryId: Int = CategoryIds.GENERAL,
        count: Int = 33
    ) = CachedPraise(
        id = id,
        arabicText = arabicText,
        englishText = englishText,
        categoryId = categoryId,
        count = count
    )

    // ==================== Settings Models ====================

    fun createSettingsData(
        id: Int = 1,
        appType: String = "JETBRAINS_PLUGIN",
        versionCode: Int = 200,
        versionName: String = "2.0.0",
        praiseVersion: Int = 1,
        updateType: String = UpdateType.NORMAL,
        updateUrl: String? = "https://plugins.jetbrains.com/plugin/24628-astagfirullah",
        isActive: Boolean = true
    ) = SettingsData(
        id = id,
        appType = appType,
        versionCode = versionCode,
        versionName = versionName,
        praiseVersion = praiseVersion,
        updateType = updateType,
        updateUrl = updateUrl,
        isActive = isActive
    )

    fun createSettingsApiResponse(
        status: Boolean = true,
        message: String = "Success",
        data: SettingsData? = createSettingsData()
    ) = SettingsApiResponse(
        status = status,
        message = message,
        data = data
    )

    // ==================== Sample Praise Lists ====================

    /**
     * Creates a list of sample praises for testing bulk operations.
     */
    fun createSamplePraiseList(count: Int = 5): List<Praise> {
        val samplePhrases = listOf(
            Pair("سبحان الله", "Glory be to Allah"),
            Pair("الحمد لله", "Praise be to Allah"),
            Pair("الله أكبر", "Allah is the Greatest"),
            Pair("لا إله إلا الله", "There is no god but Allah"),
            Pair("أستغفر الله", "I seek forgiveness from Allah")
        )

        return (1..count).map { index ->
            val phraseIndex = (index - 1) % samplePhrases.size
            val (arabic, english) = samplePhrases[phraseIndex]
            createPraise(
                id = index,
                categories = listOf(createPraiseCategory(id = index, praiseId = index)),
                translations = listOf(
                    createPraiseTranslation(id = index * 2 - 1, praiseId = index, langId = LanguageIds.ARABIC, name = arabic),
                    createPraiseTranslation(id = index * 2, praiseId = index, langId = LanguageIds.ENGLISH, name = english)
                )
            )
        }
    }

    /**
     * Creates a list of cached praises for testing.
     */
    fun createSampleCachedPraiseList(count: Int = 5): List<CachedPraise> {
        val samplePhrases = listOf(
            Pair("سبحان الله", "Glory be to Allah"),
            Pair("الحمد لله", "Praise be to Allah"),
            Pair("الله أكبر", "Allah is the Greatest"),
            Pair("لا إله إلا الله", "There is no god but Allah"),
            Pair("أستغفر الله", "I seek forgiveness from Allah")
        )

        return (1..count).map { index ->
            val phraseIndex = (index - 1) % samplePhrases.size
            val (arabic, english) = samplePhrases[phraseIndex]
            createCachedPraise(
                id = index,
                arabicText = arabic,
                englishText = english,
                count = 33
            )
        }
    }

    // ==================== JSON Samples ====================

    /**
     * Sample JSON response for testing API parsing.
     */
    val SAMPLE_API_RESPONSE_JSON = """
        {
            "status": true,
            "message": "Success",
            "data": {
                "version": 1,
                "praises": [
                    {
                        "id": 1,
                        "categories": [
                            {
                                "id": 1,
                                "cat_id": 1,
                                "praise_id": 1,
                                "count": 33
                            }
                        ],
                        "praise_translations": [
                            {
                                "id": 1,
                                "praise_id": 1,
                                "lang_id": 1,
                                "name": "سبحان الله",
                                "sound": null
                            },
                            {
                                "id": 2,
                                "praise_id": 1,
                                "lang_id": 2,
                                "name": "Glory be to Allah",
                                "sound": null
                            }
                        ]
                    }
                ]
            }
        }
    """.trimIndent()

    val SAMPLE_ERROR_RESPONSE_JSON = """
        {
            "status": false,
            "message": "Error occurred",
            "data": null
        }
    """.trimIndent()

    val SAMPLE_SETTINGS_RESPONSE_JSON = """
        {
            "status": true,
            "message": "Success",
            "data": {
                "id": 1,
                "app_type": "JETBRAINS_PLUGIN",
                "version_code": 200,
                "version_name": "2.0.0",
                "praise_version": 1,
                "update_type": "NORMAL",
                "update_url": "https://plugins.jetbrains.com/plugin/24628-astagfirullah",
                "is_active": true
            }
        }
    """.trimIndent()
}