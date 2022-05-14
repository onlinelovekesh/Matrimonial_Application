package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite_chats.*

class FavoriteChats : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: FavoriteUserAdapter
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_chats)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference

        userList = ArrayList()
        adapter = FavoriteUserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.fav_recyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        fav_userStatus.text = getString(R.string.wait)

        mDbRef.child("User").child(auth.currentUser!!.uid).child("favorites")
            .addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                addFavoriteUsers(snapshot)

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        /*fav_backBtn.setOnClickListener {
            onBackPressed()
        }*/

        fav_homeBtn.setOnClickListener {
            finish()
            mDbRef.child("User").child(auth.currentUser?.uid!!).get().addOnSuccessListener{
                    if (it.exists()){
                        val loggedGender = it.child("gender").value.toString()

                        var i = Intent(this, MainActivity::class.java)
                            i.putExtra("loggedGender", loggedGender)
                            startActivity(i)
                            finish()
                    }
                }.addOnFailureListener {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
        }

        fav_favoriteBtn.setOnClickListener {
            finish()
            startActivity(intent)
        }

        fav_profileBtn.setOnClickListener {
            startActivity(Intent(this, UserProfile::class.java))
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addFavoriteUsers(snapshot: DataSnapshot) {
        userList.clear()

        for(postSnapshot in snapshot.children){
            val currentUser = postSnapshot.getValue(User::class.java)

            //########### Hide current user ################################
            if (auth.currentUser?.uid != currentUser?.uid){

                userList.add(currentUser!!)

                fav_userStatus.text = ""
            }
        }
        adapter.notifyDataSetChanged()
    }


}
