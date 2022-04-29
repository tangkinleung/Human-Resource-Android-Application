/**
 * Login and Registration of user with firebase authentication
 * Author: Kin Leung (Ui) & Wong Jun Hao (Firebase Auth)
 * References: https://firebase.google.com/docs/auth/android/
 */
package com.example.a2007_hr_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a2007_hr_app.databinding.ActivityLoginScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

val TAG: String = "LoginScreenActivity"

class LoginScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        supportActionBar?.hide()

        val intent = Intent(this, NavigationActivity::class.java)

        binding.buttonLogin.setOnClickListener {

            if (binding.editTextUsername.text.isBlank() or binding.editTextPassword.text.isBlank()) {
                Toast.makeText(
                    this, "Email or Password is Missing",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                /**
                 * Firebase Authentication
                 */
                auth.signInWithEmailAndPassword(
                    binding.editTextUsername.text.toString(),
                    binding.editTextPassword.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            Toast.makeText(
                                baseContext, "Login Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            val user = auth.currentUser
                            startActivity(intent)
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                baseContext, "Wrong Email or Password. Please try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        /**
         * Auto-Register a default user for development purpose
         */
        binding.textViewRegister?.setOnClickListener {
            /**
             * Firebase Authentication
             */
            auth.createUserWithEmailAndPassword("devUser@devmail.com", "devUser")
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Register success
                        Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(
                            baseContext, "Default DevUser Register Successful",
                            Toast.LENGTH_SHORT
                        ).show()
                        val user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = "devUser"
                        }
                        user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Profile Updated")
                            }
                        }
                    } else {
                        // Register Failed
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Register failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val intent = Intent(this, NavigationActivity::class.java)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(intent)
            finish()
        }
    }

}