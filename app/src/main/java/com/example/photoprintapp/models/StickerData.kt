package com.example.photoprintapp.models

data class StickerData(
    val id: String,
    val assetPath: String,  // path di assets/sticker/
    val x: Float,           // center x position pada preview (px)
    val y: Float,           // center y position pada preview (px)
    val size: Float = 100f,
    val rotation: Float = 0f
)