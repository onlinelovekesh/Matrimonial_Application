package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class FavoriteUserAdapter(val context: Context, private  val userList: ArrayList<User>) :
    RecyclerView.Adapter<FavoriteUserAdapter.FavoriteUserViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteUserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.favorite_user_layout, parent, false)
        return FavoriteUserViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FavoriteUserViewHolder, position: Int) {
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

        holder.favRemove.setOnClickListener {
            var ad = AlertDialog.Builder(context)
            ad.setTitle("Remove from favorites")
            ad.setMessage("Click on YES to remove user from favorites.")
            ad.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->


                removeUserFromDatabase(currentUser.uid)

            })
            ad.setNegativeButton("No",null)
            ad.show()

        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class FavoriteUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var image: ImageView = itemView.findViewById(R.id.favorite_imageView)
        val textName: TextView = itemView.findViewById(R.id.favorite_name)
        var age: TextView = itemView.findViewById(R.id.favorite_age)
        var gender: TextView = itemView.findViewById(R.id.favorite_gender)
        var favRemove: ImageView = itemView.findViewById(R.id.favourite_remove)

    }


    private fun removeUserFromDatabase( uid: String?) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()

        val mDbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
            .child("User").child(auth.currentUser?.uid!!).child("favorites").child(uid.toString())

        mDbRef.removeValue().addOnSuccessListener {
            Toast.makeText(context, "User removed from favorites", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {

            Toast.makeText(context, "Something went wrong, Please try after sometime.", Toast.LENGTH_SHORT).show()
        }

    }

}