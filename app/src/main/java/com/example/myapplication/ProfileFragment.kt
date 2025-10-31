
package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
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
        val user = Firebase.auth.currentUser
        val nickname = when {
            !user?.displayName.isNullOrBlank() -> user!!.displayName!!
            !user?.email.isNullOrBlank() -> user!!.email!!.substringBefore('@')
            else -> "사용자"
        }
        val email = user?.email ?: ""
        tvUserName.text = nickname
        tvUserEmail.text = email

        val logoutBtn: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnLogout)
        logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            (activity as? MainActivity)?.showLoginScreen()
        }
        return view
    }
}
