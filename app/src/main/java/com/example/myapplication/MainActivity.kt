package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.myapplication.auth.FindAccountActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var loginLayout: View
    private lateinit var mainContentLayout: View
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var editTextId: com.google.android.material.textfield.TextInputEditText
    private lateinit var editTextPassword: com.google.android.material.textfield.TextInputEditText
    
    // 프래그먼트 태그 상수
    private val HOME_FRAGMENT_TAG = "home_fragment"
    private val HISTORY_FRAGMENT_TAG = "history_fragment"
    private val PROFILE_FRAGMENT_TAG = "profile_fragment"
    
    // 프래그먼트 초기화 지연
    private val homeFragment: HomeFragment by lazy { HomeFragment() }
    private val historyFragment: HistoryFragment by lazy { HistoryFragment() }
    private val profileFragment: ProfileFragment by lazy { ProfileFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 뷰 초기화
        loginLayout = findViewById(R.id.loginLayout)
        mainContentLayout = findViewById(R.id.mainContentLayout)
        bottomNav = findViewById(R.id.bottom_navigation)
        editTextId = findViewById(R.id.editId)
        editTextPassword = findViewById(R.id.editPassword)
        
        // 초기 UI 상태 설정
        loginLayout.visibility = View.VISIBLE
        mainContentLayout.visibility = View.GONE
        bottomNav.visibility = View.GONE

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

        // 비밀번호 찾기 텍스트 클릭 이벤트
        findViewById<TextView>(R.id.textFindPassword).setOnClickListener {
            // FindAccountActivity로 이동
            val intent = Intent(this, FindAccountActivity::class.java)
            startActivity(intent)
        }

        // 회원가입 텍스트 클릭 이벤트
        findViewById<TextView>(R.id.textSignUp).setOnClickListener {
            // SignUpActivity로 이동
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 음식 추천 버튼 클릭은 HomeFragment에서 처리합니다.

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
        
        // 하단 네비게이션 초기화
        setupBottomNavigation()
        
        // 로그인 성공 토스트 메시지
        Toast.makeText(this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()
        
        // 키보드 숨기기
        currentFocus?.let {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setupBottomNavigation() {
        // 프래그먼트 매니저 초기화
        val fragmentManager = supportFragmentManager
        
        // 이미 추가된 프래그먼트가 있는지 확인
        var homeFragment = fragmentManager.findFragmentByTag(HOME_FRAGMENT_TAG) as? HomeFragment
        var historyFragment = fragmentManager.findFragmentByTag(HISTORY_FRAGMENT_TAG) as? HistoryFragment
        var profileFragment = fragmentManager.findFragmentByTag(PROFILE_FRAGMENT_TAG) as? ProfileFragment
        
        // 프래그먼트가 없으면 새로 생성
        if (homeFragment == null) homeFragment = this.homeFragment
        if (historyFragment == null) historyFragment = this.historyFragment
        if (profileFragment == null) profileFragment = this.profileFragment
        
        // 초기 프래그먼트 설정 (이미 추가되어 있지 않다면 추가)
        if (fragmentManager.findFragmentByTag(HOME_FRAGMENT_TAG) == null &&
            fragmentManager.findFragmentByTag(HISTORY_FRAGMENT_TAG) == null &&
            fragmentManager.findFragmentByTag(PROFILE_FRAGMENT_TAG) == null) {
            fragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, homeFragment, HOME_FRAGMENT_TAG)
                add(R.id.fragment_container, historyFragment, HISTORY_FRAGMENT_TAG).hide(historyFragment)
                add(R.id.fragment_container, profileFragment, PROFILE_FRAGMENT_TAG).hide(profileFragment)
                commit()
            }
        }
        
        // 하단 네비게이션 리스너 설정
        bottomNav.setOnItemSelectedListener { item ->
            val transaction = fragmentManager.beginTransaction()
            
            // 모든 프래그먼트 숨기기
            fragmentManager.fragments.forEach { transaction.hide(it) }
            
            when (item.itemId) {
                R.id.navigation_home -> {
                    transaction.show(homeFragment)
                    true
                }
                R.id.navigation_history -> {
                    transaction.show(historyFragment)
                    true
                }
                R.id.navigation_profile -> {
                    transaction.show(profileFragment)
                    true
                }
                else -> false
            }.also { isHandled ->
                if (isHandled) {
                    transaction.setReorderingAllowed(true)
                    transaction.commit()
                }
            }
        }
        
        // 기본 선택 항목 설정
        bottomNav.selectedItemId = R.id.navigation_home
    }
}
