package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var loginLayout: View
    private lateinit var mainContentLayout: View
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var editTextId: com.google.android.material.textfield.TextInputEditText
    private lateinit var editTextPassword: com.google.android.material.textfield.TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 뷰 초기화
        loginLayout = findViewById(R.id.loginLayout)
        mainContentLayout = findViewById(R.id.mainContentLayout)
        bottomNav = findViewById(R.id.bottom_navigation)
        editTextId = findViewById(R.id.editId)
        editTextPassword = findViewById(R.id.editPassword)

        // 로그인 상태 관찰
        viewModel.isLoggedIn.observe(this, Observer { isLoggedIn ->
            if (isLoggedIn) {
                loginSuccess()
            } else {
                loginLayout.visibility = View.VISIBLE
                mainContentLayout.visibility = View.GONE
                bottomNav.visibility = View.GONE
            }
        })

        // 로그인 버튼 클릭 이벤트
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            attemptLogin()
        }

        // 음식 추천 버튼 클릭 이벤트
        findViewById<View>(R.id.btnRecommendFood).setOnClickListener {
            // EmotionActivity로 이동
            val intent = Intent(this, EmotionActivity::class.java)
            startActivity(intent)
        }

        // 회원가입 버튼 클릭 이벤트
        findViewById<View>(R.id.textSignUp).setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 하단 네비게이션 설정
        setupBottomNavigation()
    }

    private fun attemptLogin() {
        val id = editTextId.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 여기에 실제 로그인 로직 구현 (임시로 성공 처리)
        viewModel.performLogin()
        
        // 입력 필드 초기화
        editTextId.text?.clear()
        editTextPassword.text?.clear()
    }

    private fun loginSuccess() {
        // 로그인 성공 시 UI 업데이트
        loginLayout.visibility = View.GONE
        mainContentLayout.visibility = View.VISIBLE
        bottomNav.visibility = View.VISIBLE
        
        // 로그인 성공 토스트 메시지
        Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // 홈 화면으로 이동
                    mainContentLayout.visibility = View.VISIBLE
                    true
                }
                R.id.navigation_history -> {
                    // 기록 화면으로 이동
                    // TODO: 기록 화면 구현 후 추가
                    true
                }
                R.id.navigation_profile -> {
                    // 마이페이지로 이동
                    // TODO: 마이페이지 화면 구현 후 추가
                    true
                }
                else -> false
            }
        }
    }
}
