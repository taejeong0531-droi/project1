package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editEmail: TextInputEditText
    private lateinit var editNickname: TextInputEditText
    private lateinit var editCurrentPassword: TextInputEditText
    private lateinit var editNewPassword: TextInputEditText
    private lateinit var editConfirmPassword: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        editEmail = findViewById(R.id.editEmail)
        editNickname = findViewById(R.id.editNickname)
        editCurrentPassword = findViewById(R.id.editCurrentPassword)
        editNewPassword = findViewById(R.id.editNewPassword)
        editConfirmPassword = findViewById(R.id.editConfirmPassword)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        // Prefill current values
        val user = Firebase.auth.currentUser
        editEmail.setText(user?.email ?: "")
        editNickname.setText(user?.displayName ?: user?.email?.substringBefore('@') ?: "")

        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveProfile() }
    }

    private fun saveProfile() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val newEmail = editEmail.text?.toString()?.trim().orEmpty()
        val newNickname = editNickname.text?.toString()?.trim().orEmpty()
        val currentPw = editCurrentPassword.text?.toString().orEmpty()
        val newPw = editNewPassword.text?.toString().orEmpty()
        val confirmPw = editConfirmPassword.text?.toString().orEmpty()

        if (newEmail.isEmpty()) {
            Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPw.isNotEmpty() && newPw.length < 6) {
            Toast.makeText(this, "새 비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPw.isNotEmpty() && newPw != confirmPw) {
            Toast.makeText(this, "새 비밀번호 확인이 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // Determine if reauth is needed (email or password change)
        val wantsEmailChange = newEmail != (user.email ?: "")
        val wantsPasswordChange = newPw.isNotEmpty()

        fun proceedUpdates() {
            // Chain updates: displayName -> email -> password
            updateDisplayName(newNickname) {
                updateEmailIfNeeded(newEmail, wantsEmailChange) {
                    updatePasswordIfNeeded(newPw, wantsPasswordChange) {
                        Toast.makeText(this, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        if (wantsEmailChange || wantsPasswordChange) {
            if (user.email.isNullOrEmpty()) {
                Toast.makeText(this, "현재 계정 이메일을 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }
            if (currentPw.isEmpty()) {
                Toast.makeText(this, "현재 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                return
            }
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPw)
            user.reauthenticate(credential)
                .addOnSuccessListener { proceedUpdates() }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "재인증 실패: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        } else {
            proceedUpdates()
        }
    }

    private fun updateDisplayName(nickname: String, onDone: () -> Unit) {
        val user = Firebase.auth.currentUser ?: return onDone()
        val trimmed = nickname.trim()
        if (trimmed.isEmpty() || trimmed == (user.displayName ?: "")) return onDone()
        val request = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(trimmed)
            .build()
        user.updateProfile(request)
            .addOnSuccessListener {
                // Also persist alias to Firestore users/{uid}.nickname
                val uid = user.uid
                Firebase.firestore.collection("users").document(uid)
                    .set(mapOf("nickname" to trimmed), com.google.firebase.firestore.SetOptions.merge())
                    .addOnCompleteListener { _ -> onDone() }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "닉네임 변경 실패: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateEmailIfNeeded(newEmail: String, needed: Boolean, onDone: () -> Unit) {
        val user = Firebase.auth.currentUser ?: return onDone()
        if (!needed) return onDone()
        user.updateEmail(newEmail)
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { e ->
                Toast.makeText(this, "이메일 변경 실패: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updatePasswordIfNeeded(newPw: String, needed: Boolean, onDone: () -> Unit) {
        val user = Firebase.auth.currentUser ?: return onDone()
        if (!needed) return onDone()
        user.updatePassword(newPw)
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { e ->
                Toast.makeText(this, "비밀번호 변경 실패: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
