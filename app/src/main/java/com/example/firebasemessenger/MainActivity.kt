package com.example.firebasemessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import androidx.appcompat.widget.SearchView.OnQueryTextListener

class MainActivity : AppCompatActivity() {

    //private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mDbRef: DatabaseReference
    lateinit var searchView: SearchView



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            intent.getStringExtra("Exit")

            var loggedGender = intent.getStringExtra("loggedGender").toString()
            supportActionBar?.hide()

            auth = FirebaseAuth.getInstance()
            mDbRef = FirebaseDatabase.getInstance().reference

            userList = ArrayList()
            adapter = UserAdapter(this, userList)

            userRecyclerView = findViewById(R.id.userRecyclerView)
            userRecyclerView.layoutManager = LinearLayoutManager(this)
            userRecyclerView.adapter = adapter

//#############################################################################################################################
            searchView = findViewById(R.id.searchView)
            //listView = findViewById(R.id.listView)

            //var searchAdapter : ArrayAdapter<String> = ArrayAdapter()

            //searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            /*    override fun onQueryTextSubmit(query: String?): Boolean {
                    if (userList.contains(query)){
                        searchAdapter.filter.filter(query)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    TODO("Not yet implemented")
                }

            } )*/
//############################################################################################################################
            fetch_userStatus.text = "Please wait, fetching users... "


// ###########  logged user's gender  #######################################################################################
           /* mDbRef.child("User").child(auth.currentUser?.uid!!).get().addOnSuccessListener {
                    if (it.exists()) {
                        loggedGender = it.child("gender").value.toString()
                        Log.d(
                            "Logged gender: ${loggedGender.toString()},",
                            "############################"
                        )
                    }
                }*/

            mDbRef.child("User").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    userList.clear()

                    for (postSnapshot in snapshot.children) {
                        val availableUsers = postSnapshot.getValue(User::class.java)

                        // To hide logged in user

                        if (auth.currentUser?.uid != availableUsers?.uid && loggedGender != availableUsers?.gender) {

                            Log.d(
                                "Logged gender: ${loggedGender.toString()},  ",
                                "User's gender: ${availableUsers?.gender.toString()}"
                            )

                            userList.add(availableUsers!!)

                            fetch_userStatus.text = ""

                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            main_backButton.setOnClickListener {
                val i = Intent(this, Chats::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)
            }

            favorite.setOnClickListener {
                Toast.makeText(this, loggedGender, Toast.LENGTH_LONG).show()
                //startActivity(Intent(this, Chats::class.java))
            }

            settingsButton.setOnClickListener {
                startActivity(Intent(this, UserProfile::class.java))
            }

        }

        //############################################# SORT LIST ##################################################################
        fun showPopup(v: View) {
            val popup = PopupMenu(this, v)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.sortNameAsc -> {
                        userList.sortBy {
                            it.name
                        }
                        adapter.notifyDataSetChanged()
                    }
                    R.id.sortNameDes -> {
                        userList.sortByDescending {
                            it.name
                        }
                        adapter.notifyDataSetChanged()
                    }
                    R.id.sortAgeAsc -> {
                        userList.sortBy {
                            it.age
                        }
                        adapter.notifyDataSetChanged()
                    }
                    R.id.sortAgeDes -> {
                        userList.sortByDescending {
                            it.age
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
                true
            }
            popup.show()
        }
    }