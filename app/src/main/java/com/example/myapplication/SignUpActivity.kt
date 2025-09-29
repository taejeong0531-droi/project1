package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var editNickname: TextInputEditText
    private lateinit var editSignupId: TextInputEditText
    private lateinit var editSignupPassword: TextInputEditText
    private lateinit var editConfirmPassword: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var btnCompleteSignUp: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // 뷰 초기화
        editNickname = findViewById(R.id.editNickname)
        editSignupId = findViewById(R.id.editSignupId)
        editSignupPassword = findViewById(R.id.editSignupPassword)
        editConfirmPassword = findViewById(R.id.editConfirmPassword)
        editEmail = findViewById(R.id.editEmail)
        btnCompleteSignUp = findViewById(R.id.btnCompleteSignUp)

        // 회원가입 버튼 클릭 리스너
        btnCompleteSignUp.setOnClickListener {
            if (validateInputs()) {
                // 모든 유효성 검사 통과 시 회원가입 성공 처리
                Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                
                // 로그인 화면으로 돌아가기
                val resultIntent = Intent().apply {
                    putExtra("id", editSignupId.text.toString())
                    putExtra("password", editSignupPassword.text.toString())
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val nickname = editNickname.text.toString().trim()
        val id = editSignupId.text.toString().trim()
        val password = editSignupPassword.text.toString()
        val confirmPassword = editConfirmPassword.text.toString()
        val email = editEmail.text.toString().trim()

        // 닉네임 검사
        if (nickname.isEmpty()) {
            editNickname.error = "닉네임을 입력해주세요."
            return false
        }

        // 아이디 검사
        if (id.isEmpty()) {
            editSignupId.error = "아이디를 입력해주세요."
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
}
