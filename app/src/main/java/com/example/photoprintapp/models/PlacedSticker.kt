package com.example.photoprintapp.models

data class PlacedSticker(
    val resId: Int,
    val x: Float,
    val y: Float,
    val size: Float = 120f,
    val rotation: Float = 0f,
)