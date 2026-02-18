package com.example.photoprintapp.models

/**
 * Defines where each photo slot goes inside the frame PNG.
 * All values are in pixels relative to the frame image's native resolution.
 * 
 * Frame images are portrait: 930 x 1244 px (standard)
 * 
 * To adjust positions: edit the SlotRect values per filter below.
 */
data class SlotRect(
    val x: Int,      // left position in px
    val y: Int,      // top position in px
    val width: Int,  // slot width in px
    val height: Int  // slot height in px
)

data class FilterLayout(
    val frameWidth: Int,    // native frame PNG width
    val frameHeight: Int,   // native frame PNG height
    val slots: List<SlotRect>
)

object FilterLayoutConfig {

    // ─── EMOJI FILTER ────────────────────────────────────────────────
    // frame4_photobooth.png  (930 x 1244 px)
    // 2x2 grid, black slots visible in frame
    val EMOJI_4 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 58,  y = 120, width = 390, height = 400), // top-left
            SlotRect(x = 482, y = 120, width = 390, height = 400), // top-right
            SlotRect(x = 58,  y = 560, width = 390, height = 400), // bottom-left
            SlotRect(x = 482, y = 560, width = 390, height = 400)  // bottom-right
        )
    )

    // frame6_photobooth.png  (930 x 1244 px)
    // 2 col x 3 row grid, left side has emoji decorations
    val EMOJI_6 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 310, y = 50,  width = 290, height = 360), // row1-col1
            SlotRect(x = 620, y = 50,  width = 290, height = 360), // row1-col2
            SlotRect(x = 310, y = 440, width = 290, height = 360), // row2-col1
            SlotRect(x = 620, y = 440, width = 290, height = 360), // row2-col2
            SlotRect(x = 310, y = 830, width = 290, height = 360), // row3-col1
            SlotRect(x = 620, y = 830, width = 290, height = 360)  // row3-col2
        )
    )

    // ─── FOOTBALL FILTER ─────────────────────────────────────────────
    // frame4_photobooth_football.png
    // 2x2 grid, left side has football decorations
    val FOOTBALL_4 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 310, y = 50,  width = 290, height = 260), // top-left
            SlotRect(x = 620, y = 50,  width = 290, height = 260), // top-right
            SlotRect(x = 310, y = 340, width = 290, height = 260), // mid-left
            SlotRect(x = 620, y = 340, width = 290, height = 260)  // mid-right
        )
    )

    // frame6_photobooth_football.png
    // 2 col x 3 row
    val FOOTBALL_6 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 310, y = 50,  width = 290, height = 340), // row1-col1
            SlotRect(x = 620, y = 50,  width = 290, height = 340), // row1-col2
            SlotRect(x = 310, y = 430, width = 290, height = 340), // row2-col1
            SlotRect(x = 620, y = 430, width = 290, height = 340), // row2-col2
            SlotRect(x = 310, y = 820, width = 290, height = 340), // row3-col1
            SlotRect(x = 620, y = 820, width = 290, height = 340)  // row3-col2
        )
    )

    // ─── VALENTINE FILTER ────────────────────────────────────────────
    val VALENTINE_4 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 58,  y = 120, width = 390, height = 400),
            SlotRect(x = 482, y = 120, width = 390, height = 400),
            SlotRect(x = 58,  y = 560, width = 390, height = 400),
            SlotRect(x = 482, y = 560, width = 390, height = 400)
        )
    )

    val VALENTINE_6 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 310, y = 50,  width = 290, height = 360),
            SlotRect(x = 620, y = 50,  width = 290, height = 360),
            SlotRect(x = 310, y = 440, width = 290, height = 360),
            SlotRect(x = 620, y = 440, width = 290, height = 360),
            SlotRect(x = 310, y = 830, width = 290, height = 360),
            SlotRect(x = 620, y = 830, width = 290, height = 360)
        )
    )

    // ─── FRIENDSHIP FILTER ───────────────────────────────────────────
    val FRIENDSHIP_4 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 58,  y = 120, width = 390, height = 400),
            SlotRect(x = 482, y = 120, width = 390, height = 400),
            SlotRect(x = 58,  y = 560, width = 390, height = 400),
            SlotRect(x = 482, y = 560, width = 390, height = 400)
        )
    )

    val FRIENDSHIP_6 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 310, y = 50,  width = 290, height = 360),
            SlotRect(x = 620, y = 50,  width = 290, height = 360),
            SlotRect(x = 310, y = 440, width = 290, height = 360),
            SlotRect(x = 620, y = 440, width = 290, height = 360),
            SlotRect(x = 310, y = 830, width = 290, height = 360),
            SlotRect(x = 620, y = 830, width = 290, height = 360)
        )
    )

    // ─── FLOWERS FILTER ──────────────────────────────────────────────
    val FLOWERS_4 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 58,  y = 120, width = 390, height = 400),
            SlotRect(x = 482, y = 120, width = 390, height = 400),
            SlotRect(x = 58,  y = 560, width = 390, height = 400),
            SlotRect(x = 482, y = 560, width = 390, height = 400)
        )
    )

    val FLOWERS_6 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 310, y = 50,  width = 290, height = 360),
            SlotRect(x = 620, y = 50,  width = 290, height = 360),
            SlotRect(x = 310, y = 440, width = 290, height = 360),
            SlotRect(x = 620, y = 440, width = 290, height = 360),
            SlotRect(x = 310, y = 830, width = 290, height = 360),
            SlotRect(x = 620, y = 830, width = 290, height = 360)
        )
    )

    // ─── NO FRAME FILTERS (FAMILY & TRAVELING) ───────────────────────
    // These have no frame, photos are arranged in a plain grid
    val PLAIN_4 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 10,  y = 10,  width = 450, height = 610),
            SlotRect(x = 470, y = 10,  width = 450, height = 610),
            SlotRect(x = 10,  y = 630, width = 450, height = 604),
            SlotRect(x = 470, y = 630, width = 450, height = 604)
        )
    )

    val PLAIN_6 = FilterLayout(
        frameWidth = 930,
        frameHeight = 1244,
        slots = listOf(
            SlotRect(x = 10,  y = 10,  width = 295, height = 408),
            SlotRect(x = 320, y = 10,  width = 295, height = 408),
            SlotRect(x = 625, y = 10,  width = 295, height = 408),
            SlotRect(x = 10,  y = 428, width = 295, height = 408),
            SlotRect(x = 320, y = 428, width = 295, height = 408),
            SlotRect(x = 625, y = 428, width = 295, height = 408)
        )
    )

    /**
     * Get layout config for a given filter and grid count.
     */
    fun getLayout(filterType: FilterType, gridCount: Int): FilterLayout {
        return when (filterType) {
            FilterType.EMOJI -> if (gridCount == 4) EMOJI_4 else EMOJI_6
            FilterType.FOOTBALL -> if (gridCount == 4) FOOTBALL_4 else FOOTBALL_6
            FilterType.VALENTINE -> if (gridCount == 4) VALENTINE_4 else VALENTINE_6
            FilterType.FRIENDSHIP -> if (gridCount == 4) FRIENDSHIP_4 else FRIENDSHIP_6
            FilterType.FLOWERS -> if (gridCount == 4) FLOWERS_4 else FLOWERS_6
            FilterType.NONE, FilterType.FAMILY, FilterType.TRAVELING ->
                if (gridCount == 4) PLAIN_4 else PLAIN_6
        }
    }
}