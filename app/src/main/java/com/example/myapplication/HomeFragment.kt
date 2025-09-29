package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // '음식추천받기' 버튼 클릭 시 EmotionActivity로 이동
        view.findViewById<View>(R.id.btnRecommendFood)?.setOnClickListener {
            val intent = Intent(requireContext(), EmotionActivity::class.java)
            startActivity(intent)
        }
    }
}
