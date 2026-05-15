package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AppPrefsHelper.isLoggedIn(this)) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.etEmail)
        val password = findViewById<EditText>(R.id.etPassword)
        val loginBtn = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogin)
        val signup = findViewById<TextView>(R.id.tvSignup)

        loginBtn.setOnClickListener {
            val e = email.text.toString()
            val p = password.text.toString()

            val savedEmail = AppPrefsHelper.getEmail(this)
            val savedPass = AppPrefsHelper.getPassword(this)

            if (e == savedEmail && p == savedPass) {
                AppPrefsHelper.saveLoginSession(this)
                // Also sync the name to M3 SharedPreferencesHelper
                val name = AppPrefsHelper.getName(this)
                SharedPreferencesHelper.saveUserName(this, name)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
            }
        }

        signup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
