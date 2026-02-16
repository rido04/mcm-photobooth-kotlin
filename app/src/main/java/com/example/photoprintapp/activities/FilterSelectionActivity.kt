package com.example.photoprintapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.photoprintapp.R
import com.example.photoprintapp.models.FilterType

class FilterSelectionActivity : AppCompatActivity() {

    private var selectedFilter = FilterType.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_selection)

        setupFilterGrid()

        findViewById<View>(R.id.btnContinue).setOnClickListener {
            val intent = Intent(this, PhotoboothActivity::class.java).apply {
                putExtra(PhotoboothActivity.EXTRA_FILTER, selectedFilter.name)
            }
            startActivity(intent)
        }
    }

    private fun setupFilterGrid() {
        val grid = findViewById<GridLayout>(R.id.filterGrid)
        grid.removeAllViews()

        FilterType.entries.forEach { filter ->
            val card = layoutInflater.inflate(R.layout.item_filter_card, grid, false)

            val imgPreview = card.findViewById<ImageView>(R.id.imgFilterPreview)
            val tvName = card.findViewById<TextView>(R.id.tvFilterName)
            val tvDesc = card.findViewById<TextView>(R.id.tvFilterDesc)
            val cardView = card.findViewById<CardView>(R.id.cardFilter)

            tvName.text = filter.displayName
            tvDesc.text = filter.description

            // Load preview image dari drawable
            val resId = resources.getIdentifier(
                filter.previewDrawable, "drawable", packageName
            )
            if (resId != 0) imgPreview.setImageResource(resId)

            cardView.setOnClickListener {
                selectedFilter = filter
                updateSelectionUI(grid, card)
            }

            // Default selection - FIX: Use alpha for selection highlight
            if (filter == selectedFilter) {
                cardView.alpha = 1f
                cardView.scaleX = 1.05f
                cardView.scaleY = 1.05f
            } else {
                cardView.alpha = 0.7f
                cardView.scaleX = 1f
                cardView.scaleY = 1f
            }

            grid.addView(card)
        }
    }

    private fun updateSelectionUI(grid: GridLayout, selectedCard: View) {
        // Reset semua - FIX: Use alpha and scale for selection
        for (i in 0 until grid.childCount) {
            val cardView = grid.getChildAt(i).findViewById<CardView>(R.id.cardFilter)
            cardView?.alpha = 0.7f
            cardView?.scaleX = 1f
            cardView?.scaleY = 1f
        }
        // Highlight selected - FIX: Use alpha and scale
        val cardView = selectedCard.findViewById<CardView>(R.id.cardFilter)
        cardView?.alpha = 1f
        cardView?.scaleX = 1.05f
        cardView?.scaleY = 1.05f
    }
}