package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var mDbRef: DatabaseReference
    //lateinit var FirebaseRecyclerAdapter : FirebaseRecyclerAdapter<User, UsersViewHolder>
    lateinit var mSearchText: EditText

        @SuppressLint("NotifyDataSetChanged")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            fetch_userStatus.text = getString(R.string.wait)

            val loggedGender = intent.getStringExtra("loggedGender").toString()
            supportActionBar?.hide()

            auth = FirebaseAuth.getInstance()
            mDbRef = FirebaseDatabase.getInstance().reference

            userList = ArrayList()
            adapter = UserAdapter(this, userList)

            userRecyclerView = findViewById(R.id.userRecyclerView)
            userRecyclerView.layoutManager = LinearLayoutManager(this)
            userRecyclerView.adapter = adapter

            mSearchText = findViewById<EditText?>(R.id.searchText)

            val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

//################################### search user ###########################################################################

            /*mSearchText.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    val searchText = mSearchText.text.toString()
                    //loadFirebaseData(searchText)

                    mDbRef.child("User").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            userList.clear()

                            for (postSnapshot in snapshot.children) {
                                val availableUsers = postSnapshot.getValue(User::class.java)

                                //############## hide current user from list ############################################################

                                if (auth.currentUser?.uid != availableUsers?.uid &&
                                    loggedGender != availableUsers?.gender &&
                                    searchText == availableUsers?.name) {

                                        Log.d(searchText,
                                            "#########################################################")

                                    userList.add(availableUsers!!)

                                    fetch_userStatus.text = ""
                                }
                            }
                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })


                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })*/

//################################### Show users in "available users" page ###################################################

            swipeRefresh.setOnRefreshListener {
                swipeRefresh.isRefreshing = true

                getAvailableUsers(loggedGender)

                adapter.notifyDataSetChanged()
                swipeRefresh.isRefreshing = false
            }

            getAvailableUsers(loggedGender)

//################################### Bottom navigation bar ##################################################################

            homeBtn.setOnClickListener {

                finish()
                startActivity(intent)

            /*mDbRef.child("User").child(auth.currentUser?.uid!!).get().addOnSuccessListener{
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
                }*/
            }

            favoriteBtn.setOnClickListener {
                finishAfterTransition()
                startActivity(Intent(this, FavoriteChats::class.java))
            }

            profileBtn.setOnClickListener {
                startActivity(Intent(this, UserProfile::class.java))
            }

            main_searchButton.setOnClickListener {
                searchBarLayout.visibility = View.VISIBLE
                main_searchButton.setBackgroundResource(R.drawable.ic_cancel_icon)
            }

        }

    private fun getAvailableUsers(loggedGender: String) {
        mDbRef.child("User").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                for (postSnapshot in snapshot.children) {
                    val availableUsers = postSnapshot.getValue(User::class.java)

//################################### hide current user from list ############################################################

                    if (auth.currentUser?.uid != availableUsers?.uid && loggedGender != availableUsers?.gender) {

                        userList.add(availableUsers!!)

                        fetch_userStatus.text = ""
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

//############################################# SORT LIST ###################################################################

    @SuppressLint("NotifyDataSetChanged")
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

//#############################################  ###################################################################

    /*private fun loadFirebaseData(searchText: String) {

        if (searchText.isEmpty()){

            FirebaseRecyclerAdapter.cleanup()
            userRecyclerView.adapter = FirebaseRecyclerAdapter

        }else{

            FirebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<User , UsersViewHolder>(
                User::class.java,
                R.layout.user_layout,
                UsersViewHolder::class.java,
                mDbRef
            ){
                override fun populateViewHolder(viewHolder: UsersViewHolder, userModel: User?, position: Int) {
                    viewHolder.mView.txt_name.text = userModel?.name
                    viewHolder.mView.txt_age.text = userModel?.age
                    viewHolder.mView.txt_gender.text = userModel?.gender
                    Picasso.get().load(userModel?.profileImageUri).into(viewHolder.mView.chat_imageView)

                }

            }

            userRecyclerView.adapter = FirebaseRecyclerAdapter
        }

    }

    class UsersViewHolder(var mView : View) : RecyclerView.ViewHolder(mView){

    }*/

}