package com.example.firebasemessenger

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.File

class UserProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var storageReference: StorageReference

    private lateinit var imageUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        val selectOrUpdate = findViewById<Button>(R.id.selectOrUpdatePhoto)

        mDbRef.child("User").child(auth.currentUser?.uid!!).get()
            .addOnSuccessListener {
                if (it.exists()){
                    val name = it.child("name").value
                    val email = it.child("email").value
                    val gender = it.child("gender").value
                    val age = it.child("age").value
                    val mobile = it.child("mobile").value
                    val maritalStatus = it.child("maritalStatus").value

                    profile_name.text = name.toString()
                    profile_email.text = email.toString()
                    profile_gender.text = gender.toString()
                    profile_age.text = age.toString()
                    profile_mobile.text = mobile.toString()
                    profile_maritalStatus.text = maritalStatus.toString()

                }
        }

        val imageRef = storageReference.child("Users").child(auth.currentUser?.uid!!).child("profileImage")
        val localFile = File.createTempFile("tempImage",".jpg")
        Toast.makeText(this,"Loading image",Toast.LENGTH_SHORT).show()
        imageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            profile_image.setImageBitmap(bitmap)


        }.addOnFailureListener {
            Toast.makeText(this,"Unable to retrieve image",Toast.LENGTH_SHORT).show()
        }

        selectOrUpdate.setOnClickListener {
            if (selectOrUpdate.text == "Update Profile Picture"){
                startFileChooser()
            }else{
                uploadPhoto()

            }
        }

        btn_deleteAccount.setOnClickListener {
            var ad = AlertDialog.Builder(this)
            ad.setTitle("Delete Account")
            ad.setMessage("On clicking delete button, all data will be deleted.")
            ad.setPositiveButton("Delete", DialogInterface.OnClickListener { _, _ ->

                deleteUser()

            })
            ad.setNegativeButton("Cancel",null)
            ad.show()

        }

        user_backButton.setOnClickListener {
            val i = Intent(this,MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }

        logoutButton.setOnClickListener{
            auth.signOut()
            val i = Intent(this,LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }

    }

    private fun startFileChooser() {
        val i = Intent()
        i.type = "image/*" + auth.currentUser?.uid
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(i, "Choose Picture"), 111)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK && data != null){
            imageUri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
            profile_image.setImageBitmap(bitmap)
            selectOrUpdatePhoto.text = "Save New Image"
        }
    }

    private fun uploadPhoto() {

        if (imageUri!=null){
            val pd = ProgressDialog(this)
            pd.setTitle("Uploading image")
            pd.show()

            //var fileName = UUID.randomUUID().toString() //optional
            val imageRef = FirebaseStorage.getInstance().reference
                .child("Users").child(auth.currentUser?.uid!!).child("profileImage")//.child(fileName)
            imageRef.putFile(imageUri)
                .addOnSuccessListener {p0 ->
                    pd.dismiss()
                    selectOrUpdatePhoto.text = "Update Profile Picture"
                    Toast.makeText(this, "Image updated successfully", Toast.LENGTH_LONG).show()

                    //getting path of the image after inserting
                    /*imageRef.downloadUrl.addOnSuccessListener {
                        //addUserToDatabase(it.toString())
                        // save updated user details in database storage also
                        mDbRef = FirebaseDatabase.getInstance().reference.child("User").child(auth.currentUser?.uid!!)
                        val userDetails = User(profile_name.text.toString(),gender_menu.text.toString(),age_menu.text.toString(),
                            marital_status_menu.text.toString(),profile_email.text.toString(),reg_mobile.text.toString(),
                            auth.currentUser?.uid!!,it.toString())
                        mDbRef.setValue(userDetails)

                    }*/

                }
                .addOnFailureListener{p0 ->
                    pd.dismiss()
                    Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener {p0 ->
                    val progress = (100.0 * p0.bytesTransferred)/p0.totalByteCount
                    pd.setMessage("uploaded ${progress.toInt()}%")

                }
        }
    }

    //private fun addUserToDatabase(profileImageUri: String) {
    //}
/*
    var user = Firebase.auth.currentUser
    auth = FirebaseAuth.getInstance()
    mDbRef = FirebaseDatabase.getInstance().reference
    storageReference = FirebaseStorage.getInstance().reference
*/
    private fun deleteUser() {

        //########## delete user profile image/user data from firebase storage ###############################################
        storageReference.child("Users").child(auth.currentUser?.uid!!).child("profileImage").delete()
            .addOnSuccessListener {

                //############### delete user from realtime database #########################################################
                mDbRef.child("User").child(auth.currentUser?.uid!!).removeValue()

                //############### delete user from authentication and signOut ################################################
                auth.currentUser?.delete()
                auth.signOut()

                //############### add flags ##################################################################################
                val i = Intent(this, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)

                Toast.makeText(this, "User deleted Successfully", Toast.LENGTH_SHORT).show()

            }.addOnFailureListener {
                Toast.makeText(this, "Error occurred, Please try again after few minutes", Toast.LENGTH_SHORT).show()
            }
    }

}