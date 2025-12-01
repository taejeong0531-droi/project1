package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class FoodItem(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val calories: Int?,
    val tags: List<String> = emptyList(),
)

class EmotionAdapter(
    private var items: List<FoodItem> = emptyList(),
    private val onClickMore: (FoodItem) -> Unit = {}
) : RecyclerView.Adapter<EmotionAdapter.FoodVH>() {

    fun submitList(newItems: List<FoodItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getCurrentItems(): List<FoodItem> = items.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodVH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: FoodVH, position: Int) {
        holder.bind(items[position], onClickMore)
    }

    class FoodVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.imageFood)
        private val name: TextView = itemView.findViewById(R.id.textFoodName)
        private val tags: TextView = itemView.findViewById(R.id.textFoodTags)
        private val calories: TextView = itemView.findViewById(R.id.textFoodCalories)
        private val btnMore: Button = itemView.findViewById(R.id.btnMore)

        fun bind(item: FoodItem, onClickMore: (FoodItem) -> Unit) {
            name.text = item.name
            tags.text = if (item.tags.isNotEmpty()) item.tags.joinToString("  ", prefix = "#", transform = { it }) else ""
            calories.text = item.calories?.let { "$it kcal" } ?: ""
            // 이미지 로드 (item.imageUrl 우선, 없으면 FoodImages 매핑)
            val url = item.imageUrl ?: FoodImages.urlFor(item.name)
            if (url.isNullOrBlank()) {
                image.setImageResource(R.mipmap.ic_launcher)
            } else {
                Glide.with(image).load(url)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .centerCrop()
                    .into(image)
            }

            btnMore.setOnClickListener { onClickMore(item) }
        }
    }
}
