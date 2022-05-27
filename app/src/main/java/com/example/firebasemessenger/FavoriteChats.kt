package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_favorite_chats.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class FavoriteChats : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: FavoriteUserAdapter
    private lateinit var mDbRef: DatabaseReference
    lateinit var mSearchText: EditText

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

        mSearchText = findViewById(R.id.fav_searchText)
        val fav_swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.fav_swipeRefreshLayout)
        fav_userStatus.text = "Add your favorite profiles\nfrom home page"

//############################################# SEARCH USER #################################################################

        mSearchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val searchText = mSearchText.text.toString()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

                if (searchText.isEmpty()){
                    getFavoriteUsers()
                }else{
                    searchUsers(searchText)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        fav_swipeRefresh.setOnRefreshListener {
            fav_swipeRefresh.isRefreshing = true

            getFavoriteUsers()

            adapter.notifyDataSetChanged()
            fav_swipeRefresh.isRefreshing = false
        }

        getFavoriteUsers()
//########################################################################################################################
        /*mDbRef.child("User").child(auth.currentUser!!.uid).child("favorites")
            .addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                addFavoriteUsers(snapshot)

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })*/

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

        fav_searchButton.setOnClickListener {
            fav_searchBarLayout.visibility = View.VISIBLE
        }
        fav_searchCancelButton.setOnClickListener {
            fav_searchText.text = null
            fav_searchBarLayout.visibility = View.GONE
        }

    }

    /*@SuppressLint("NotifyDataSetChanged")
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
    }*/

    private fun getFavoriteUsers() {
        mDbRef.child("User").child(auth.currentUser!!.uid).child("favorites")
            .addValueEventListener(object : ValueEventListener {

                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

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
                override fun onCancelled(error: DatabaseError) { }
            })
    }

//############################################# SEARCH USER #################################################################
    private fun searchUsers(searchText: String) {
        val searchQuery = mDbRef.child("User").child(auth.currentUser!!.uid).child("favorites")
            .orderByChild("name").startAt(searchText).endAt(searchText + "uf8ff")

        searchQuery.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

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

            override fun onCancelled(error: DatabaseError) { }
        })


    }

}
