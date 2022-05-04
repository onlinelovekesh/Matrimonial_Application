package com.example.firebasemessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.errorprone.annotations.Var
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference



        mDbRef.child("User").child(auth.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val loggedGender = it.child("gender").value.toString()
                val name = it.child("name").value.toString()
                Log.d(
                    "Logged gender: ${loggedGender},",
                    "####################################################################################1111"
                )

                splashActivityName.text = "Welcome $name!"

                var i = Intent(this, LoginActivity::class.java)
                i.putExtra("loggedGender", loggedGender)
                startActivity(i)
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Gender not found",Toast.LENGTH_LONG).show()
        }

    }
}