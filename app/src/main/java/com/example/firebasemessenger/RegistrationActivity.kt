package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.widget.*
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.regex.Pattern

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private var imageUri: Uri? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        supportActionBar?.hide()

        val gender = resources.getStringArray(R.array.gender)
        val arrayAdapterGender = ArrayAdapter(this, R.layout.dropdown_menu, gender)
        val genderMenu = findViewById<AutoCompleteTextView>(R.id.reg_genderMenu)
        genderMenu.setAdapter(arrayAdapterGender)

        val age = resources.getStringArray(R.array.age)
        val arrayAdapterAge = ArrayAdapter(this, R.layout.dropdown_menu, age)
        val ageMenu = findViewById<AutoCompleteTextView>(R.id.reg_ageMenu)
        ageMenu.setAdapter(arrayAdapterAge)

        val maritalStatus = resources.getStringArray(R.array.maritalStatus)
        val arrayAdapterMaritalStatus = ArrayAdapter(this, R.layout.dropdown_menu, maritalStatus)
        val maritalStatusMenu = findViewById<AutoCompleteTextView>(R.id.reg_maritalStatusMenu)
        maritalStatusMenu.setAdapter(arrayAdapterMaritalStatus)

        auth = FirebaseAuth.getInstance()

        reg_mobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (isValidPhoneNumber(reg_mobile.text.toString())){
                    reg_mobile.error = null
                }
                else reg_mobile.error = "Invalid Mobile"
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        reg_email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (isValidEmail(reg_email.text.toString())){
                    reg_email.error = null
                }
                else reg_email.error = "Invalid Email"
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        reg_btnRegistration.setOnClickListener {
            val name = findViewById<TextView>(R.id.reg_name).text.toString()
            val selectedGender = genderMenu.text
            val selectedAge = ageMenu.text
            val selectedMaritalStatus = maritalStatusMenu.text
            val email = findViewById<TextView>(R.id.reg_email).text.toString()
            val mobile = findViewById<TextView>(R.id.reg_mobile).text.toString()
            val password = findViewById<TextView>(R.id.reg_password).text.toString()
            val cnfPassword = findViewById<TextView>(R.id.reg_confirmPassword).text.toString()

            when {
                name.isEmpty() -> Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                selectedGender.isEmpty() -> Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
                selectedAge.isEmpty() -> Toast.makeText(this, "Please select age", Toast.LENGTH_SHORT).show()
                selectedMaritalStatus.isEmpty() -> Toast.makeText(this, "Please select your marriage status", Toast.LENGTH_SHORT).show()
                email.isEmpty() -> Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                mobile.isEmpty() -> Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
                cnfPassword.isEmpty() -> Toast.makeText(this, "Confirm password field is empty", Toast.LENGTH_SHORT).show()
                password != cnfPassword -> Toast.makeText(this, "Password and confirm password are not same", Toast.LENGTH_SHORT).show()

                else -> signUp(email, password)
            }
        }

        reg_loginHereLink.setOnClickListener {
            reg_loginHereLink.setTextColor(Color.RED)
            startActivity(Intent(this, LoginActivity::class.java))
        }

        reg_addImage.setOnClickListener {
            startFileChooser()
        }

    }

    private fun signUp(email: String, password:String){
        if (imageUri != null){
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                if (it.isSuccessful){
                    uploadPhoto()
                }
            }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }else{
            Toast.makeText(this, "Please add profile image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPhoto() {
        val pd = ProgressDialog(this)
        pd.setTitle("Uploading image")
        pd.show()

        val imageRef = FirebaseStorage.getInstance().reference.child("Users")
            .child(auth.currentUser?.uid!!).child("profileImage")
        imageRef.putFile(imageUri!!)
            .addOnSuccessListener {_ ->
                pd.dismiss()
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_LONG).show()

                //getting path of the image after inserting
                imageRef.downloadUrl.addOnSuccessListener {
                    addUserToDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                pd.dismiss()
                Toast.makeText(this, "Please try again", Toast.LENGTH_LONG).show()
            }
            .addOnProgressListener {p0 ->
                val progress = (100.0 * p0.bytesTransferred)/p0.totalByteCount
                pd.setMessage("uploaded ${progress.toInt()}%")
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
                reg_profileImage.setImageBitmap(bitmap)
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        }
    }

    /*private fun startFileChooser() {
        val i = Intent()
        i.type = "image/*" + auth.currentUser?.uid
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(i, "Choose Picture"), 111)

    }*/*/

    /*@Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == Activity.RESULT_OK && data != null){
            imageUri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
            reg_profileImage.setImageBitmap(bitmap)

            reg_addImage.text = "Change Image"
        }
    }*/

    private fun addUserToDatabase(profileImageUri: String) {
        mDbRef = FirebaseDatabase.getInstance().reference.child("User").child(auth.currentUser?.uid!!)
        val userDetails = User(reg_name.text.toString(),reg_genderMenu.text.toString(),reg_ageMenu.text.toString(),
        reg_maritalStatusMenu.text.toString(),reg_email.text.toString(),reg_mobile.text.toString(),auth.currentUser?.uid!!,
            profileImageUri)
        mDbRef.setValue(userDetails).addOnSuccessListener {

            Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()
            val i = Intent(this, SplashActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }

    }

    private fun isValidPhoneNumber(text : String?):Boolean{
        val p = Pattern.compile("[6-9][0-9]{9}")
        val m = p.matcher(text!!)
        return m.matches()
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
