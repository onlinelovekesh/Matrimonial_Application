package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        val loggedGender = intent.getStringExtra("loggedGender").toString()
        Toast.makeText(this, loggedGender, Toast.LENGTH_SHORT).show()

        auth = FirebaseAuth.getInstance()
        val user = Firebase.auth.currentUser
        if (user != null) {
            val i = Intent(this, MainActivity::class.java)
            i.putExtra("loggedGender", loggedGender)
            startActivity(i)
            finish()
            user.let {
                // Name, email address, and profile photo Url
                //val name = user.displayName
                val email = user.email

                Toast.makeText(this, "Welcome Back... $email", Toast.LENGTH_SHORT).show()

            }
        } else {
            log_btnLogin.setOnClickListener {
                var email = findViewById<TextView>(R.id.log_email).text.toString()
                var password = findViewById<TextView>(R.id.log_password).text.toString()

                when {
                    email.isEmpty() -> Toast.makeText(this,"Please enter your Email",Toast.LENGTH_SHORT).show()
                    password.isEmpty() -> Toast.makeText(this, "Please enter your Password", Toast.LENGTH_SHORT).show()
                    else -> signIn(email, password)
                }
            }

        }

        forgot_pwd.setOnClickListener {
            getPassword()
        }

        log_newUser.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getPassword() {
        var email = findViewById<TextView>(R.id.log_email).text.toString()
        if (email.isEmpty()){

            Toast.makeText(this, "Please enter email address", Toast.LENGTH_SHORT).show()

        }
        else {
            auth.sendPasswordResetEmail(log_email.text.toString()).addOnSuccessListener {
                emailSent_status.text = "An email with password reset link has been sent to your registered email successfully."
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                val loggedGender = intent.getStringExtra("Gender").toString()
                val i = Intent(this, MainActivity::class.java)
                i.putExtra("loggedGender",loggedGender)
                startActivity(i)

                finish()
            }
            else {
                Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show()
            }
        }

    }
}