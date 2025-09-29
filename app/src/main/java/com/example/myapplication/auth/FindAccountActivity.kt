package com.example.myapplication.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityFindAccountBinding
import com.google.android.material.tabs.TabLayoutMediator

class FindAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFindAccountBinding
    private val tabTitles = arrayOf("아이디 찾기", "비밀번호 찾기")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupBackButton()
    }

    private fun setupViewPager() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = FindAccountPagerAdapter(this)
        viewPager.adapter = adapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

class FindAccountPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FindIdFragment()
            else -> FindPasswordFragment()
        }
    }
}
