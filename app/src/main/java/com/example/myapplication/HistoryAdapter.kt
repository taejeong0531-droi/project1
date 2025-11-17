package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.EmotionEntry
import com.example.myapplication.data.FoodSelection
import java.text.SimpleDateFormat
import java.util.*

data class HistoryItemWithFoods(
    val entry: EmotionEntry,
    val foods: List<FoodSelection>
)

class HistoryAdapter(
    private var items: List<HistoryItemWithFoods> = emptyList(),
    private val onDetailClick: (HistoryItemWithFoods) -> Unit = {}
) : RecyclerView.Adapter<HistoryAdapter.HistoryVH>() {

    fun submitList(newItems: List<HistoryItemWithFoods>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryVH(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: HistoryVH, position: Int) {
        holder.bind(items[position], onDetailClick)
    }

    class HistoryVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textEmotion: TextView = itemView.findViewById(R.id.textEmotion)
        private val textScore: TextView = itemView.findViewById(R.id.textScore)
        private val textTime: TextView = itemView.findViewById(R.id.textTime)
        private val textRecommended: TextView = itemView.findViewById(R.id.textRecommended)
        private val textSelected: TextView = itemView.findViewById(R.id.textSelected)

        fun bind(item: HistoryItemWithFoods, onDetailClick: (HistoryItemWithFoods) -> Unit) {
            val entry = item.entry
            
            // 감정 표시
            val emotionText = when (entry.emotion) {
                "happy" -> "😊 행복"
                "angry" -> "😠 화남"
                "neutral" -> "😐 평온"
                else -> entry.emotion
            }
            textEmotion.text = "감정: $emotionText"
            
            // 점수 표시 (숨김)
            textScore.visibility = View.GONE
            
            // 시간 표시
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            textTime.text = timeFormat.format(Date(entry.createdAt))
            
            // 추천된 음식 목록 표시 (숨김)
            textRecommended.visibility = View.GONE
            
            // 선택한 음식 표시
            val selectedFoods = item.foods.filter { it.isSelected }
            textSelected.text = if (selectedFoods.isNotEmpty()) {
                "선택: ${selectedFoods.joinToString(", ") { it.name }}"
            } else {
                "선택: 없음"
            }
        }
    }
}
