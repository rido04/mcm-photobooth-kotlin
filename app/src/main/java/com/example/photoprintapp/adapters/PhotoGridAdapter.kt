package com.example.photoprintapp.adapters

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
// import android.view_group.ViewGroup
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.photoprintapp.R
import java.io.File

class PhotoGridAdapter(
    private val photos: MutableList<String?>
) : RecyclerView.Adapter<PhotoGridAdapter.VH>() {

    fun updatePhotos(newPhotos: List<String?>) {
        photos.clear()
        photos.addAll(newPhotos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_slot, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(photos[position], position)
    }

    override fun getItemCount() = photos.size

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val ivThumb: ImageView = v.findViewById(R.id.ivThumb)
        private val layoutEmpty: LinearLayout = v.findViewById(R.id.layoutEmpty)
        private val tvSlotLabel: TextView = v.findViewById(R.id.tvSlotLabel)

        fun bind(path: String?, index: Int) {
            tvSlotLabel.text = "Foto ${index + 1}"
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                    ivThumb.setImageBitmap(bmp)
                    ivThumb.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                }
            } else {
                ivThumb.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
            }
        }
    }
}