package com.example.firebasemessenger

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.errorprone.annotations.Var
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_splash.*
import kotlin.system.exitProcess

class SplashActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        val user = Firebase.auth.currentUser

        if (user != null){
            //Toast.makeText(this,"Fetching Gender",Toast.LENGTH_LONG).show()
            mDbRef.child("User").child(auth.currentUser?.uid!!).get().addOnSuccessListener {

                val loggedGender = it.child("gender").value.toString()
                val name = it.child("name").value.toString()
                splashActivityName.text = "Welcome $name!"

                Handler(Looper.getMainLooper()).postDelayed({

                    val i = Intent(this, MainActivity::class.java)
                    i.putExtra("loggedGender", loggedGender)
                    startActivity(i)
                    finish()

                },1000)

            }.addOnFailureListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
        }


        /*mDbRef.child("User").child(auth.currentUser?.uid!!).get().addOnSuccessListener {
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
        }*/

    }

}
