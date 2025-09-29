package com.example.myapplication.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentFindIdBinding

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
            
            // TODO: 실제 서버 연동 시 이메일로 아이디 찾기 요청
            // 임시로 테스트용 코드
            findUserId(email)
        }
    }
    
    private fun findUserId(email: String) {
        // TODO: 서버 연동 시 실제 API 호출로 변경
        // 임시로 1초 후에 결과 표시
        binding.textFindIdResult.visibility = View.VISIBLE
        binding.textFindIdResult.text = "${email}@example.com\n으로 가입된 아이디가 있습니다."
        
        // 실제 구현 시에는 아래와 같이 서버 통신을 합니다.
        /*
        authService.findUserId(email).enqueue(object : Callback<FindIdResponse> {
            override fun onResponse(call: Call<FindIdResponse>, response: Response<FindIdResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()
                    binding.textFindIdResult.visibility = View.VISIBLE
                    binding.textFindIdResult.text = "${result?.email}로 가입된 아이디: ${result?.username}"
                } else {
                    showError("아이디를 찾을 수 없습니다.")
                }
            }

            override fun onFailure(call: Call<FindIdResponse>, t: Throwable) {
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
