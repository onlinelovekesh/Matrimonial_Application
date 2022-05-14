package com.example.firebasemessenger

class User {
    var name: String? = null
    var gender: String? = null
    var age: String? = null
    var maritalStatus: String? = null
    var email: String? = null
    var mobile: String? = null
    var uid:String? = null
    var profileImageUri: String? = null

    constructor(){}

    constructor(
        name1: String, gender1:String, age1:String, maritalStatus1:String, email1:String, mobile1:String,
        uid1:String,
        profileImageUri1:String
    ){
        this.name = name1
        this.gender = gender1
        this.age = age1
        this.maritalStatus = maritalStatus1
        this.email = email1
        this.mobile = mobile1
        this.uid = uid1
        this.profileImageUri = profileImageUri1
    }
}

/*

package com.example.firebasemessenger

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
    private lateinit var adapter: UserAdapter
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_chats)

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
        mDbRef.child("User").child(auth.currentUser!!.uid).child("favorites").addValueEventListener(object: ValueEventListener {
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


 */

/*

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".FavoriteChats"
    android:background="@color/purple_500">

    <LinearLayout
        android:id="@+id/old_actionBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center"
        android:weightSum="10"
        android:layout_marginVertical="10dp">

        <ImageButton
            android:id="@+id/actionBar_settingsIcon"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="0.5"
            android:src="@drawable/back_icon"
            android:background="@android:color/transparent" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="10dp"
            android:layout_weight="9"
            android:text="Favourites"
            android:textColor="@color/white"
            android:textSize="25sp"
            android:gravity="start"/>

        <ImageButton
            android:id="@+id/old_logoutButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_weight="0.5"
            android:src="@drawable/logout_icon"
            android:background="@android:color/transparent"
            android:visibility="invisible"/>

    </LinearLayout>

    <RelativeLayout
        android:layout_below="@+id/old_actionBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content">

        <TextView
            android:id="@+id/old_fetch_userStatus"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:text="Please wait\nfetching users..."
            android:textColor="@color/white"
            android:textSize="20sp" />

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/old_userRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            tools:listitem="@layout/user_layout" />

    </RelativeLayout>

    <androidx.coordinatorlayout.widget.CoordinatorLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.google.android.material.bottomappbar.BottomAppBar
            android:id="@+id/old_bottomAppBar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom"
            app:backgroundTint="@color/white"
            app:contentInsetLeft="0dp"
            app:contentInsetStart="0dp"
            app:fabAlignmentMode="center"
            app:fabCradleRoundedCornerRadius="16dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/old_bt1"
                    style="?android:attr/borderlessButtonStyle"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:background="@drawable/list_item_shape"
                    android:gravity="center"
                    android:orientation="vertical"
                    android:text="New chat"
                    android:textAllCaps="false"
                    android:textColor="@color/purple_500"
                    app:drawableTopCompat="@drawable/menu_icon" />

                <TextView
                    style="?android:attr/borderlessButtonStyle"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:background="@color/white"
                    android:gravity="center"
                    android:orientation="vertical"
                    android:textAllCaps="false"
                    android:textColor="@color/black"
                    android:visibility="invisible" />

                <TextView
                  android:id="@+id/old_bt3"
                  style="?android:attr/borderlessButtonStyle"
                  android:layout_width="0dp"
                  android:layout_height="wrap_content"
                  android:layout_weight="1"
                  android:background="@drawable/list_item_shape"
                  android:gravity="center"
                  android:orientation="vertical"
                  android:text="Search"
                  android:textAllCaps="false"
                  android:textColor="@color/purple_500"
                  app:drawableTopCompat="@drawable/search_icon" />

                <TextView
                  android:id="@+id/old_settingsButton"
                  style="?android:attr/borderlessButtonStyle"
                  android:layout_width="0dp"
                  android:layout_height="wrap_content"
                  android:layout_weight="1"
                  android:background="@drawable/list_item_shape"
                  android:orientation="vertical"
                  android:text="Settings"
                  android:textAllCaps="false"
                  android:textColor="@color/purple_500"
                  app:drawableTopCompat="@drawable/settings_icon" />

            </LinearLayout>

        </com.google.android.material.bottomappbar.BottomAppBar>

        <com.google.android.material.floatingactionbutton.FloatingActionButton
              android:id="@+id/fab"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:src="@drawable/add_new_user"
              app:backgroundTint="@color/white"
              app:layout_anchor="@id/bottomAppBar"
              app:tint="@color/purple_500" />

    </androidx.coordinatorlayout.widget.CoordinatorLayout>

</RelativeLayout>




*/