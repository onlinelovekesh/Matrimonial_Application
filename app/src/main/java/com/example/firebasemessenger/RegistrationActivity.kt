package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.regex.Pattern

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var imageUri: Uri

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        supportActionBar?.hide()

        val gender = resources.getStringArray(R.array.gender)
        val arrayAdapterGender = ArrayAdapter(this, R.layout.dropdown_menu, gender)
        val genderMenu = findViewById<AutoCompleteTextView>(R.id.gender_menu)
        genderMenu.setAdapter(arrayAdapterGender)

        val age = resources.getStringArray(R.array.age)
        val arrayAdapterAge = ArrayAdapter(this, R.layout.dropdown_menu, age)
        val ageMenu = findViewById<AutoCompleteTextView>(R.id.age_menu)
        ageMenu.setAdapter(arrayAdapterAge)

        val maritalStatus = resources.getStringArray(R.array.maritalStatus)
        val arrayAdapterMaritalStatus = ArrayAdapter(this, R.layout.dropdown_menu, maritalStatus)
        val maritalStatusMenu = findViewById<AutoCompleteTextView>(R.id.marital_status_menu)
        maritalStatusMenu.setAdapter(arrayAdapterMaritalStatus)

        //initialize the FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        reg_mobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (isValidPhoneNumber(reg_mobile.text.toString())){
                    reg_mobile.error = null
                }
                else {
                    reg_mobile.error = "Invalid"
                }
            }

        })

        reg_btnRegistration.setOnClickListener {
            val name = findViewById<TextView>(R.id.reg_name).text.toString()
            val selectedGender = genderMenu.text
            val selectedAge = ageMenu.text
            val selectedMaritalStatus = maritalStatusMenu.text
            val email = findViewById<TextView>(R.id.reg_email).text.toString()
            val mobile = findViewById<TextView>(R.id.reg_mobile).text.toString()
            val password = findViewById<TextView>(R.id.reg_password).text.toString()
            val cnfPassword = findViewById<TextView>(R.id.reg_cnf_password).text.toString()

            when {
                name.isEmpty() -> Toast.makeText(this, "Please enter your name", Toast.LENGTH_LONG).show()
                selectedGender.isEmpty() -> Toast.makeText(this, "Please select gender", Toast.LENGTH_LONG).show()
                selectedAge.isEmpty() -> Toast.makeText(this, "Please select age", Toast.LENGTH_LONG).show()
                selectedMaritalStatus.isEmpty() -> Toast.makeText(this, "Please select your marriage status", Toast.LENGTH_LONG).show()
                email.isEmpty() -> Toast.makeText(this, "Please enter your email", Toast.LENGTH_LONG).show()
                mobile.isEmpty() -> Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(this, "Please enter your password", Toast.LENGTH_LONG).show()
                cnfPassword.isEmpty() -> Toast.makeText(this, "Confirm password field is empty", Toast.LENGTH_LONG).show()
                password != cnfPassword -> Toast.makeText(this, "Password and confirm password are not same", Toast.LENGTH_LONG).show()

                else -> signUp(email, password)
            }
        }



        reg_alreadyRegistered.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        reg_addImage.setOnClickListener {
            startFileChooser()
        }

    }

    private fun signUp(email: String, password:String){

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            if (it.isSuccessful){

                    uploadPhoto()

            }
        }
            .addOnFailureListener {
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadPhoto() {

        if (imageUri!=null){
            val pd = ProgressDialog(this)
            pd.setTitle("Uploading image")
            pd.show()

            //val fileName = UUID.randomUUID().toString() //optional
            val imageRef = FirebaseStorage.getInstance().reference
                .child("Users").child(auth.currentUser?.uid!!).child("profileImage")//.child(fileName)
            imageRef.putFile(imageUri)
                .addOnSuccessListener {p0 ->
                    pd.dismiss()
                    Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_LONG).show()

                    //getting path of the image after inserting
                    imageRef.downloadUrl.addOnSuccessListener {
                        addUserToDatabase(it.toString())
                    }

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
        else{
            Toast.makeText(this,"Please add profile image",Toast.LENGTH_SHORT).show()
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
            reg_profileImage.setImageBitmap(bitmap)
        }
    }

    private fun addUserToDatabase(profileImageUri: String) {
        mDbRef = FirebaseDatabase.getInstance().reference.child("User").child(auth.currentUser?.uid!!)
        val userDetails = User(reg_name.text.toString(),gender_menu.text.toString(),age_menu.text.toString(),
        marital_status_menu.text.toString(),reg_email.text.toString(),reg_mobile.text.toString(),auth.currentUser?.uid!!,profileImageUri)
        mDbRef.setValue(userDetails).addOnSuccessListener {

            Toast.makeText(this, "Registration successful", Toast.LENGTH_LONG).show()
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }

    }

    /*fun CharSequence?.isValidPhoneNumber(text: String):Boolean{
        return !isNullOrEmpty() && Patterns.PHONE.matcher(text).matches()
    }*/

    fun isValidPhoneNumber(text: String?):Boolean{
        var p = Pattern.compile("[6-9] [0-9] {9}")
        var m = p.matcher(text)
        return m.matches()
    }
}
