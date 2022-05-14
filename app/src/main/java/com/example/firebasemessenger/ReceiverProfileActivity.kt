package com.example.firebasemessenger

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_receiver_profile.*
import java.io.File

class ReceiverProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receiver_profile)

        supportActionBar?.hide()

        val receiverUid = intent.getStringExtra("uid")

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        mDbRef.child("User").child(receiverUid!!).get()
            .addOnSuccessListener {
                if (it.exists()){
                    val name = it.child("name").value.toString()
                    val email = it.child("email").value.toString()

                    receiver_name.text = name
                    receiver_s_name.text = "$name's Profile"
                    receiver_email.text = email

                }
            }

        val imageRef = storageReference.child("Users").child(receiverUid).child("profileImage")
        val localFile = File.createTempFile("tempImage",".jpg")
        Toast.makeText(this,"Loading image", Toast.LENGTH_SHORT).show()
        imageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            receiver_profileImage.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Toast.makeText(this,"Unable to retrieve image", Toast.LENGTH_SHORT).show()
        }
    }

}