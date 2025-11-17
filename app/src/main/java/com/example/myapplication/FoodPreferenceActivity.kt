package com.example.myapplication

import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class FoodPreferenceActivity : AppCompatActivity() {

    private lateinit var cbSpicy: CheckBox
    private lateinit var cbSweet: CheckBox
    private lateinit var cbLight: CheckBox
    private lateinit var cbVeg: CheckBox
    private lateinit var cbNoPork: CheckBox
    private lateinit var cbLowCarb: CheckBox
    private lateinit var cbHighProtein: CheckBox
    private lateinit var cbSoup: CheckBox
    private lateinit var cbAdventurous: CheckBox

    private lateinit var btnSave: MaterialButton
    private lateinit var btnSaveB: MaterialButton
    private lateinit var btnBack: ImageButton

    private lateinit var rootScroll: ScrollView
    private lateinit var sectionB: android.view.View
    private lateinit var rgStress: RadioGroup
    private lateinit var rgGoodMood: RadioGroup
    private lateinit var rgLonely: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_preference)

        cbSpicy = findViewById(R.id.cbSpicy)
        cbSweet = findViewById(R.id.cbSweet)
        cbLight = findViewById(R.id.cbLight)
        cbVeg = findViewById(R.id.cbVeg)
        cbNoPork = findViewById(R.id.cbNoPork)
        cbLowCarb = findViewById(R.id.cbLowCarb)
        cbHighProtein = findViewById(R.id.cbHighProtein)
        cbSoup = findViewById(R.id.cbSoup)
        cbAdventurous = findViewById(R.id.cbAdventurous)
        btnSave = findViewById(R.id.btnSave)
        btnSaveB = findViewById(R.id.btnSaveB)
        btnBack = findViewById(R.id.btnBack)

        rootScroll = findViewById(R.id.rootScroll)
        sectionB = findViewById(R.id.sectionB)
        rgStress = findViewById(R.id.rgStress)
        rgGoodMood = findViewById(R.id.rgGoodMood)
        rgLonely = findViewById(R.id.rgLonely)

        // Load saved preferences
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val saved = prefs.getStringSet(KEY_SELECTED_TAGS, emptySet()) ?: emptySet()
        cbSpicy.isChecked = "spicy" in saved
        cbSweet.isChecked = "sweet" in saved
        cbLight.isChecked = "light" in saved
        cbVeg.isChecked = "veg" in saved
        cbNoPork.isChecked = "no_pork" in saved
        cbLowCarb.isChecked = "low_carb" in saved
        cbHighProtein.isChecked = "high_protein" in saved
        cbSoup.isChecked = "soup" in saved
        cbAdventurous.isChecked = "adventurous" in saved

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener {
            // Save A then smooth scroll to B
            savePreferencesA()
            // 스크롤을 B 섹션으로 이동
            rootScroll.post {
                rootScroll.smoothScrollTo(0, sectionB.top)
            }
            Toast.makeText(this, "A(취향)이 저장되었습니다. B 설문을 이어서 진행하세요.", Toast.LENGTH_SHORT).show()
        }
        btnSaveB.setOnClickListener { saveAndExitB() }
    }

    private fun savePreferencesA() {
        val selected = buildSet {
            if (cbSpicy.isChecked) add("spicy")
            if (cbSweet.isChecked) add("sweet")
            if (cbLight.isChecked) add("light")
            if (cbVeg.isChecked) add("veg")
            if (cbNoPork.isChecked) add("no_pork")
            if (cbLowCarb.isChecked) add("low_carb")
            if (cbHighProtein.isChecked) add("high_protein")
            if (cbSoup.isChecked) add("soup")
            if (cbAdventurous.isChecked) add("adventurous")
        }
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_SELECTED_TAGS, selected).apply()
    }

    private fun saveAndExitB() {
        // Save B selections (nullable allowed)
        val stress = selectedValue(rgStress)
        val good = selectedValue(rgGoodMood)
        val lonely = selectedValue(rgLonely)

        val obj = org.json.JSONObject()
        stress?.let { obj.put("stress_craving", it) }
        good?.let { obj.put("good_mood_pick", it) }
        lonely?.let { obj.put("lonely_food", it) }

        val prefs = getSharedPreferences(EXP_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(EXP_KEY_JSON, obj.toString()).apply()

        // Ensure A is saved as well before exit
        savePreferencesA()
        Toast.makeText(this, "A/B가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun selectedValue(group: RadioGroup): String? {
        val id = group.checkedRadioButtonId
        if (id == -1) return null
        val rb = findViewById<RadioButton>(id)
        return rb.tag as? String
    }

    companion object {
        const val PREFS_NAME = "food_prefs"
        const val KEY_SELECTED_TAGS = "selected_tags"
        const val EXP_PREFS_NAME = "experience_prefs"
        const val EXP_KEY_JSON = "experience_json"
    }
}
