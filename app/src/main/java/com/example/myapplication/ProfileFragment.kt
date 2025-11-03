
package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.content.Intent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Set nickname (displayName -> email prefix -> "사용자") and email
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = view.findViewById(R.id.tvUserEmail)
        fun refreshUserUi() {
            val user = Firebase.auth.currentUser
            val nickname = when {
                !user?.displayName.isNullOrBlank() -> user!!.displayName!!
                !user?.email.isNullOrBlank() -> user!!.email!!.substringBefore('@')
                else -> "사용자"
            }
            val email = user?.email ?: ""
            tvUserName.text = nickname
            tvUserEmail.text = email
        }
        refreshUserUi()

        val logoutBtn: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnLogout)
        logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            (activity as? MainActivity)?.showLoginScreen()
        }

        // 프로필 수정 화면 진입
        val layoutEditProfile: View = view.findViewById(R.id.layoutEditProfile)
        layoutEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }

        // 음식 취향 설정 화면 진입
        val layoutFoodPreference: View = view.findViewById(R.id.layoutFoodPreference)
        layoutFoodPreference.setOnClickListener {
            startActivity(Intent(requireContext(), FoodPreferenceActivity::class.java))
        }

        // 설문조사(감정-음식 경험) 화면 진입
        val layoutEmotionPattern: View = view.findViewById(R.id.layoutEmotionPattern)
        layoutEmotionPattern.setOnClickListener {
            startActivity(Intent(requireContext(), EmotionExperienceActivity::class.java))
        }

        // onResume에서 최신 사용자 정보 반영
        viewLifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                refreshUserUi()
            }
        })
        return view
    }
}
