package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SupportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val textEmail: TextView = findViewById(R.id.textEmail)
        val textFaq: TextView = findViewById(R.id.textFaq)

        btnBack.setOnClickListener { finish() }

        val pendingMsg = "기능 준비중입니다"
        textEmail.setOnClickListener {
            Toast.makeText(this, pendingMsg, Toast.LENGTH_SHORT).show()
        }

        textFaq.setOnClickListener {
            Toast.makeText(this, pendingMsg, Toast.LENGTH_SHORT).show()
        }
    }
}
