package com.alfayedoficial.astagfirullah.data.quran

import com.google.gson.annotations.SerializedName

/**
 * DTOs for the public mp3quran.net v3 API and the flattened models the UI uses.
 *
 * Reciters: GET https://www.mp3quran.net/api/v3/reciters?language=eng
 * Suwar:    GET https://www.mp3quran.net/api/v3/suwar?language=eng
 * Audio:    {moshaf.server}{NNN}.mp3   (server ends with '/', NNN is the zero-padded surah id)
 */
internal data class RecitersResponse(val reciters: List<ReciterDto> = emptyList())

internal data class ReciterDto(
    val id: Int = 0,
    val name: String = "",
    val moshaf: List<MoshafDto> = emptyList(),
)

internal data class MoshafDto(
    val id: Int = 0,
    val name: String = "",
    val server: String = "",
    @SerializedName("surah_total") val surahTotal: Int = 0,
    /** Comma-separated surah ids this moshaf actually has, e.g. "1,2,3,...". */
    @SerializedName("surah_list") val surahList: String = "",
)

internal data class SuwarResponse(val suwar: List<SurahDto> = emptyList())

internal data class SurahDto(
    val id: Int = 0,
    val name: String = "",
)

/** A reciter+moshaf pairing shown in the picker. One reciter can have several moshafs. */
data class QuranReciter(
    val reciterId: Int,
    val label: String,
    /** Server base URL, guaranteed to end with '/'. */
    val server: String,
    /** Surah ids this moshaf provides. */
    val availableSurahs: Set<Int>,
) {
    override fun toString(): String = label
}

/** A surah entry: its number and display name. */
data class QuranSurah(
    val id: Int,
    val name: String,
) {
    /** e.g. "2. Al-Baqarah". */
    val display: String get() = "$id. $name"

    override fun toString(): String = display
}
