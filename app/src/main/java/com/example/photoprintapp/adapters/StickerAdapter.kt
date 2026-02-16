package com.example.photoprintapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R

class StickerAdapter(
    private val stickers: List<Int>,
    private val onSelect: (Int) -> Unit,
) : RecyclerView.Adapter<StickerAdapter.VH>() {

    private var selectedPos = -1

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgSticker)
        val highlight: View = view.findViewById(R.id.viewHighlight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sticker, parent, false)
        return VH(v)
    }

    override fun getItemCount() = stickers.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.img.setImageResource(stickers[position])
        holder.highlight.visibility =
            if (position == selectedPos) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val prev = selectedPos
            selectedPos = if (selectedPos == position) -1 else position
            notifyItemChanged(prev)
            notifyItemChanged(selectedPos)
            onSelect(if (selectedPos == -1) 0 else stickers[position])
        }
    }
}