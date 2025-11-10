package com.example.myapplication.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityFindAccountBinding

class FindAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()

        // 직접 프래그먼트를 컨테이너에 로드
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, FindIdFragment())
                .commit()
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}
