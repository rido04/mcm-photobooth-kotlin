package com.example.photoprintapp.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R

class StickerAdapter(
    private val context: Context,
    private val stickerFiles: List<String>, // asset filenames e.g. "sticker/beard.png"
    private val onStickerSelected: (String) -> Unit
) : RecyclerView.Adapter<StickerAdapter.VH>() {

    private var selectedPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val file = stickerFiles[position]
        try {
            val stream = context.assets.open(file)
            val bmp = BitmapFactory.decodeStream(stream)
            stream.close()
            holder.iv.setImageBitmap(bmp)
        } catch (e: Exception) {
            holder.iv.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.iv.setBackgroundResource(
            if (position == selectedPos) R.drawable.bg_sticker_item_selected
            else R.drawable.bg_sticker_item
        )

        holder.itemView.setOnClickListener {
            val old = selectedPos
            selectedPos = position
            notifyItemChanged(old)
            notifyItemChanged(position)
            onStickerSelected(file)
        }
    }

    override fun getItemCount() = stickerFiles.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val iv: ImageView = v.findViewById(R.id.ivSticker)
    }
}