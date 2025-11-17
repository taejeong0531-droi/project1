package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var editNickname: TextInputEditText
    private lateinit var editSignupPassword: TextInputEditText
    private lateinit var editConfirmPassword: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var btnCompleteSignUp: MaterialButton
    private lateinit var btnBackSignup: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // 뷰 초기화
        editNickname = findViewById(R.id.editNickname)
        editSignupPassword = findViewById(R.id.editSignupPassword)
        editConfirmPassword = findViewById(R.id.editConfirmPassword)
        editEmail = findViewById(R.id.editEmail)
        btnCompleteSignUp = findViewById(R.id.btnCompleteSignUp)
        btnBackSignup = findViewById(R.id.btnBackSignup)

        // 뒤로가기 버튼 클릭 시 종료
        btnBackSignup.setOnClickListener { finish() }

        // 실시간 검증 리스너 등록
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateRealtime()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        editEmail.addTextChangedListener(watcher)
        editSignupPassword.addTextChangedListener(watcher)
        editConfirmPassword.addTextChangedListener(watcher)
        editNickname.addTextChangedListener(watcher)
        // 초기 버튼 상태 업데이트
        validateRealtime()

        // 회원가입 버튼 클릭 리스너
        btnCompleteSignUp.setOnClickListener {
            if (validateInputs()) {
                val email = editEmail.text.toString().trim()
                val password = editSignupPassword.text.toString()
                val nickname = editNickname.text.toString().trim()

                // Firebase Auth로 회원 생성
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = task.result?.user?.uid
                            // Firestore에 사용자 문서(선택) 생성
                            uid?.let { id ->
                                val userDoc = mapOf(
                                    "nickname" to nickname,
                                    // username은 이메일로 고정 저장
                                    "username" to email,
                                    "email" to email
                                )
                                Firebase.firestore.collection("users").document(id)
                                    .set(userDoc)
                            }

                            Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            // 로그인 화면으로 돌아가기(이메일/비번 전달)
                            val resultIntent = Intent().apply {
                                putExtra("id", email)
                                putExtra("password", password)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            showAuthError(task.exception)
                        }
                    }
            }
        }
    }

    private fun validateInputs(): Boolean {
        val nickname = editNickname.text.toString().trim()
        val password = editSignupPassword.text.toString()
        val confirmPassword = editConfirmPassword.text.toString()
        val email = editEmail.text.toString().trim()

        // 닉네임 검사
        if (nickname.isEmpty()) {
            editNickname.error = "닉네임을 입력해주세요."
            return false
        }

        // 비밀번호 검사
        if (password.isEmpty()) {
            editSignupPassword.error = "비밀번호를 입력해주세요."
            return false
        }
        
        if (password.length < 6) {
            editSignupPassword.error = "비밀번호는 6자 이상이어야 합니다."
            return false
        }

        // 비밀번호 확인
        if (password != confirmPassword) {
            editConfirmPassword.error = "비밀번호가 일치하지 않습니다."
            return false
        }

        // 이메일 검사
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "유효한 이메일 주소를 입력해주세요."
            return false
        }

        return true
    }

    private fun showAuthError(e: Exception?) {
        val message = when (e) {
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "이미 사용 중인 이메일입니다. 로그인 또는 비밀번호 재설정을 해주세요."
                    "ERROR_INVALID_EMAIL" -> "유효하지 않은 이메일 형식입니다."
                    "ERROR_WEAK_PASSWORD" -> "비밀번호는 6자 이상이어야 합니다."
                    "ERROR_USER_DISABLED" -> "사용이 중지된 계정입니다."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "이 인증 방식이 비활성화되어 있습니다."
                    else -> "회원가입 실패: ${e.localizedMessage ?: e.errorCode}"
                }
            }
            else -> e?.localizedMessage ?: "회원가입 중 오류가 발생했습니다."
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun validateRealtime() {
        val email = editEmail.text?.toString()?.trim().orEmpty()
        val password = editSignupPassword.text?.toString().orEmpty()
        val confirm = editConfirmPassword.text?.toString().orEmpty()
        val nickname = editNickname.text?.toString()?.trim().orEmpty()

        // 이메일 형식
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "유효한 이메일 주소를 입력해주세요."
        } else {
            editEmail.error = null
        }

        // 비밀번호 길이
        if (password.isNotEmpty() && password.length < 6) {
            editSignupPassword.error = "비밀번호는 6자 이상이어야 합니다."
        } else {
            editSignupPassword.error = null
        }

        // 비밀번호 확인 일치
        if (confirm.isNotEmpty() && confirm != password) {
            editConfirmPassword.error = "비밀번호가 일치하지 않습니다."
        } else {
            editConfirmPassword.error = null
        }

        // 버튼 활성화 조건: 모든 필수값 유효
        val validEmail = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val validPw = password.length >= 6
        val validMatch = confirm == password && confirm.isNotEmpty()
        val validNickname = nickname.isNotEmpty()
        btnCompleteSignUp.isEnabled = validEmail && validPw && validMatch && validNickname
    }
}

