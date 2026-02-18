package com.example.photoprintapp.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R

data class FilterItem(
    val key: String,
    val name: String,
    val desc: String,
    val bgAsset: String  // path di assets, e.g. "background-filter/emoticon.png"
)

class FilterAdapter(
    private val context: Context,
    private val filters: List<FilterItem>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<FilterAdapter.VH>() {

    private var selectedKey = "NONE"

    fun setSelected(key: String) {
        val old = filters.indexOfFirst { it.key == selectedKey }
        selectedKey = key
        val new = filters.indexOfFirst { it.key == key }
        if (old >= 0) notifyItemChanged(old)
        if (new >= 0) notifyItemChanged(new)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(filters[position], filters[position].key == selectedKey)
    }

    override fun getItemCount() = filters.size

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val ivBg: ImageView = v.findViewById(R.id.ivBackground)
        private val tvName: TextView = v.findViewById(R.id.tvName)
        private val tvDesc: TextView = v.findViewById(R.id.tvDesc)
        private val viewSelected: View = v.findViewById(R.id.viewSelected)
        private val ivCheck: ImageView = v.findViewById(R.id.ivCheck)

        fun bind(item: FilterItem, isSelected: Boolean) {
            tvName.text = item.name
            tvDesc.text = item.desc

            // Load background dari assets
            try {
                val stream = context.assets.open(item.bgAsset)
                val bmp = BitmapFactory.decodeStream(stream)
                stream.close()
                ivBg.setImageBitmap(bmp)
            } catch (e: Exception) {
                ivBg.setImageResource(android.R.color.darker_gray)
            }

            // Selected state
            viewSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
            ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onSelected(item.key)
                setSelected(item.key)
            }
        }
    }
}