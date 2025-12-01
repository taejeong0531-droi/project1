package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.EmotionEntry
import com.example.myapplication.data.EmotionRepository
import com.example.myapplication.data.FoodSelection
import com.example.myapplication.network.ApiClient
import com.example.myapplication.network.model.RecommendRequest
import com.example.myapplication.network.model.PreferencesReq
import com.example.myapplication.network.model.RecentLogReq
import com.example.myapplication.network.model.EmotionVector
import com.example.myapplication.ui.EmotionViewModel
import com.example.myapplication.util.UserIdProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.google.android.gms.tasks.Tasks

class EmotionActivity : AppCompatActivity() {
    private val emotionViewModel: EmotionViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var btnVeryNo: Button
    private lateinit var btnNo: Button
    private lateinit var btnMid: Button
    private lateinit var btnYes: Button
    private lateinit var btnVeryYes: Button
    private lateinit var btnMore: Button
    private lateinit var btnBack: ImageButton
    private lateinit var progress: ProgressBar
    private lateinit var textQuestion: TextView
    private lateinit var textQuestionCounter: TextView
    private lateinit var textResultTitle: TextView
    private lateinit var textEmotionResult: TextView
    private lateinit var explanationBox: View
    private lateinit var imageTopEgg: ImageView
    private lateinit var recyclerFoods: RecyclerView
    private lateinit var adapter: EmotionAdapter
    private lateinit var repository: EmotionRepository
    
