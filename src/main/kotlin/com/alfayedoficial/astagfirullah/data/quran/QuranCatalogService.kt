package com.alfayedoficial.astagfirullah.data.quran

import com.alfayedoficial.astagfirullah.data.api.ApiHelper
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger

/**
 * Fetches the reciter and surah catalog from the public mp3quran.net v3 API and turns it
 * into the flat lists the Quran tool window needs. Stateless; the panel caches the result.
 */
object QuranCatalogService {

    private val logger = Logger.getInstance(QuranCatalogService::class.java)
    private val gson = Gson()

    private const val RECITERS_URL = "https://www.mp3quran.net/api/v3/reciters?language="
    private const val SUWAR_URL = "https://www.mp3quran.net/api/v3/suwar?language="

    // mp3quran's CDN 301-redirects the bare host and can be picky without a UA.
    private val HEADERS = mapOf("User-Agent" to "Astagfirullah-IntelliJ-Plugin")

    /**
     * Maps the plugin's language labels (AstagfirullahSettings.SUPPORTED_LANGUAGES) to
     * mp3quran.net locale codes. Every one of the plugin's seven languages is supported by
     * mp3quran, so surah AND reciter names come back localized. Anything unmapped falls back
     * to English.
     */
    private val LANGUAGE_CODES = mapOf(
        "العربية" to "ar",   // Arabic
        "English" to "eng",  // English
        "أردو" to "ur",      // Urdu
        "فارسى" to "fa",     // Farsi / Persian
        "Türkçe" to "tr",    // Turkish
        "Bahasa" to "id",    // Indonesian
        "বাংলা" to "bn",     // Bengali
    )

    fun languageCode(pluginLanguage: String): String = LANGUAGE_CODES[pluginLanguage] ?: "eng"

    data class Catalog(val reciters: List<QuranReciter>, val surahs: List<QuranSurah>)

    sealed class Result {
        data class Success(val catalog: Catalog) : Result()
        data class Error(val message: String) : Result()
    }

    /**
     * Blocking; call from a background thread.
     * @param languageCode an mp3quran locale code from [languageCode].
     */
    fun loadCatalog(languageCode: String): Result {
        val surahs = when (val r = fetchSurahs(languageCode)) {
            is Result.Error -> return r
            is Result.Success -> r.catalog.surahs
        }
        val reciters = when (val r = fetchReciters(languageCode)) {
            is Result.Error -> return r
            is Result.Success -> r.catalog.reciters
        }
        if (reciters.isEmpty() || surahs.isEmpty()) {
            return Result.Error("Quran catalog was empty")
        }
        return Result.Success(Catalog(reciters, surahs))
    }

    private fun fetchSurahs(languageCode: String): Result {
        return when (val res = ApiHelper.get(SUWAR_URL + languageCode, HEADERS)) {
            is ApiHelper.HttpResult.Success -> runCatching {
                val parsed = gson.fromJson(res.body, SuwarResponse::class.java)
                val surahs = parsed.suwar
                    .filter { it.id in 1..114 }
                    .map { QuranSurah(it.id, it.name.trim()) }
                    .sortedBy { it.id }
                Result.Success(Catalog(emptyList(), surahs))
            }.getOrElse {
                logger.warn("Failed to parse suwar", it)
                Result.Error("Could not read the surah list")
            }
            is ApiHelper.HttpResult.Error -> Result.Error("Could not load surahs: ${res.message}")
        }
    }

    private fun fetchReciters(languageCode: String): Result {
        return when (val res = ApiHelper.get(RECITERS_URL + languageCode, HEADERS)) {
            is ApiHelper.HttpResult.Success -> runCatching {
                val parsed = gson.fromJson(res.body, RecitersResponse::class.java)
                val reciters = parsed.reciters.flatMap { reciter ->
                    reciter.moshaf.mapNotNull { moshaf -> toReciter(reciter, moshaf) }
                }.sortedBy { it.label.lowercase() }
                Result.Success(Catalog(reciters, emptyList()))
            }.getOrElse {
                logger.warn("Failed to parse reciters", it)
                Result.Error("Could not read the reciter list")
            }
            is ApiHelper.HttpResult.Error -> Result.Error("Could not load reciters: ${res.message}")
        }
    }

    /** null when the moshaf has no usable server or surah list. */
    private fun toReciter(reciter: ReciterDto, moshaf: MoshafDto): QuranReciter? {
        val server = moshaf.server.trim()
        if (!server.startsWith("http")) return null
        val surahs = moshaf.surahList.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..114 }
            .toSet()
        if (surahs.isEmpty()) return null

        // Several moshafs per reciter -> disambiguate the label with the moshaf name.
        val moshafName = moshaf.name.trim()
        val label = if (moshafName.isBlank() || moshafName.equals(reciter.name.trim(), ignoreCase = true)) {
            reciter.name.trim()
        } else {
            "${reciter.name.trim()} — $moshafName"
        }
        return QuranReciter(
            reciterId = reciter.id,
            label = label,
            server = if (server.endsWith("/")) server else "$server/",
            availableSurahs = surahs,
        )
    }

    /** Builds the direct MP3 URL, e.g. https://server6.mp3quran.net/akdr/002.mp3 */
    fun audioUrl(reciter: QuranReciter, surahId: Int): String =
        "${reciter.server}${surahId.toString().padStart(3, '0')}.mp3"
}
