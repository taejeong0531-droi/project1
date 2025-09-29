package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // 뷰 초기화
        editTextName = findViewById(R.id.editNickname)  // 닉네임 필드를 이름으로 사용
        editTextId = findViewById(R.id.editSignupId)
        editTextPassword = findViewById(R.id.editSignupPassword)
        editTextConfirmPassword = findViewById(R.id.editConfirmPassword)
        buttonSignUp = findViewById(R.id.btnCompleteSignUp)

        // 회원가입 버튼 클릭 이벤트
        buttonSignUp.setOnClickListener {
            attemptSignUp()
        }
    }

    private fun attemptSignUp() {
        val name = editTextName.text.toString().trim()
        val id = editTextId.text.toString().trim()
        val password = editTextPassword.text.toString()
        val confirmPassword = editTextConfirmPassword.text.toString()

        // 유효성 검사
        when {
            name.isEmpty() -> {
                editTextName.error = "이름을 입력해주세요."
                return
            }
            id.isEmpty() -> {
                editTextId.error = "아이디를 입력해주세요."
                return
            }
            password.isEmpty() -> {
                editTextPassword.error = "비밀번호를 입력해주세요."
                return
            }
            password != confirmPassword -> {
                editTextConfirmPassword.error = "비밀번호가 일치하지 않습니다."
                return
            }
        }

        // 여기에 회원가입 로직 구현 (예: 서버 통신)
        // 임시로 성공했다고 가정
        Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        
        // 회원가입 성공 시 로그인 화면으로 돌아가기
        val resultIntent = Intent()
        resultIntent.putExtra("id", id)
        resultIntent.putExtra("password", password)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