    // 설문: 카테고리별 질문 풀에서 랜덤 2개씩 선택(총 10문항)
    private data class QA(val category: String, val text: String)
    private val categories: Map<String, List<String>> = mapOf(
        "기쁨-슬픔" to listOf(
            "오늘 하루 기분은 대체로 밝은 편인가요?",
            "최근에 사소한 일에도 미소가 지어졌나요?",
            "최근 3일 내 즐거웠던 순간이 떠오르나요?",
            "요즘 마음이 무겁거나 우울하게 느껴지나요?", // 부정
            "최근 눈물이 날 만큼 속상했던 적이 있었나요?", // 부정
            "아침에 일어났을 때 기분이 가벼웠나요?"
        ),
        "피로-활력" to listOf(
            "몸이 피곤하거나 기운이 빠진 느낌이 있나요?", // 부정(피로)
            "지난밤 수면이 충분했다고 느끼나요?",
            "가벼운 운동을 할 에너지가 있나요?",
            "하루 종일 축 늘어진 느낌이 있었나요?", // 부정
            "커피나 에너지 드링크 없이도 버틸 수 있었나요?",
            "쉬고 싶다는 생각이 자주 들었나요?" // 부정
        ),
        "외로움-안정감" to listOf(
            "요즘 혼자 있는 시간이 외롭다고 느껴지나요?", // 부정(외로움)
            "오늘 누군가와 대화를 나누며 위안을 받았나요?",
            "주변 사람들과의 관계가 안정적이라고 느끼나요?",
            "누군가에게 기대고 싶다는 생각이 들었나요?", // 부정
            "스스로를 편안히 돌볼 시간이 있었나요?",
            "혼자 있으면 마음이 허전하게 느껴지나요?" // 부정
        ),
        "스트레스-여유" to listOf(
            "요즘 일이나 공부 때문에 머리가 복잡한가요?", // 부정(스트레스)
            "오늘은 마음의 여유를 느낀 순간이 있었나요?",
            "해결되지 않은 걱정이 머릿속을 떠나지 않았나요?", // 부정
            "숨 고르기나 짧은 휴식을 일부러 했나요?",
            "긴장으로 인해 몸이 뻣뻣하거나 두통이 있었나요?", // 부정
            "일과 휴식의 균형이 잘 맞았다고 느끼나요?"
        ),
        "집중-산만" to listOf(
            "요즘 집중이 잘 되는 편인가요?",
            "해야 할 일에 몰입한 시간이 있었나요?",
            "생각이 자꾸 다른 데로 새거나 멍해지는 순간이 많았나요?", // 부정
            "스마트폰/알림 때문에 자주 흐트러졌나요?", // 부정
            "일의 우선순위를 정하고 차근차근 진행했나요?",
            "사소한 소음에도 쉽게 산만해졌나요?" // 부정
        )
    )
    private lateinit var selectedQA: List<QA>
    private var currentIndex = 0
    // 감정 벡터 누적
    private val vector = mutableMapOf(
        "joy" to 0,
        "energy" to 0,
        "social" to 0, // 안정감(+)/외로움(-)
        "calm" to 0,   // 여유(+)/스트레스(-)
        "focus" to 0   // 집중(+)/산만(-)
    )
    // 카테고리 가중치(분석 민감도)
    private val weights: Map<String, Float> = mapOf(
        "joy" to 1.0f,
        "energy" to 1.2f,
        "social" to 1.0f,
        "calm" to 1.2f,
        "focus" to 0.8f
    )
    // 카테고리별 선택 문항 수(총 문항수 = 5 * 이 값)
    private val QUESTIONS_PER_CATEGORY = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion)

        // Resolve user id (prefer Firebase UID, fallback to per-installation id)
        userId = resolveUserId()

        // Start Firestore real-time observation (optional UI usage)
        emotionViewModel.startObserving(userId)
        Log.d("EmotionActivity", "Resolved userId onCreate: $userId")

        // View refs
        btnVeryNo = findViewById(R.id.btnVeryNo)
        btnNo = findViewById(R.id.btnNo)
        btnMid = findViewById(R.id.btnMid)
        btnYes = findViewById(R.id.btnYes)
        btnVeryYes = findViewById(R.id.btnVeryYes)
        btnMore = findViewById(R.id.btnMore)
        btnBack = findViewById(R.id.btnBack)
        progress = findViewById(R.id.progress)
        textQuestion = findViewById(R.id.textQuestion)
        textQuestionCounter = findViewById(R.id.textQuestionCounter)
        textResultTitle = findViewById(R.id.textResultTitle)
        textEmotionResult = findViewById(R.id.textEmotionResult)
        explanationBox = findViewById(R.id.explanationBox)
        imageTopEgg = findViewById(R.id.imageTopEgg)
        recyclerFoods = findViewById(R.id.recyclerFoods)

        // Recycler setup
        adapter = EmotionAdapter(onClickMore = { food -> onFoodSelected(food) })
        recyclerFoods.layoutManager = LinearLayoutManager(this)
        recyclerFoods.adapter = adapter

        // Repository 초기화 (로컬 Room)
        val database = AppDatabase.getDatabase(this)
        repository = EmotionRepository(database.emotionDao())

        // 카테고리별 랜덤 문항 선택
        selectedQA = categories.flatMap { (cat, list) ->
            list.shuffled().take(QUESTIONS_PER_CATEGORY).map { q -> QA(cat, q) }
        }
        // 첫 질문 표시
        updateQuestion()

        // 5점 척도 응답: -2, -1, 0, +1, +2
        btnVeryNo.setOnClickListener { onAnswerScore(-2) }
        btnNo.setOnClickListener { onAnswerScore(-1) }
        btnMid.setOnClickListener { onAnswerScore(0) }
        btnYes.setOnClickListener { onAnswerScore(+1) }
        btnVeryYes.setOnClickListener { onAnswerScore(+2) }
        btnMore.setOnClickListener { showOtherFoods() }
        btnBack.setOnClickListener { goBackOneStep() }

        // Prepare auth listener to react to account changes while screen is on
        authListener = { _ ->
            val newId = resolveUserId()
            if (newId != userId) {
                userId = newId
                Log.d("EmotionActivity", "Auth changed. New userId: $userId")
                emotionViewModel.startObserving(userId)
            }
        }
    }
    private var lastEmotionLabel: String? = null
    private var lastScore: Float = 0.9f
    private var lastRecommendedFoods: List<FoodItem> = emptyList()
    private var altIndex: Int = 0
    private var savedOnce: Boolean = false
    private var selectionCommitted: Boolean = false
    private var authListener: ((com.google.firebase.auth.FirebaseAuth) -> Unit)? = null
    private var forceNoAuthTest: Boolean = true

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnVeryNo.isEnabled = !loading
        btnNo.isEnabled = !loading
        btnMid.isEnabled = !loading
        btnYes.isEnabled = !loading
        btnVeryYes.isEnabled = !loading
    }

    private fun fetchIdToken(): String? {
        val user = Firebase.auth.currentUser ?: return null
        return try {
            val result = Tasks.await(user.getIdToken(false))
            result?.token
        } catch (e: Exception) {
            null
        }
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
                  // health pre-check: if backend is unavailable, skip recommend
                  val healthy = try { ApiClient.api.health(); true } catch (e: Exception) { false }
                  if (!healthy) return@withContext null
                  val token = if (forceNoAuthTest) null else fetchIdToken()
                  val authHeader = token?.let { "Bearer $it" }
                  val uidForApi = if (forceNoAuthTest) "dev-local-user" else (Firebase.auth.currentUser?.uid ?: "dev-local-user")
                  Log.d("EmotionActivity", "recommend authHeaderPresent=${authHeader != null} uidForApi=$uidForApi")
                  val resp = ApiClient.api.recommend(
                      authHeader,
                      RecommendRequest(
                          user_id = uidForApi,
                          text = emotionLabel,
                          emotion_label = emotionLabel,
                          emotion_vector = EmotionVector(
                              joy = vector["joy"] ?: 0,
                              energy = vector["energy"] ?: 0,
                              social = vector["social"] ?: 0,
                              calm = vector["calm"] ?: 0,
                              focus = vector["focus"] ?: 0
                          ),
                          score_intensity = lastScore.toDouble(),
                          weather = null,
                          recent_logs = null,
                          preferences = PreferencesReq(
                              likes = loadSelectedTags(),
                              dislikes = null,
                              sensitive_spicy = null
                          )
                      )
                  )
                  Log.d("EmotionActivity", "recommend status=${resp.code()} success=${resp.isSuccessful}")
                  if (!resp.isSuccessful) {
                      val err = try { resp.errorBody()?.string() } catch (e: Exception) { null }
                      Log.e("EmotionActivity", "recommend error body=${err}")
                  }
                  resp
              } catch (e: Exception) {
                  Log.e("EmotionActivity", "recommend exception", e)
                  null
              }
          }

          val fromApi = response?.isSuccessful == true && response.body() != null
          var foods: List<FoodItem> = if (fromApi) {
              response!!.body()!!.items.map { fs ->
                  FoodItem(
                      id = fs.name,
                      name = fs.name,
                      imageUrl = null,
                      calories = fs.kcal,
                      tags = fs.tags
                  )
              }
          } else {
              android.widget.Toast.makeText(this@EmotionActivity, "네트워크 문제로 기본 추천을 보여드려요", android.widget.Toast.LENGTH_SHORT).show()
              getFoodsFor(emotionLabel, altIndex)
          }

          if (foods.size > 4) foods = foods.take(4)
          if (!fromApi && foods.size < 4) {
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
          // 결과 설명 표시
          explanationBox.visibility = View.VISIBLE
          textEmotionResult.visibility = View.VISIBLE
          textEmotionResult.text = buildExplanation()

          // 리스트 표시
          adapter.submitList(foods)
          recyclerFoods.visibility = View.VISIBLE
          btnMore.visibility = View.VISIBLE

          // 자동 저장 제거: 사용자가 실제로 음식을 선택할 때만 기록을 저장합니다.
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
                    // health pre-check
                    val healthy = try { ApiClient.api.health(); true } catch (e: Exception) { false }
                    if (!healthy) return@withContext null
                    val token = if (forceNoAuthTest) null else fetchIdToken()
                    val authHeader = token?.let { "Bearer $it" }
                    val uidForApi = if (forceNoAuthTest) "dev-local-user" else (Firebase.auth.currentUser?.uid ?: "dev-local-user")
                    Log.d("EmotionActivity", "recommend(more) authHeaderPresent=${authHeader != null} uidForApi=$uidForApi")
                    val resp = ApiClient.api.recommend(
                        authHeader,
                        RecommendRequest(
                            user_id = uidForApi,
                            text = when (label) { "happy", "angry", "neutral" -> label else -> "neutral" },
                            emotion_label = when (label) { "happy", "angry", "neutral" -> label else -> "neutral" },
                            emotion_vector = EmotionVector(
                                joy = vector["joy"] ?: 0,
                                energy = vector["energy"] ?: 0,
                                social = vector["social"] ?: 0,
                                calm = vector["calm"] ?: 0,
                                focus = vector["focus"] ?: 0
                            ),
                            score_intensity = lastScore.toDouble(),
                            weather = null,
                            recent_logs = lastRecommendedFoods.map { f ->
                                RecentLogReq(food = f.name, timestamp = java.time.Instant.now().toString())
                            },
                            preferences = PreferencesReq(
                                likes = loadSelectedTags(),
                                dislikes = null,
                                sensitive_spicy = null
                            )
                        )
                    )
                    Log.d("EmotionActivity", "recommend(more) status=${resp.code()} success=${resp.isSuccessful}")
                    if (!resp.isSuccessful) {
                        val err = try { resp.errorBody()?.string() } catch (e: Exception) { null }
                        Log.e("EmotionActivity", "recommend(more) error body=${err}")
                    }
                    resp
                } catch (e: Exception) {
                    Log.e("EmotionActivity", "recommend(more) exception", e)
                    null
                }
            }

            val fromApi = response?.isSuccessful == true && response.body() != null
            var foods: List<FoodItem> = if (fromApi) {
                response!!.body()!!.items.map { fs ->
                    FoodItem(
                        id = fs.name,
                        name = fs.name,
                        imageUrl = null,
                        calories = fs.kcal,
                        tags = fs.tags
                    )
                }
            } else {
                android.widget.Toast.makeText(this@EmotionActivity, "네트워크 문제로 기본 추천을 보여드려요", android.widget.Toast.LENGTH_SHORT).show()
                altIndex += 1
                getFoodsFor(
                    when (label) { "happy", "angry", "neutral" -> label else -> "neutral" },
                    altIndex
                )
            }

            if (foods.size > 4) foods = foods.take(4)
            if (!fromApi && foods.size < 4) {
                val extra = getFoodsFor(label, altIndex + 1)
                foods = (foods + extra).distinctBy { it.id }.take(4)
            }

            lastRecommendedFoods = foods
            adapter.submitList(foods)
            recyclerFoods.visibility = View.VISIBLE
            setLoading(false)
        }
    }

    private fun updateQuestion() {
        val total = selectedQA.size
        val title = selectedQA.getOrNull(currentIndex)?.text ?: selectedQA.last().text
        textQuestion.text = title
        textQuestionCounter.text = "${currentIndex + 1}/$total"
        // 설문 진행 UI 보이기
        textQuestion.visibility = View.VISIBLE
        textQuestionCounter.visibility = View.VISIBLE
        imageTopEgg.visibility = View.VISIBLE
        textResultTitle.visibility = View.GONE
        textEmotionResult.visibility = View.GONE
        explanationBox.visibility = View.GONE
        recyclerFoods.visibility = View.GONE
        btnVeryNo.visibility = View.VISIBLE
        btnNo.visibility = View.VISIBLE
        btnMid.visibility = View.VISIBLE
        btnYes.visibility = View.VISIBLE
        btnVeryYes.visibility = View.VISIBLE
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

    private fun resolveUserId(): String {
        val uid = Firebase.auth.currentUser?.uid
        return uid ?: UserIdProvider.getOrCreate(this)
    }

    private fun goBackOneStep() {
        explanationBox.visibility = View.GONE
        textEmotionResult.visibility = View.GONE
        textResultTitle.visibility = View.GONE
        recyclerFoods.visibility = View.GONE
        btnMore.visibility = View.GONE
        adapter.submitList(emptyList())

        // 결과 화면 상태라면 설문 마지막 문항으로 되돌림
        if (textResultTitle.visibility == View.VISIBLE || currentIndex >= selectedQA.size) {
            currentIndex = (selectedQA.size - 1).coerceAtLeast(0)
            updateQuestion()
            return
        }

        // 설문 도중이면 한 문항 뒤로
        if (currentIndex > 0) {
            currentIndex -= 1
            updateQuestion()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        goBackOneStep()
    }

    override fun onStart() {
        super.onStart()
        authListener?.let { Firebase.auth.addAuthStateListener(it) }
    }

    override fun onStop() {
        super.onStop()
        authListener?.let { Firebase.auth.removeAuthStateListener(it) }
    }

    private fun onAnswerScore(score: Int) {
        val qa = selectedQA.getOrNull(currentIndex) ?: return
        val sign = polarityFor(qa)
        when (qa.category) {
            "기쁨-슬픔" -> vector["joy"] = (vector["joy"] ?: 0) + score * sign
            "피로-활력" -> vector["energy"] = (vector["energy"] ?: 0) + score * sign
            "외로움-안정감" -> vector["social"] = (vector["social"] ?: 0) + score * sign
            "스트레스-여유" -> vector["calm"] = (vector["calm"] ?: 0) + score * sign
            "집중-산만" -> vector["focus"] = (vector["focus"] ?: 0) + score * sign
        }

        currentIndex += 1
        if (currentIndex < selectedQA.size) {
            updateQuestion()
            return
        }

        val label = decideMoodFromVector()
        analyzeWithEmotion(label)
        btnVeryNo.visibility = View.GONE
        btnNo.visibility = View.GONE
        btnMid.visibility = View.GONE
        btnYes.visibility = View.GONE
        btnVeryYes.visibility = View.GONE
    }

    private fun polarityFor(qa: QA): Int {
        val t = qa.text
        val negativeKeywords = listOf(
            "무겁", "우울", "눈물", "피곤", "축 늘어", "쉬고 싶", "외롭", "허전",
            "스트레스", "긴장", "두통", "불안", "초조", "산만", "멍", "복잡", "새는",
            "흐트러", "방해", "지치"
        )
        val hasNeg = negativeKeywords.any { t.contains(it) }
        return if (hasNeg) -1 else +1
    }

    private fun decideMoodFromVector(): String {
        val joy = (vector["joy"] ?: 0) * (weights["joy"] ?: 1f)
        val energy = (vector["energy"] ?: 0) * (weights["energy"] ?: 1f)
        val calm = (vector["calm"] ?: 0) * (weights["calm"] ?: 1f)
        val focus = (vector["focus"] ?: 0) * (weights["focus"] ?: 1f)
        return when {
            joy >= 2f && energy >= 1f -> "happy"
            calm <= -1f || energy <= -2f -> "angry"
            else -> "neutral"
        }
    }

    private fun buildExplanation(): String {
        val sb = StringBuilder()
        val joy = (vector["joy"] ?: 0) * (weights["joy"] ?: 1f)
        val energy = (vector["energy"] ?: 0) * (weights["energy"] ?: 1f)
        val social = (vector["social"] ?: 0) * (weights["social"] ?: 1f)
        val calm = (vector["calm"] ?: 0) * (weights["calm"] ?: 1f)
        val focus = (vector["focus"] ?: 0) * (weights["focus"] ?: 1f)

        if (energy <= -1f) sb.appendLine("에너지가 낮게 측정되어 따뜻한 국물/가벼운 메뉴를 우선합니다.")
        if (calm <= -1f) sb.appendLine("스트레스/긴장이 감지되어 자극적인 음식은 낮게 평가했습니다.")
        if (social <= -1f) sb.appendLine("외로움 지표로 따뜻한 국물/면요리를 소폭 가산했습니다.")
        if (joy >= 2f) sb.appendLine("기쁨 지표로 가벼운 양식/면류에 소폭 가산했습니다.")
        if (focus <= -1f) sb.appendLine("집중 저하로 아주 무거운 메뉴는 감점했습니다.")
        if (sb.isEmpty()) sb.append("균형 잡힌 상태로 다양한 메뉴를 제안합니다.")
        return sb.toString().trim()
    }

    private fun clearSurvey() {
        currentIndex = 0
        vector.keys.forEach { key -> vector[key] = 0 }
        btnVeryNo.visibility = View.VISIBLE
        btnNo.visibility = View.VISIBLE
        btnMid.visibility = View.VISIBLE
        btnYes.visibility = View.VISIBLE
        btnVeryYes.visibility = View.VISIBLE
        textQuestion.visibility = View.VISIBLE
        textResultTitle.visibility = View.GONE
        explanationBox.visibility = View.GONE
        updateQuestion()
    }

    private fun resetResult() {
        adapter.submitList(emptyList())
        textEmotionResult.visibility = View.GONE
        btnMore.visibility = View.GONE
        clearSurvey()
    }

    private fun onFoodSelected(selectedFood: FoodItem) {
        if (selectionCommitted) return
        selectionCommitted = true
        val emotionLabel = lastEmotionLabel ?: "neutral"
        lifecycleScope.launch {
            val today = LocalDate.now().toEpochDay()
            val entry = EmotionEntry(
                userId = userId,
                dateEpochDay = today,
                emotion = emotionLabel,
                score = lastScore
            )

            // 선택한 음식 1개만 저장
            val foodSelections = listOf(
                FoodSelection(
                    entryId = 0,
                    name = selectedFood.name,
                    calories = selectedFood.calories,
                    tags = selectedFood.tags.joinToString(","),
                    isSelected = true
                )
            )

            repository.saveEmotionAnalysis(entry, foodSelections)

            // 홈 화면으로 돌아가기
            finish()
        }
    }

    /**
     * 사용자가 `FoodPreferenceActivity`에서 저장한 태그를 불러온다.
     */
    private fun loadSelectedTags(): List<String> {
        val prefs = getSharedPreferences(FoodPreferenceActivity.PREFS_NAME, MODE_PRIVATE)
        val set = prefs.getStringSet(FoodPreferenceActivity.KEY_SELECTED_TAGS, emptySet())
            ?: emptySet()
        return set.toList()
    }

    /**
     * 추천 결과가 처음 표시될 때, 사용자가 항목을 탭하지 않아도 분석 기록이 남도록 1회 저장한다.
     * 모든 항목은 isSelected = false 로 저장한다.
     */
    private fun saveEmotionResult(label: String, score: Float, foods: List<FoodItem>) {
        lifecycleScope.launch {
            val today = LocalDate.now().toEpochDay()
            val entry = EmotionEntry(
                userId = userId,
                dateEpochDay = today,
                emotion = label,
                score = score
            )

            val selections = foods.map { food ->
                FoodSelection(
                    entryId = 0,
                    name = food.name,
                    calories = food.calories,
                    tags = food.tags.joinToString(","),
                    isSelected = false
                )
            }

            repository.saveEmotionAnalysis(entry, selections)
        }
    }
}
