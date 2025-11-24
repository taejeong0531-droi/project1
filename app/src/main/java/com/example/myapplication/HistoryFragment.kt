package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.EmotionRepository
import com.example.myapplication.util.UserIdProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class HistoryFragment : Fragment() {
    
    private lateinit var calendarView: CalendarView
    private lateinit var textSelectedDate: TextView
    private lateinit var recyclerHistory: RecyclerView
    private lateinit var textEmptyState: TextView
    private lateinit var adapter: HistoryAdapter
    private lateinit var repository: EmotionRepository
    
    private var selectedDateEpochDay: Long = LocalDate.now().toEpochDay()
    private lateinit var userId: String
    private var entriesJob: Job? = null
    private var authListener: ((com.google.firebase.auth.FirebaseAuth) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // View 초기화
        calendarView = view.findViewById(R.id.calendarView)
        textSelectedDate = view.findViewById(R.id.textSelectedDate)
        recyclerHistory = view.findViewById(R.id.recyclerHistory)
        textEmptyState = view.findViewById(R.id.textEmptyState)
        
        // Repository 초기화
        val database = AppDatabase.getDatabase(requireContext())
        repository = EmotionRepository(database.emotionDao())
        
        // RecyclerView 설정
        adapter = HistoryAdapter(onDetailClick = { })
        recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        recyclerHistory.adapter = adapter
        
        // Resolve user id (prefer Firebase UID, fallback to per-installation id)
        userId = resolveUserId()
        Log.d("HistoryFragment", "Resolved userId onViewCreated: $userId")
        
        // 오늘 날짜로 초기화
        viewLifecycleOwner.lifecycleScope.launch {
            // 과거 자동저장 잔재 정리
            repository.cleanupEntriesWithoutSelection(userId)
            updateSelectedDate(selectedDateEpochDay)
        }
        
        // 달력 날짜 선택 리스너
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            selectedDateEpochDay = selectedDate.toEpochDay()
            viewLifecycleOwner.lifecycleScope.launch {
                repository.cleanupEntriesWithoutSelection(userId)
                updateSelectedDate(selectedDateEpochDay)
            }
        }

        // Prepare auth listener
        authListener = { _ ->
            val newId = resolveUserId()
            if (newId != userId) {
                userId = newId
                Log.d("HistoryFragment", "Auth changed. New userId: $userId")
                // reload for new user
                entriesJob?.cancel()
                viewLifecycleOwner.lifecycleScope.launch {
                    repository.cleanupEntriesWithoutSelection(userId)
                    updateSelectedDate(selectedDateEpochDay)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        authListener?.let { Firebase.auth.addAuthStateListener(it) }
        // Auto-refresh when returning to this screen
        viewLifecycleOwner.lifecycleScope.launch {
            repository.cleanupEntriesWithoutSelection(userId)
            updateSelectedDate(selectedDateEpochDay)
        }
    }

    override fun onStop() {
        super.onStop()
        authListener?.let { Firebase.auth.removeAuthStateListener(it) }
        entriesJob?.cancel()
    }
    
    private fun updateSelectedDate(dateEpochDay: Long) {
        val localDate = LocalDate.ofEpochDay(dateEpochDay)
        val dateFormat = SimpleDateFormat("yyyy년 M월 d일의 기록", Locale.KOREAN)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        textSelectedDate.text = dateFormat.format(date)
        
        loadEntriesForDate(dateEpochDay)
    }
    
    private fun loadEntriesForDate(dateEpochDay: Long) {
        entriesJob?.cancel()
        entriesJob = viewLifecycleOwner.lifecycleScope.launch {
            repository.getEntriesByDate(dateEpochDay, userId).collectLatest { entries ->
                if (entries.isEmpty()) {
                    recyclerHistory.visibility = View.GONE
                    textEmptyState.visibility = View.VISIBLE
                } else {
                    recyclerHistory.visibility = View.VISIBLE
                    textEmptyState.visibility = View.GONE
                    
                    // 각 엔티티에 대한 음식 정보 로드 후, 선택된 음식이 있는 기록만 노출
                    val itemsWithFoods = entries.map { entry ->
                        val foods = repository.getFoodsByEntry(entry.id)
                        HistoryItemWithFoods(entry, foods)
                    }
                    val onlySelected = itemsWithFoods.filter { it.foods.any { f -> f.isSelected } }
                    if (onlySelected.isEmpty()) {
                        recyclerHistory.visibility = View.GONE
                        textEmptyState.visibility = View.VISIBLE
                    } else {
                        recyclerHistory.visibility = View.VISIBLE
                        textEmptyState.visibility = View.GONE
                        adapter.submitList(onlySelected)
                    }
                }
            }
        }
    }
    
    private fun showDetailDialog(item: HistoryItemWithFoods) {
        val entry = item.entry
        val foods = item.foods
        
        val emotionText = when (entry.emotion) {
            "happy" -> "😊 행복"
            "angry" -> "😠 화남"
            "neutral" -> "😐 평온"
            else -> entry.emotion
        }
        
        val foodList = foods.joinToString("\n") { food ->
            "• ${food.name} (${food.calories ?: 0} kcal) - ${food.tags}"
        }
        
        val message = """
            감정: $emotionText
            점수: ${(entry.score * 100).toInt()}%
            
            추천 음식:
            $foodList
        """.trimIndent()
        
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun resolveUserId(): String {
        val uid = Firebase.auth.currentUser?.uid
        return uid ?: UserIdProvider.getOrCreate(requireContext())
    }
}
