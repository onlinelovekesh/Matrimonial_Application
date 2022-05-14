package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var myDbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference

    //var senderRoom: String? = null
    //var receiverRoom: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        myDbRef = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val senderUid = FirebaseAuth.getInstance().currentUser?.uid

        val senderRoom = receiverUid + senderUid
        val receiverRoom = senderUid + receiverUid

        // custom actionbar name
        chat_userName.text = name

//########################### display profile image from database storage ################################################

        val profileImageRef = storageReference.child("Users").child(receiverUid!!).child("profileImage")
        val localFile = File.createTempFile("tempImage", ".jpg")

        profileImageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            chat_profileImage.setImageBitmap(bitmap)
        }

        val chatRecyclerView = findViewById<RecyclerView>(R.id.chat_recyclerView)
        val messageBox = findViewById<EditText>(R.id.chat_messageBox)
        val sendButton = findViewById<ImageView>(R.id.chat_sendButton)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList, senderUid!!, receiverUid)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = messageAdapter

//######################## adding messages in recyclerView ################################################################

        myDbRef.child("chats").child(senderRoom).child("messages")
            .addChildEventListener(object : ChildEventListener{

                @SuppressLint("NotifyDataSetChanged")
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(Message::class.java)
                    if (message != null ){

                        messageList.add(message)

                    }
                    chatRecyclerView.scrollToPosition(messageAdapter.itemCount -1)
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val messageKey = snapshot.key
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }
                override fun onCancelled(error: DatabaseError) { }

            })

        /*myDbRef.child("chats").child(senderRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {

                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {

                    messageList.clear()

                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        messageList.add(message!!)
                        chatRecyclerView.smoothScrollToPosition(messageAdapter.itemCount -1)
                        //chatRecyclerView.smoothScrollToPosition(messageAdapter.itemCount -1)//chatRecyclerView.bottom)

                    }
                    //chatRecyclerView.smoothScrollToPosition(messageAdapter.itemCount -1)//chatRecyclerView.bottom)
                    messageAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {  }

            })*/

//########################### add new message to database ##############################################################

        sendButton.setOnClickListener {

            val messageText = messageBox.text.toString()

            if (messageText != "") {
                addNewMessageToDatabase(senderRoom, receiverRoom, messageText, senderUid, receiverUid, messageBox)
            }
        }

//############################ BUTTONS #################################################################################
        chat_backButton.setOnClickListener {
            onBackPressed()
        }

        chat_profileImage.setOnClickListener {
            val i = Intent(this, ReceiverProfileActivity::class.java)
            i.putExtra("uid",receiverUid)
            startActivity(i)
        }

        chat_userName.setOnClickListener {
            val i = Intent(this, ReceiverProfileActivity::class.java)
            i.putExtra("uid",receiverUid)
            startActivity(i)
        }

//########################### delete chat from database ################################################################

        chat_delete.setOnClickListener {

            val ad = AlertDialog.Builder(this)
            ad.setTitle("Delete")
            ad.setMessage("Do you want to delete all chat messages ?")
            ad.setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->

                myDbRef.child("chats").child(receiverUid + senderUid).child("messages")
                    .removeValue().addOnSuccessListener {
                        Toast.makeText(this, "Chat Deleted", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show()
                    }

            })
            ad.setNegativeButton("No",null)
            ad.show()

        }
    }

    private fun addNewMessageToDatabase(senderRoom: String, receiverRoom: String, messageText: String, senderUid: String,
        receiverUid: String, messageBox: EditText) {

        val senderMessage = myDbRef.child("chats").child(senderRoom).child("messages").push()
        val senderMessageObject = Message(senderMessage.key,messageText, senderUid,System.currentTimeMillis())

        val receiverMessage = myDbRef.child("chats").child(receiverRoom).child("messages").push()
        val receiverMessageObject = Message(receiverMessage.key,messageText, receiverUid,System.currentTimeMillis())

        //myDbRef.child("chats").child(senderRoom).child("messages").push()
        senderMessage.setValue(senderMessageObject).addOnSuccessListener {
            receiverMessage.setValue(receiverMessageObject)
        }
        messageBox.setText("")
    }
}

