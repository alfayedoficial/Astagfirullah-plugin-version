package com.alfayedoficial.astagfirullah.data.model

import com.google.gson.annotations.SerializedName

/**
 * API Response wrapper
 */
data class ApiResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PraiseData?
)

/**
 * Praise data containing version and list of praises
 */
data class PraiseData(
    @SerializedName("version") val version: Int,
    @SerializedName("praises") val praises: List<Praise>
)

/**
 * Individual praise with categories and translations
 */
data class Praise(
    @SerializedName("id") val id: Int,
    @SerializedName("categories") val categories: List<PraiseCategory>,
    @SerializedName("praise_translations") val translations: List<PraiseTranslation>
)

/**
 * Category association for a praise
 */
data class PraiseCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("cat_id") val categoryId: Int,
    @SerializedName("praise_id") val praiseId: Int,
    @SerializedName("count") val count: Int
)

/**
 * Translation of a praise in a specific language
 * Note: interpretation field from API is ignored (can be String or Object)
 * Gson ignores unknown JSON fields by default, so we don't define it
 */
data class PraiseTranslation(
    @SerializedName("id") val id: Int,
    @SerializedName("praise_id") val praiseId: Int,
    @SerializedName("lang_id") val langId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sound") val sound: String?
)

/**
 * Cached praise for local storage (simplified)
 */
data class CachedPraise(
    val id: Int,
    val arabicText: String,
    val englishText: String,
    val categoryId: Int,
    val count: Int
)

/**
 * Language ID constants matching API
 */
object LanguageIds {
    const val ARABIC = 1
    const val ENGLISH = 2
}

/**
 * Category ID constants
 */
object CategoryIds {
    const val GENERAL = 1  // Filter by this category
    const val AFTER_PRAYER = 2
}
