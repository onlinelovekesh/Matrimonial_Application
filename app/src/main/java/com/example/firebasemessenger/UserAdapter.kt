package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserAdapter(val context: Context, private val userList: ArrayList<User>):
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout,parent,false)
        return UserViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

//################# show image, name, age gender in home page(available users) list view ###################################

        Picasso.get().load(currentUser.profileImageUri).into(holder.image)      // image in home page
        holder.textName.text = currentUser.name     //name in home page
        holder.age.text = "Age: ${currentUser.age} Yrs."
        holder.gender.text = "Gender: ${currentUser.gender}"

        holder.itemView.setOnClickListener {
            val i = Intent(context, ChatActivity::class.java)
            i.putExtra("name", currentUser.name)
            i.putExtra("uid", currentUser.uid)
            context.startActivity(i)
        }

        holder.favorite.setOnClickListener {
                val ad = AlertDialog.Builder(context)
                ad.setTitle("Add to favorites")
                ad.setMessage("Click on YES to add ${currentUser.name} to favorites.")
                ad.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->

                    Toast.makeText(context, "${currentUser.name} added to favorites",Toast.LENGTH_SHORT).show()

                    addUserToDatabase(currentUser.name, currentUser.age, currentUser.mobile, currentUser.maritalStatus,
                        currentUser.gender, currentUser.email, currentUser.profileImageUri, currentUser.uid)

                })
                ad.setNegativeButton("No",null)
                ad.show()

            }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var image: ImageView = itemView.findViewById(R.id.chat_imageView)
        val textName: TextView = itemView.findViewById(R.id.txt_name)
        var age: TextView = itemView.findViewById(R.id.txt_age)
        var gender: TextView = itemView.findViewById(R.id.txt_gender)
        var favorite: ImageView = itemView.findViewById(R.id.favourite)

    }

    private fun addUserToDatabase(name: String?, age: String?, mobile: String?, maritalStatus: String?, gender: String?,
        email: String?, profileImageUri: String?, uid: String?) {

        val auth = FirebaseAuth.getInstance()

        val mDbRef = FirebaseDatabase.getInstance().reference
            .child("User").child(auth.currentUser?.uid!!).child("favorites").child(uid.toString())

        val userDetails = User(name.toString(),gender.toString(),age.toString(),maritalStatus.toString(),email.toString()
            ,mobile.toString(),uid.toString(),profileImageUri.toString())

        mDbRef.setValue(userDetails).addOnSuccessListener {
            Toast.makeText(context, "$name added to favorites", Toast.LENGTH_LONG).show()
        }

    }

}