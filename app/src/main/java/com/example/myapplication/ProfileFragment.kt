package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val logoutBtn: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnLogout)
        logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            (activity as? MainActivity)?.showLoginScreen()
        }
        return view
    }
}
