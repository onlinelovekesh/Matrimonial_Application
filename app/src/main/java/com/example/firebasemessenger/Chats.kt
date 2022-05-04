package com.example.firebasemessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_chats.*
import kotlinx.android.synthetic.main.activity_main.*

class Chats : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        mDbRef = FirebaseDatabase.getInstance().reference
        var senderUid = FirebaseAuth.getInstance().currentUser?.uid

        userList = ArrayList()
        adapter = UserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.old_userRecyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        old_fetch_userStatus.text = "Please wait, fetching users... "
        mDbRef.child("User").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                for(postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)

                    // To hide username of current user
                    if (auth.currentUser?.uid != currentUser?.uid){
                        if (mDbRef.child("chats").child("$senderUid"+"") != null){

                            userList.add(currentUser!!)

                            old_fetch_userStatus.text = ""

                        }
                        else{
                            old_fetch_userStatus.text = "No chats available"
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })


    }
}