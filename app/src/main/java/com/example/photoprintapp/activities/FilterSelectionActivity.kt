package com.example.photoprintapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R
import com.example.photoprintapp.adapters.FilterAdapter
import com.example.photoprintapp.adapters.FilterItem

class FilterSelectionActivity : AppCompatActivity() {

    private var selectedFilter = "NONE"

    // bgAsset harus sesuai nama file di assets/background-filter/
    private val filters = listOf(
        FilterItem("NONE",       "No Filter",    "Original photo",          "background-filter/no-filter.png"),
        FilterItem("EMOJI",      "Emoji Fun",    "Fun emoji frame",         "background-filter/emoticon.png"),
        FilterItem("FOOTBALL",   "Football",     "Football fan frame",      "background-filter/football.png"),
        FilterItem("VALENTINE",  "Valentine",    "Romantic couple",         "background-filter/love.png"),
        FilterItem("FRIENDSHIP", "Friendship",   "With your besties",       "background-filter/friend.png"),
        FilterItem("FLOWERS",    "Flower Power", "Beautiful flowers",       "background-filter/flower.png"),
        FilterItem("FAMILY",     "Family",       "Enjoy family moments",    "background-filter/family.png"),
        FilterItem("TRAVELING",  "Traveling",    "Beauty of world",         "background-filter/travel.png")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_selection)

        val adapter = FilterAdapter(this, filters) { key ->
            selectedFilter = key
        }

        val rv = findViewById<RecyclerView>(R.id.rvFilters)
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = adapter
        adapter.setSelected("NONE")

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val intent = Intent(this, PhotoboothActivity::class.java)
            intent.putExtra("filter", selectedFilter)
            startActivity(intent)
        }
    }
}