package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class EmotionExperienceActivity : AppCompatActivity() {

    private lateinit var rgStress: RadioGroup
    private lateinit var rgGoodMood: RadioGroup
    private lateinit var rgLonely: RadioGroup

    private lateinit var btnSkip: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_experience)

        rgStress = findViewById(R.id.rgStress)
        rgGoodMood = findViewById(R.id.rgGoodMood)
        rgLonely = findViewById(R.id.rgLonely)
        btnSkip = findViewById(R.id.btnSkip)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
        btnSkip.setOnClickListener {
            saveExperience(null, null, null)
            goNext()
        }
        btnNext.setOnClickListener {
            val stress = selectedValue(rgStress)
            val good = selectedValue(rgGoodMood)
            val lonely = selectedValue(rgLonely)
            saveExperience(stress, good, lonely)
            goNext()
        }
    }

    private fun selectedValue(group: RadioGroup): String? {
        val id = group.checkedRadioButtonId
        if (id == -1) return null
        val rb = findViewById<RadioButton>(id)
        return rb.tag as? String
    }

    private fun saveExperience(stress: String?, good: String?, lonely: String?) {
        val obj = JSONObject()
        stress?.let { obj.put("stress_craving", it) }
        good?.let { obj.put("good_mood_pick", it) }
        lonely?.let { obj.put("lonely_food", it) }
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_EXPERIENCE_JSON, obj.toString()).apply()
        Toast.makeText(this, "응답이 저장되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun goNext() {
        startActivity(Intent(this, EmotionActivity::class.java))
        finish()
    }

    companion object {
        const val PREFS_NAME = "experience_prefs"
        const val KEY_EXPERIENCE_JSON = "experience_json"
    }
}
