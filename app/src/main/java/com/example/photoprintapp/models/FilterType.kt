package com.example.photoprintapp.models

enum class FilterType(
    val displayName: String,
    val description: String,
    val previewDrawable: String,
) {
    NONE("No Filter", "Foto polos tanpa filter", "preview_none"),
    VALENTINE("Valentine", "Tema cinta & romantis", "preview_valentine"),
    BIRTHDAY("Birthday", "Tema ulang tahun ceria", "preview_birthday"),
    FRIENDSHIP("Friendship", "Tema persahabatan", "preview_friendship"),
    FLOWERS("Flowers", "Tema bunga cantik", "preview_flowers"),
    FOOTBALL("Football", "Tema sepak bola", "preview_football"),
    FAMILY("Family", "Tema keluarga hangat", "preview_family"),
    TRAVEL("Travel", "Tema jalan-jalan", "preview_travel");

    /// Ambil resource ID frame overlay berdasarkan jumlah foto
    fun getFrameResId(gridCount: Int): Int {
        // Return 0 kalau tidak ada frame untuk filter ini
        // Nanti isi sesuai drawable yang tersedia
        return 0
    }
}