package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.EmotionEntry
import com.example.myapplication.data.EmotionRepository
import com.example.myapplication.data.FoodSelection
import kotlinx.coroutines.launch
import java.time.LocalDate

class EmotionActivity : AppCompatActivity() {
    private lateinit var btnGood: Button
    private lateinit var btnAngry: Button
    private lateinit var btnNone: Button
    private lateinit var btnMore: Button
    private lateinit var progress: ProgressBar
    private lateinit var textQuestion: TextView
    private lateinit var textResultTitle: TextView
    private lateinit var textEmotionResult: TextView
    private lateinit var recyclerFoods: RecyclerView
    private lateinit var adapter: EmotionAdapter
    private lateinit var repository: EmotionRepository

    // 5문항 진행 상태
    private val questions = listOf(
        "오늘 하루 중 가장 기억에 남는 일",
        "지금 기분을 한 단어로 고른다면?",
        "오늘 사람들과의 관계는 어땠나요?",
        "몸 컨디션은 어떤가요?",
        "지금 먹고 싶은 음식 느낌은?"
    )
    private var currentIndex = 0
    private val scores = mutableMapOf(
        "happy" to 0,
        "angry" to 0,
        "neutral" to 0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        // View refs
        btnGood = findViewById(R.id.btnGood)
        btnAngry = findViewById(R.id.btnAngry)
        btnNone = findViewById(R.id.btnNone)
        btnMore = findViewById(R.id.btnMore)
        progress = findViewById(R.id.progress)
        textQuestion = findViewById(R.id.textQuestion)
        textResultTitle = findViewById(R.id.textResultTitle)
        textEmotionResult = findViewById(R.id.textEmotionResult)
        recyclerFoods = findViewById(R.id.recyclerFoods)

        // Recycler setup
        adapter = EmotionAdapter(onClickMore = { food -> onFoodSelected(food) })
        recyclerFoods.layoutManager = LinearLayoutManager(this)
        recyclerFoods.adapter = adapter
        
        // Repository 초기화
        val database = AppDatabase.getDatabase(this)
        repository = EmotionRepository(database.emotionDao())

        // 첫 질문 표시
        updateQuestion()

        // 버튼 클릭으로 응답 저장 후 다음 질문 진행
        btnGood.setOnClickListener { onAnswer("happy") }
        btnAngry.setOnClickListener { onAnswer("angry") }
        btnNone.setOnClickListener { onAnswer("neutral") }
        btnMore.setOnClickListener { showOtherFoods() }
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnGood.isEnabled = !loading
        btnAngry.isEnabled = !loading
        btnNone.isEnabled = !loading
    }

    private var lastEmotionLabel: String? = null
    private var lastScore: Float = 0.9f
    private var lastRecommendedFoods: List<FoodItem> = emptyList()
    private var altIndex: Int = 0

    private fun analyzeWithEmotion(label: String) {
        setLoading(true)

        lastEmotionLabel = label
        altIndex = 0
        val emotionLabel = when (label) {
            "happy" -> "happy"
            "angry" -> "angry"
            "none", "neutral" -> "neutral"
            else -> "neutral"
        }
        val foods = getFoodsFor(emotionLabel, altIndex)
        lastRecommendedFoods = foods
        lastScore = 0.9f

        // 질문 숨기고 결과 타이틀 표시
        textQuestion.visibility = View.GONE
        textResultTitle.visibility = View.VISIBLE

        textEmotionResult.text = "감정: $emotionLabel (${(lastScore * 100).toInt()}%)"
        textEmotionResult.visibility = View.VISIBLE
        adapter.submitList(foods)
        btnMore.visibility = View.VISIBLE

        setLoading(false)
    }

