package com.example.firebasemessenger

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.ByteArrayOutputStream
import java.io.File

class UserProfile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        val selectOrUpdate = findViewById<Button>(R.id.profile_selectOrUpdatePhoto)

//########################### Getting user details ###################################################################

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

//############################# Buttons ##############################################################################
        selectOrUpdate.setOnClickListener {
            if (selectOrUpdate.text == "Update Profile Picture"){
                startFileChooser()
            }else{
                uploadPhoto()
            }
        }

        profile_btnDeleteAccount.setOnClickListener {
            val ad = AlertDialog.Builder(this)
            ad.setTitle("Delete Account")
            ad.setMessage("On clicking delete button, all data including chats will be deleted. It can not be recovered later.")
            ad.setPositiveButton("Delete", DialogInterface.OnClickListener { _, _ ->

                deleteUser()

            })
            ad.setNegativeButton("Cancel",null)
            ad.show()

        }

        profile_backButton.setOnClickListener {
            onBackPressed() // go back to previous page(main activity)
        }

        profile_logoutButton.setOnClickListener{
            val ad = AlertDialog.Builder(this)
            ad.setTitle("Log Out")
            ad.setMessage("Are you sure you want to Log Out?")
            ad.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->

                auth.signOut()
                val i = Intent(this,LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)

            })
            ad.setNegativeButton("No",null)
            ad.show()
        }

    }

    private fun startFileChooser(){
        if(CropImage.isExplicitCameraPermissionRequired(this)) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 0)
        }
        else {
            startActivityForResult(CropImage.getPickImageChooserIntent(this), CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imgUri = CropImage.getPickImageResultUri(this, data)
            CropImage.activity(imgUri)
                .setRequestedSize(2500, 2500)
                .start(this)
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
                imageUri = result.uri

                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
                profile_image.setImageBitmap(bitmap)
                profile_selectOrUpdatePhoto.text = "Save New Image"
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }else{
                val imageBitmap = data?.extras?.get("data") as Bitmap
                val baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val datar = baos.toByteArray()

                profile_image.setImageBitmap(imageBitmap)
                //storageRef!!.putBytes(datar)
            }
        }

    }

    private fun uploadPhoto() {
        if (imageUri!=null){
            val pd = ProgressDialog(this)
            pd.setTitle("Uploading image")
            pd.show()

            val imageRef = FirebaseStorage.getInstance().reference.child("Users").child(auth.currentUser?.uid!!)
                .child("profileImage")
            imageRef.putFile(imageUri!!).addOnSuccessListener {p0 ->
                    pd.dismiss()

                profile_selectOrUpdatePhoto.text = "Update Profile Picture"
                Toast.makeText(this, "Image updated successfully", Toast.LENGTH_SHORT).show()
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

    private fun deleteUser() {

        //########## delete user profile image/user data from firebase storage ###############################################
        storageReference.child("Users").child(auth.currentUser?.uid!!).child("profileImage").delete()
            .addOnSuccessListener {

                //############### delete user from realtime database #########################################################
                mDbRef.child("User").child(auth.currentUser?.uid!!).removeValue()

                //############### delete user from authentication and signOut ################################################
                auth.currentUser?.delete()
                auth.signOut()
                Toast.makeText(this, "User deleted Successfully", Toast.LENGTH_SHORT).show()

                //############### add flags ##################################################################################
                val i = Intent(this, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)

            }.addOnFailureListener {
                Toast.makeText(this, "Error occurred, Please try again after few minutes", Toast.LENGTH_SHORT).show()
            }
    }
}