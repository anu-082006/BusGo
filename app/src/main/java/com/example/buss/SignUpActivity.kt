package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val name = findViewById<EditText>(R.id.etName)
        val email = findViewById<EditText>(R.id.etEmail)
        val mobile = findViewById<EditText>(R.id.etMobile)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val password = findViewById<EditText>(R.id.etPassword)
        val confirm = findViewById<EditText>(R.id.etConfirmPassword)
        val register = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRegister)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        register.setOnClickListener {
            val passStr = password.text.toString()
            val confStr = confirm.text.toString()
            val nameStr = name.text.toString()
            val emailStr = email.text.toString()
            val mobileStr = mobile.text.toString()
            
            val gender = when(rgGender.checkedRadioButtonId) {
                R.id.rbFemale -> "Female"
                R.id.rbOther -> "Other"
                else -> "Male"
            }

            if (nameStr.isEmpty() || emailStr.isEmpty() || mobileStr.isEmpty() || passStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passStr == confStr) {
                AppPrefsHelper.saveUser(
                    this,
                    nameStr,
                    emailStr,
                    passStr,
                    mobileStr,
                    gender
                )
                // Also save name to the M3 SharedPreferencesHelper for consistency
                SharedPreferencesHelper.saveUserName(this, nameStr)
                
                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
