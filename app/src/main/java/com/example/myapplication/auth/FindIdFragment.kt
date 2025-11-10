package com.example.myapplication.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFindIdBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FindIdFragment : Fragment() {
    private var _binding: FragmentFindIdBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnCheckEmailExists.setOnClickListener {
            val email = binding.editEmailForFindId.text.toString().trim()
            val emailLower = email.lowercase()
            if (email.isEmpty()) {
                showError("이메일을 입력해주세요.")
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("유효한 이메일 주소를 입력해주세요.")
                return@setOnClickListener
            }
            // Auth만 사용해 가입 여부 확인 (우리 앱은 소셜 로그인 없음)
            fetchAuthExistence(emailLower)
        }

        binding.btnFindId.setOnClickListener {
            val email = binding.editEmailForFindId.text.toString().trim()
            
            if (email.isEmpty()) {
                showError("이메일을 입력해주세요.")
                return@setOnClickListener
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("유효한 이메일 주소를 입력해주세요.")
                return@setOnClickListener
            }
            
            // 이메일이 곧 아이디이므로, 비밀번호 재설정 메일을 전송합니다.
            sendPasswordReset(email)
        }
    }
    
    private fun sendPasswordReset(email: String) {
        // Firebase Auth 비밀번호 재설정 메일 전송
        val emailLower = email.lowercase()
        Firebase.auth.sendPasswordResetEmail(emailLower)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.textFindIdResult.visibility = View.VISIBLE
                    binding.textFindIdResult.text = "입력하신 이메일로 비밀번호 재설정 링크를 보냈습니다."
                } else {
                    val msg = task.exception?.localizedMessage ?: "메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요."
                    showError(msg)
                }
            }
    }

    private fun fetchAuthExistence(emailLower: String) {
        Firebase.auth.fetchSignInMethodsForEmail(emailLower)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val methods = task.result?.signInMethods ?: emptyList()
                    val hasPassword = methods.contains("password")
                    showExistenceSimple(hasPassword)
                } else {
                    val msg = task.exception?.localizedMessage ?: "확인 중 오류가 발생했습니다."
                    showError(msg)
                }
            }
    }

    private fun showExistenceSimple(existsPassword: Boolean) {
        binding.textFindIdResult.visibility = View.VISIBLE
        binding.textFindIdResult.text = if (existsPassword) {
            "해당 이메일로 가입된 계정이 있습니다. (이메일/비밀번호)"
        } else {
            "해당 이메일로 가입된 계정을 찾을 수 없습니다."
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
