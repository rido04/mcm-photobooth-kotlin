package com.example.photoprintapp.models

data class SlotRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class FilterLayout(
    val frameWidth: Int,
    val frameHeight: Int,
    val slots: List<SlotRect>
)

object FilterLayoutConfig {

    // ─── EMOJI ─────────────────────────────
    val EMOJI_4 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(58, 120, 390, 400),
            SlotRect(482, 120, 390, 400),
            SlotRect(58, 560, 390, 400),
            SlotRect(482, 560, 390, 400)
        )
    )

    val EMOJI_6 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(310, 50, 290, 360),
            SlotRect(620, 50, 290, 360),
            SlotRect(310, 440, 290, 360),
            SlotRect(620, 440, 290, 360),
            SlotRect(310, 830, 290, 360),
            SlotRect(620, 830, 290, 360)
        )
    )

    // ─── FOOTBALL ─────────────────────────
    val FOOTBALL_4 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(310, 50, 290, 260),
            SlotRect(620, 50, 290, 260),
            SlotRect(310, 340, 290, 260),
            SlotRect(620, 340, 290, 260)
        )
    )

    val FOOTBALL_6 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(310, 50, 290, 340),
            SlotRect(620, 50, 290, 340),
            SlotRect(310, 430, 290, 340),
            SlotRect(620, 430, 290, 340),
            SlotRect(310, 820, 290, 340),
            SlotRect(620, 820, 290, 340)
        )
    )

    // ─── FRIENDSHIP ───────────────────────
    val FRIENDSHIP_4 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(58, 120, 390, 400),
            SlotRect(482, 120, 390, 400),
            SlotRect(58, 560, 390, 400),
            SlotRect(482, 560, 390, 400)
        )
    )

    val FRIENDSHIP_6 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(310, 50, 290, 360),
            SlotRect(620, 50, 290, 360),
            SlotRect(310, 440, 290, 360),
            SlotRect(620, 440, 290, 360),
            SlotRect(310, 830, 290, 360),
            SlotRect(620, 830, 290, 360)
        )
    )

    // ─── FLOWERS ──────────────────────────
    val FLOWERS_4 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(58, 120, 390, 400),
            SlotRect(482, 120, 390, 400),
            SlotRect(58, 560, 390, 400),
            SlotRect(482, 560, 390, 400)
        )
    )

    val FLOWERS_6 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(310, 50, 290, 360),
            SlotRect(620, 50, 290, 360),
            SlotRect(310, 440, 290, 360),
            SlotRect(620, 440, 290, 360),
            SlotRect(310, 830, 290, 360),
            SlotRect(620, 830, 290, 360)
        )
    )

    // ─── PLAIN (NO FRAME) ─────────────────
    val PLAIN_4 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(10, 10, 450, 610),
            SlotRect(470, 10, 450, 610),
            SlotRect(10, 630, 450, 604),
            SlotRect(470, 630, 450, 604)
        )
    )

    val PLAIN_6 = FilterLayout(
        930, 1244,
        listOf(
            SlotRect(10, 10, 295, 408),
            SlotRect(320, 10, 295, 408),
            SlotRect(625, 10, 295, 408),
            SlotRect(10, 428, 295, 408),
            SlotRect(320, 428, 295, 408),
            SlotRect(625, 428, 295, 408)
        )
    )

    // ─── MAIN MAPPING ─────────────────────
    fun getLayout(filterType: FilterType, gridCount: Int): FilterLayout {
        return when (filterType) {
            FilterType.EMOJI -> if (gridCount == 4) EMOJI_4 else EMOJI_6
            FilterType.FOOTBALL -> if (gridCount == 4) FOOTBALL_4 else FOOTBALL_6
            FilterType.FRIENDSHIP -> if (gridCount == 4) FRIENDSHIP_4 else FRIENDSHIP_6
            FilterType.FLOWERS -> if (gridCount == 4) FLOWERS_4 else FLOWERS_6

            // 👇 AL AZHAR sementara pake plain dulu
            FilterType.AL_AZHAR,
            FilterType.AL_AZHAR_41,
            FilterType.NONE,
            FilterType.FAMILY -> 
                if (gridCount == 4) PLAIN_4 else PLAIN_6
        }
    }
}