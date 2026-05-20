package com.example.buss

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException

import com.google.android.material.button.MaterialButton

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Modern Activity Result API
    private val googleSignInLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(
                    result.data
                )

            try {

                val account =
                    task.getResult(ApiException::class.java)

                firebaseAuthWithGoogle(
                    account.idToken!!
                )

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Auto login
        if (auth.currentUser != null) {

            startActivity(
                Intent(this, HomeActivity::class.java)
            )

            finish()

            return
        }

        // Email login views
        val email =
            findViewById<com.google.android.material.textfield.TextInputEditText>(
                R.id.etEmail
            )

        val password =
            findViewById<com.google.android.material.textfield.TextInputEditText>(
                R.id.etPassword
            )

        val loginBtn =
            findViewById<MaterialButton>(
                R.id.btnLogin
            )

        val signup =
            findViewById<TextView>(
                R.id.tvSignup
            )

        // Google button
        val googleBtn =
            findViewById<SignInButton>(
                R.id.googleSignInBtn
            )

        // Google Sign In Config
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(
                getString(R.string.default_web_client_id)
            )
            .requestEmail()
            .build()

        googleSignInClient =
            GoogleSignIn.getClient(this, gso)

        // Email login
        loginBtn.setOnClickListener {

            val e = email.text.toString().trim()
            val p = password.text.toString().trim()

            if (e.isEmpty() || p.isEmpty()) {

                Toast.makeText(
                    this,
                    "Fill all fields",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener(this) { task ->

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                this,
                                HomeActivity::class.java
                            )
                        )

                        finish()

                    } else {

                        Toast.makeText(
                            this,
                            task.exception?.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Google login
        googleBtn.setOnClickListener {

            // Sign out first to force account chooser
            googleSignInClient.signOut().addOnCompleteListener {

                val signInIntent =
                    googleSignInClient.signInIntent

                googleSignInLauncher.launch(signInIntent)
            }
        }

        // Signup page
        signup.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SignUpActivity::class.java
                )
            )
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String
    ) {

        val credential =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    Toast.makeText(
                        this,
                        "Google Login Successful",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(
                            this,
                            HomeActivity::class.java
                        )
                    )

                    finish()

                } else {

                    Toast.makeText(
                        this,
                        "Google Authentication Failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}