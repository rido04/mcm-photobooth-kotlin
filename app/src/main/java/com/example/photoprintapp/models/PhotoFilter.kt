package com.example.photoprintapp.models

data class PhotoFilter(
    val type: FilterType,
    val name: String,
    val emoji: String,
    val description: String,
    val backgroundAsset: String, // path di assets/background-filter/
    val frame4Path: String? = null,
    val frame6Path: String? = null
) {
    fun getFramePath(gridCount: Int): String? {
        return when (gridCount) {
            4 -> frame4Path
            6 -> frame6Path
            else -> null
        }
    }

    fun hasFrame(gridCount: Int): Boolean {
        return getFramePath(gridCount) != null
    }

    companion object {
    val availableFilters = listOf(
        PhotoFilter(
            type = FilterType.NONE,
            name = "No Filter",
            emoji = "📷",
            description = "Original photo",
            backgroundAsset = "background-filter/no-filter.png"
        ),
        PhotoFilter(
            type = FilterType.AL_AZHAR_41,
            name = "Al-Azhar 41",
            emoji = "🏫",
            description = "SMP Islam Al Azhar 41 Serpong",
            backgroundAsset = "background-filter/al_azhar_41.png",
            frame4Path = "frames/frame4_photobooth_al_azhar.png",
            frame6Path = "frames/frame6_photobooth_al_azhar.png"
        ),
        // nanti aktifin kalau asset udah ready
        PhotoFilter(
            type = FilterType.AL_AZHAR,
            name = "Al-Azhar",
            emoji = "🏫",
            description = "Yayasan Pesantren Islam Al Azhar",
            backgroundAsset = "background-filter/al_azhar.png",
            frame4Path = "frames/frame4_photobooth.png",
            frame6Path = "frames/frame6_photobooth.png"
        ),

        PhotoFilter(
            type = FilterType.EMOJI,
            name = "Emoji Fun",
            emoji = "😊",
            description = "Fun emoji frame",
            backgroundAsset = "background-filter/emoticon.png",
            frame4Path = "frames/frame4_photobooth.png",
            frame6Path = "frames/frame6_photobooth.png"
        ),
        PhotoFilter(
            type = FilterType.FOOTBALL,
            name = "Football",
            emoji = "⚽",
            description = "Football fan frame",
            backgroundAsset = "background-filter/football.png",
            frame4Path = "frames/frame4_photobooth_football.png",
            frame6Path = "frames/frame6_photobooth_football.png"
        ),
        PhotoFilter(
            type = FilterType.FRIENDSHIP,
            name = "Friendship",
            emoji = "🫂",
            description = "With your besties",
            backgroundAsset = "background-filter/friend.png",
            frame4Path = "frames/friendship_4.png",
            frame6Path = "frames/friendship_6.png"
        ),
        PhotoFilter(
            type = FilterType.FLOWERS,
            name = "Flower Power",
            emoji = "🌸",
            description = "Beautiful flowers",
            backgroundAsset = "background-filter/flower.png",
            frame4Path = "frames/flower_4.png",
            frame6Path = "frames/flower_6.png"
        ),
        PhotoFilter(
            type = FilterType.FAMILY,
            name = "Family",
            emoji = "👨‍👩‍👧‍👦",
            description = "Enjoy family moments",
            backgroundAsset = "background-filter/family.png"
        )
    )
}
}