package com.example.myapplication
  
  import android.os.Bundle
  import android.view.View
  import android.widget.Button
  import android.widget.ProgressBar
  import android.widget.TextView
  import android.widget.ImageButton
  import android.widget.ImageView
  import androidx.activity.viewModels
  import androidx.appcompat.app.AppCompatActivity
  import androidx.lifecycle.lifecycleScope
  import androidx.recyclerview.widget.LinearLayoutManager
  import androidx.recyclerview.widget.RecyclerView
  import com.example.myapplication.data.AppDatabase
  import com.example.myapplication.data.EmotionEntry
  import com.example.myapplication.data.EmotionRepository
  import com.example.myapplication.data.FoodSelection
  import com.example.myapplication.ui.EmotionViewModel
  import com.example.myapplication.util.UserIdProvider
  import com.example.myapplication.network.ApiClient
  import com.example.myapplication.network.model.RecommendRequest
  import com.google.firebase.ktx.Firebase
  import com.google.firebase.auth.ktx.auth
  import kotlinx.coroutines.Dispatchers
  import kotlinx.coroutines.launch
  import kotlinx.coroutines.withContext
  import java.time.LocalDate
  class EmotionActivity : AppCompatActivity() {
      private val emotionViewModel: EmotionViewModel by viewModels()
      private lateinit var userId: String
      private lateinit var btnGood: Button
      private lateinit var btnAngry: Button
    private lateinit var btnNone: Button
    private lateinit var btnMore: Button
    private lateinit var btnBack: ImageButton
    private lateinit var progress: ProgressBar
    private lateinit var textQuestion: TextView
    private lateinit var textQuestionCounter: TextView
    private lateinit var textResultTitle: TextView
    private lateinit var textEmotionResult: TextView
    private lateinit var imageTopEgg: ImageView
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
    private val answersHistory = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        // Per-installation userId (no Auth)
        userId = UserIdProvider.getOrCreate(this)

        // Start Firestore real-time observation (optional UI usage)
        emotionViewModel.startObserving(userId)

        // View refs
        btnGood = findViewById(R.id.btnGood)
        btnAngry = findViewById(R.id.btnAngry)
        btnNone = findViewById(R.id.btnNone)
        btnMore = findViewById(R.id.btnMore)
        btnBack = findViewById(R.id.btnBack)
        progress = findViewById(R.id.progress)
        textQuestion = findViewById(R.id.textQuestion)
        textQuestionCounter = findViewById(R.id.textQuestionCounter)
        textResultTitle = findViewById(R.id.textResultTitle)
        textEmotionResult = findViewById(R.id.textEmotionResult)
        imageTopEgg = findViewById(R.id.imageTopEgg)
        recyclerFoods = findViewById(R.id.recyclerFoods)

        // Recycler setup
        adapter = EmotionAdapter(onClickMore = { food -> onFoodSelected(food) })
        recyclerFoods.layoutManager = LinearLayoutManager(this)
        recyclerFoods.adapter = adapter

        // Repository 초기화 (로컬 Room)
        val database = AppDatabase.getDatabase(this)
        repository = EmotionRepository(database.emotionDao())

        // 첫 질문 표시
        updateQuestion()

        // 버튼 클릭으로 응답 저장 후 다음 질문 진행
        btnGood.setOnClickListener { onAnswer("happy") }
        btnAngry.setOnClickListener { onAnswer("angry") }
        btnNone.setOnClickListener { onAnswer("neutral") }
        btnMore.setOnClickListener { showOtherFoods() }
        btnBack.setOnClickListener { goBackOneStep() }
    }
    private var lastEmotionLabel: String? = null
    private var lastScore: Float = 0.9f
    private var lastRecommendedFoods: List<FoodItem> = emptyList()
    private var altIndex: Int = 0

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnGood.isEnabled = !loading
        btnAngry.isEnabled = !loading
        btnNone.isEnabled = !loading
    }

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

      // Firestore 로그 저장 (비동기)
      lifecycleScope.launch {
          val score = 0.9f
          lastScore = score
          val scoreInt = (score * 100).toInt()
          emotionViewModel.addEmotion(userId, emotionLabel, note = "", score = scoreInt)
      }

      // 서버 추천 호출
      lifecycleScope.launch {
          val response = withContext(Dispatchers.IO) {
              try {
                  ApiClient.api.recommend(
                      RecommendRequest(
                          mood = emotionLabel,
                          preferences = null,
                          top_k = 4
                      )
                  )
              } catch (e: Exception) {
                  null
              }
          }

          var foods: List<FoodItem> = if (response != null) {
              response.items.map { item ->
                  FoodItem(
                      id = item.name, // 간단히 이름을 ID로 사용
                      name = item.name,
                      imageUrl = null,
                      calories = item.kcal,
                      tags = item.tags
                  )
              }
          } else {
              // 서버 실패 시 로컬 기본 추천으로 폴백
              getFoodsFor(emotionLabel, altIndex)
          }

          // 갯수 보정: 4개로 맞춤
          if (foods.size > 4) foods = foods.take(4)
          if (foods.size < 4) {
              // 다른 세트에서 채워 넣기
              val extra = getFoodsFor(emotionLabel, altIndex + 1)
              foods = (foods + extra).distinctBy { it.id }.take(4)
          }

          lastRecommendedFoods = foods

          // 질문 숨기고 결과 타이틀/텍스트 표시
          textQuestion.visibility = View.GONE
          textQuestionCounter.visibility = View.GONE
          imageTopEgg.visibility = View.GONE
          textResultTitle.visibility = View.VISIBLE
          val nickname = getNickname()
          textResultTitle.text = "${nickname}님을 위한 음식이에요 😊"
          // 퍼센트 제거: 감정 텍스트만 표시
          textEmotionResult.text = "감정: $emotionLabel"
          textEmotionResult.visibility = View.VISIBLE

          // 리스트 표시
          adapter.submitList(foods)
          btnMore.visibility = View.VISIBLE
          setLoading(false)
      }
    }

    private fun getFoodsFor(label: String, alt: Int): List<FoodItem> {
        val sets: List<List<FoodItem>> = when (label) {
            "happy" -> listOf(
                listOf(
                    FoodItem("h1", "상큼 과일 샐러드", null, 220, listOf("상큼", "가벼움")),
                    FoodItem("h2", "탄산수 레몬", null, 0, listOf("청량")),
                    FoodItem("h3", "베리 요거트", null, 180, listOf("상큼", "달콤")),
                    FoodItem("h7", "과일 플레이트", null, 200, listOf("상큼", "가벼움"))
                ),
                listOf(
                    FoodItem("h4", "망고 스무디", null, 260, listOf("달콤", "상큼")),
                    FoodItem("h5", "요거트 파르페", null, 300, listOf("가벼움")),
                    FoodItem("h6", "딸기 케이크", null, 350, listOf("달콤", "행복")),
                    FoodItem("h8", "바나나 팬케이크", null, 420, listOf("달콤"))
                )
            )
            "angry" -> listOf(
                listOf(
                    FoodItem("a1", "매콤 치킨", null, 560, listOf("매운맛", "해소")),
                    FoodItem("a2", "핫 칠리 라면", null, 480, listOf("얼큰")),
                    FoodItem("a3", "김치찌개", null, 420, listOf("얼큰", "해소")),
                    FoodItem("a7", "매운 돈까스", null, 700, listOf("매운맛"))
                ),
                listOf(
                    FoodItem("a4", "매운 떡볶이", null, 520, listOf("매운맛")),
                    FoodItem("a5", "불닭 비빔면", null, 530, listOf("매운맛")),
                    FoodItem("a6", "청양고추 피자", null, 680, listOf("매운맛", "강렬")),
                    FoodItem("a8", "마라샹궈", null, 650, listOf("매운맛"))
                )
            )
            else -> listOf(
                listOf(
                    FoodItem("n1", "연어 샐러드", null, 350, listOf("담백", "건강")),
                    FoodItem("n2", "녹차", null, 0, listOf("은은함")),
                    FoodItem("n3", "닭가슴살 샐러드", null, 280, listOf("담백", "건강")),
                    FoodItem("n7", "두유 스무디", null, 180, listOf("가벼움"))
                ),
                listOf(
                    FoodItem("n4", "두부 샐러드", null, 290, listOf("가벼움")),
                    FoodItem("n5", "캐모마일 티", null, 2, listOf("진정")),
                    FoodItem("n6", "현미밥 정식", null, 450, listOf("건강", "담백")),
                    FoodItem("n8", "야채 수프", null, 220, listOf("담백"))
                )
            )
        }
        val idx = if (sets.isNotEmpty()) (alt % sets.size + sets.size) % sets.size else 0
        return sets.getOrElse(idx) { emptyList() }
    }

    private fun showOtherFoods() {
        val label = lastEmotionLabel ?: return
        // 서버에서 같은 감정으로 새로운 추천 4개를 다시 요청 (오류 시 로컬 세트 폴백)
        lifecycleScope.launch {
            setLoading(true)
            val response = withContext(Dispatchers.IO) {
                try {
                    ApiClient.api.recommend(
                        RecommendRequest(
                            mood = when (label) { "happy", "angry", "neutral" -> label else -> "neutral" },
                            preferences = null,
                            top_k = 4
                        )
                    )
                } catch (e: Exception) {
                    null
                }
            }

            var foods: List<FoodItem> = if (response != null) {
                response.items.map { item ->
                    FoodItem(
                        id = item.name,
                        name = item.name,
                        imageUrl = null,
                        calories = item.kcal,
                        tags = item.tags
                    )
                }
            } else {
                // 서버 실패 시 로컬 다른 세트 사용
                altIndex += 1
                getFoodsFor(
                    when (label) { "happy", "angry", "neutral" -> label else -> "neutral" },
                    altIndex
                )
            }

            if (foods.size > 4) foods = foods.take(4)
            if (foods.size < 4) {
                val extra = getFoodsFor(label, altIndex + 1)
                foods = (foods + extra).distinctBy { it.id }.take(4)
            }

            lastRecommendedFoods = foods
            adapter.submitList(foods)
            setLoading(false)
        }
    }

    private fun updateQuestion() {
        val total = questions.size
        val title = questions.getOrNull(currentIndex) ?: questions.last()
        textQuestion.text = title
        textQuestionCounter.text = "${currentIndex + 1}/$total"
        // 설문 진행 UI 보이기
        textQuestion.visibility = View.VISIBLE
        textQuestionCounter.visibility = View.VISIBLE
        imageTopEgg.visibility = View.VISIBLE
        textResultTitle.visibility = View.GONE
        textEmotionResult.visibility = View.GONE
        btnGood.visibility = View.VISIBLE
        btnAngry.visibility = View.VISIBLE
        btnNone.visibility = View.VISIBLE
        btnMore.visibility = View.GONE
        adapter.submitList(emptyList())
    }

    private fun getNickname(): String {
        val user = Firebase.auth.currentUser
        val display = user?.displayName?.takeIf { it.isNotBlank() }
        if (display != null) return display
        val email = user?.email
        if (!email.isNullOrBlank()) return email.substringBefore('@')
        return "사용자"
    }

    private fun goBackOneStep() {
        // 결과 화면 상태라면 설문 마지막 문항으로 되돌림
        if (textResultTitle.visibility == View.VISIBLE || currentIndex >= questions.size) {
            currentIndex = (questions.size - 1).coerceAtLeast(0)
            // 마지막 선택 취소 처리 (있다면)
            if (answersHistory.isNotEmpty()) {
                val last = answersHistory.removeAt(answersHistory.size - 1)
                scores[last] = (scores[last] ?: 1) - 1
            }
            updateQuestion()
            return
        }

        // 설문 도중이면 한 문항 뒤로
        if (currentIndex > 0) {
            currentIndex -= 1
            // 이전 문항에서 선택한 점수를 되돌림
            if (answersHistory.isNotEmpty()) {
                val last = answersHistory.removeAt(answersHistory.size - 1)
                scores[last] = (scores[last] ?: 1) - 1
            }
            updateQuestion()
        } else {
            // 첫 문항이면 액티비티 종료(필요 시 유지)
            finish()
        }
    }

    override fun onBackPressed() {
        goBackOneStep()
    }

    private fun onAnswer(bucket: String) {
        // 점수 누적
        scores[bucket] = (scores[bucket] ?: 0) + 1
        answersHistory.add(bucket)

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