    private fun getFoodsFor(label: String, alt: Int): List<FoodItem> {
        val sets: List<List<FoodItem>> = when (label) {
            "happy" -> listOf(
                listOf(
                    FoodItem("h1", "상큼 과일 샐러드", null, 220, listOf("상큼", "가벼움")),
                    FoodItem("h2", "탄산수 레몬", null, 0, listOf("청량")),
                    FoodItem("h3", "베리 요거트", null, 180, listOf("상큼", "달콤"))
                ),
                listOf(
                    FoodItem("h4", "망고 스무디", null, 260, listOf("달콤", "상큼")),
                    FoodItem("h5", "요거트 파르페", null, 300, listOf("가벼움")),
                    FoodItem("h6", "딸기 케이크", null, 350, listOf("달콤", "행복"))
                )
            )
            "angry" -> listOf(
                listOf(
                    FoodItem("a1", "매콤 치킨", null, 560, listOf("매운맛", "해소")),
                    FoodItem("a2", "핫 칠리 라면", null, 480, listOf("얼큰")),
                    FoodItem("a3", "김치찌개", null, 420, listOf("얼큰", "해소"))
                ),
                listOf(
                    FoodItem("a4", "매운 떡볶이", null, 520, listOf("매운맛")),
                    FoodItem("a5", "불닭 비빔면", null, 530, listOf("매운맛")),
                    FoodItem("a6", "청양고추 피자", null, 680, listOf("매운맛", "강렬"))
                )
            )
            else -> listOf(
                listOf(
                    FoodItem("n1", "연어 샐러드", null, 350, listOf("담백", "건강")),
                    FoodItem("n2", "녹차", null, 0, listOf("은은함")),
                    FoodItem("n3", "닭가슴살 샐러드", null, 280, listOf("담백", "건강"))
                ),
                listOf(
                    FoodItem("n4", "두부 샐러드", null, 290, listOf("가벼움")),
                    FoodItem("n5", "캐모마일 티", null, 2, listOf("진정")),
                    FoodItem("n6", "현미밥 정식", null, 450, listOf("건강", "담백"))
                )
            )
        }
        val idx = if (sets.isNotEmpty()) (alt % sets.size + sets.size) % sets.size else 0
        return sets.getOrElse(idx) { emptyList() }
    }

    private fun showOtherFoods() {
        val label = lastEmotionLabel ?: return
        altIndex += 1
        val foods = getFoodsFor(
            when (label) {
                "happy", "angry", "neutral" -> label
                else -> "neutral"
            },
            altIndex
        )
        lastRecommendedFoods = foods  // 현재 표시되는 음식 목록 업데이트
        adapter.submitList(foods)
    }

    private fun updateQuestion() {
        val total = questions.size
        val remain = total - currentIndex
        val title = questions.getOrNull(currentIndex) ?: questions.last()
        textQuestion.text = "$title"
        // 진행 상황을 결과 텍스트에 함께 보여주고 싶다면 여기에 표시 가능
    }

    private fun onAnswer(bucket: String) {
        // 점수 누적
        scores[bucket] = (scores[bucket] ?: 0) + 1

        currentIndex += 1
        if (currentIndex < questions.size) {
            updateQuestion()
            return
        }

        // 5문항 종료 → 최다 득표 감정 선택
        val maxEntry = scores.maxByOrNull { it.value }
        val label = maxEntry?.key ?: "neutral"
        analyzeWithEmotion(label)

        // 응답 종료 후 버튼 유지/숨김 처리 (원하면 숨김)
        btnGood.visibility = View.GONE
        btnAngry.visibility = View.GONE
        btnNone.visibility = View.GONE
    }

    private fun clearSurvey() {
        currentIndex = 0
        scores.keys.forEach { scores[it] = 0 }
        btnGood.visibility = View.VISIBLE
        btnAngry.visibility = View.VISIBLE
        btnNone.visibility = View.VISIBLE
        textQuestion.visibility = View.VISIBLE
        textResultTitle.visibility = View.GONE
        updateQuestion()
    }

    private fun resetResult() {
        adapter.submitList(emptyList())
        textEmotionResult.visibility = View.GONE
        btnMore.visibility = View.GONE
        clearSurvey()
    }
    
    private fun saveEmotionResult(emotion: String, score: Float, foods: List<FoodItem>) {
        lifecycleScope.launch {
            val today = LocalDate.now().toEpochDay()
            val entry = EmotionEntry(
                dateEpochDay = today,
                emotion = emotion,
                score = score
            )
            
            val foodSelections = foods.map { food ->
                FoodSelection(
                    entryId = 0, // DAO에서 자동으로 채워짐
                    name = food.name,
                    calories = food.calories,
                    tags = food.tags.joinToString(",")
                )
            }
            
            repository.saveEmotionAnalysis(entry, foodSelections)
        }
    }
    
    private fun onFoodSelected(selectedFood: FoodItem) {
        val emotionLabel = lastEmotionLabel ?: "neutral"

        // 현재 어댑터에 표시되고 있는 음식 목록에서 선택한 음식을 찾아서 저장
        lifecycleScope.launch {
            val today = LocalDate.now().toEpochDay()
            val entry = EmotionEntry(
                dateEpochDay = today,
                emotion = emotionLabel,
                score = lastScore
            )

            // 현재 표시되고 있는 모든 음식을 저장하되, 선택한 음식만 isSelected = true
            val currentFoods = adapter.getCurrentItems()
            val foodSelections = currentFoods.map { food ->
                FoodSelection(
                    entryId = 0,
                    name = food.name,
                    calories = food.calories,
                    tags = food.tags.joinToString(","),
                    isSelected = food.id == selectedFood.id
                )
            }

            repository.saveEmotionAnalysis(entry, foodSelections)

            // 홈 화면으로 돌아가기
            finish()
        }
    }
}
