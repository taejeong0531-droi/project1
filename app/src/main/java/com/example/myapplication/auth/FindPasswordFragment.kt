package com.example.myapplication.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFindPasswordBinding

class FindPasswordFragment : Fragment() {
    private var _binding: FragmentFindPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnFindPassword.setOnClickListener {
            val id = binding.editIdForFindPassword.text.toString().trim()
            val email = binding.editEmailForFindPassword.text.toString().trim()
            
            if (id.isEmpty()) {
                showError("아이디를 입력해주세요.")
                return@setOnClickListener
            }
            
            if (email.isEmpty()) {
                showError("이메일을 입력해주세요.")
                return@setOnClickListener
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("유효한 이메일 주소를 입력해주세요.")
                return@setOnClickListener
            }
            
            // TODO: 실제 서버 연동 시 비밀번호 재설정 이메일 전송 요청
            // 임시로 테스트용 코드
            sendPasswordResetEmail(id, email)
        }
    }
    
    private fun sendPasswordResetEmail(id: String, email: String) {
        // TODO: 서버 연동 시 실제 API 호출로 변경
        // 임시로 1초 후에 결과 표시
        binding.textFindPasswordResult.visibility = View.VISIBLE
        binding.textFindPasswordResult.text = "${email}로 비밀번호 재설정 링크를 전송했습니다.\n이메일을 확인해주세요."
        
        // 실제 구현 시에는 아래와 같이 서버 통신을 합니다.
        /*
        authService.requestPasswordReset(PasswordResetRequest(id, email)).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                if (response.isSuccessful) {
                    binding.textFindPasswordResult.visibility = View.VISIBLE
                    binding.textFindPasswordResult.text = "${email}로 비밀번호 재설정 링크를 전송했습니다.\n이메일을 확인해주세요."
                } else {
                    showError("비밀번호 재설정 요청에 실패했습니다.")
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: View) {
                showError("네트워크 오류가 발생했습니다.")
            }
        })
        */
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
