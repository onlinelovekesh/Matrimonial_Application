package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_layout.*
import kotlinx.android.synthetic.main.user_layout.view.*
import java.io.File

class UserAdapter(val context: Context, private val userList: ArrayList<User>): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_layout,parent,false)
        return UserViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

//################# show image, name, age gender in home page(available users) list view #################################################

        holder.textName.text = currentUser.name     //name in home page
        Picasso.get().load(currentUser.profileImageUri).into(holder.image)      // image in home page
        holder.age.text = "Age: ${currentUser.age} Yrs."
        holder.gender.text = "Gender: ${currentUser.gender}"

        holder.itemView.setOnClickListener {
            val i = Intent(context, ChatActivity::class.java)
            i.putExtra("name", currentUser.name)
            i.putExtra("uid", currentUser.uid)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val textName: TextView = itemView.findViewById(R.id.txt_name)
        var image: ImageView = itemView.findViewById(R.id.chat_imageView)
        var age: TextView = itemView.findViewById(R.id.txt_age)
        var gender: TextView = itemView.findViewById(R.id.txt_gender)

    }

}