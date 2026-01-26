package com.alfayedoficial.astagfirullah.data.api

import com.alfayedoficial.astagfirullah.core.Constants
import com.alfayedoficial.astagfirullah.data.model.ApiResponse
import com.alfayedoficial.astagfirullah.data.model.CachedPraise
import com.alfayedoficial.astagfirullah.data.model.CategoryIds
import com.alfayedoficial.astagfirullah.data.model.LanguageIds
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

/**
 * Service for fetching praises from the remote API.
 * Handles HTTP requests and response parsing.
 */
object PraiseApiService {

    private val logger = Logger.getInstance(PraiseApiService::class.java)
    private val gson = Gson()

    /**
     * Fetches praises from the API.
     * Uses version-based sync: if local version < server version, returns new data.
     *
     * @param currentVersion The current local version (0 for first sync)
     * @return ApiResult containing either the parsed response or an error
     */
    fun fetchPraises(currentVersion: Int): ApiResult {
        val urlString = "${Constants.API_BASE_URL}${Constants.API_PRAISE_LIST_ENDPOINT}?version=$currentVersion&is_quran=false"

        logger.debug("Fetching praises from API with version: $currentVersion")

        return when (val httpResult = ApiHelper.get(urlString)) {
            is ApiHelper.HttpResult.Success -> {
                try {
                    val apiResponse = gson.fromJson(httpResult.body, ApiResponse::class.java)
                    if (apiResponse.status && apiResponse.data != null) {
                        logger.debug("API response: version=${apiResponse.data.version}, praises=${apiResponse.data.praises.size}")
                        ApiResult.Success(apiResponse)
                    } else {
                        logger.warn("API returned status=false: ${apiResponse.message}")
                        ApiResult.Error("API error: ${apiResponse.message}")
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to parse API response", e)
                    ApiResult.Error("Parse error: ${e.message}")
                }
            }
            is ApiHelper.HttpResult.Error -> {
                ApiResult.Error(httpResult.message)
            }
        }
    }

    /**
     * Parses API response and filters praises by category.
     * Extracts Arabic and English translations.
     *
     * @param response The API response to parse
     * @param categoryId The category to filter by (default: GENERAL = 1)
     * @return List of cached praises ready for storage
     */
    fun parsePraises(response: ApiResponse, categoryId: Int = CategoryIds.GENERAL): List<CachedPraise> {
        val data = response.data ?: return emptyList()

        val filteredPraises = data.praises.filter { praise ->
            praise.categories.any { it.categoryId == categoryId }
        }

        val result = filteredPraises.mapNotNull { praise ->
            val arabicText = praise.translations
                .find { it.langId == LanguageIds.ARABIC }?.name

            val englishText = praise.translations
                .find { it.langId == LanguageIds.ENGLISH }?.name

            // Only include if at least Arabic text is available
            if (arabicText != null) {
                val category = praise.categories.find { it.categoryId == categoryId }
                CachedPraise(
                    id = praise.id,
                    arabicText = arabicText,
                    englishText = englishText ?: arabicText, // Fallback to Arabic
                    categoryId = categoryId,
                    count = category?.count ?: 1
                )
            } else {
                null
            }
        }

        logger.debug("Parsed ${result.size} praises from API (category=$categoryId)")
        return result
    }
}

/**
 * Result wrapper for API calls
 */
sealed class ApiResult {
    data class Success(val response: ApiResponse) : ApiResult()
    data class Error(val message: String) : ApiResult()
}
