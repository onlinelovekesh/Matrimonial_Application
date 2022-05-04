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
import com.google.firebase.firestore.FieldValue
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
        // custom actionbar back button
        chat_backButton.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
        }

        // custom actionbar profile image
        val profileImageRef =
            storageReference.child("Users").child(receiverUid!!).child("profileImage")
        val localFile = File.createTempFile("tempImage", ".jpg")
        Toast.makeText(this, "Loading image", Toast.LENGTH_SHORT).show()
        profileImageRef.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            chat_profileImage.setImageBitmap(bitmap)

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


            var chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
            var messageBox = findViewById<EditText>(R.id.messageBox)
            var sendButton = findViewById<ImageView>(R.id.sendButton)
            messageList = ArrayList()
            messageAdapter = MessageAdapter(this, messageList, senderUid!!, receiverUid)

            chatRecyclerView.layoutManager = LinearLayoutManager(this)
            chatRecyclerView.adapter = messageAdapter

            // adding data to recyclerView
            myDbRef.child("chats").child(senderRoom).child("messages")
                .addValueEventListener(object : ValueEventListener {

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {

                        messageList.clear()

                        for (postSnapshot in snapshot.children) {
                            val message = postSnapshot.getValue(Message::class.java)
                            messageList.add(message!!)


                        }
                        messageAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {  }

                })


            //adding message to database
            sendButton.setOnClickListener {
                val message = messageBox.text.toString()
                var messageObject = Message(message, senderUid,System.currentTimeMillis())

                if (message != "") {
                    //myDbRef.child("chats").child(senderUid!!).child(message).child().push()
                    myDbRef.child("chats").child(senderRoom!!).child("messages").push()
                        .setValue(messageObject).addOnSuccessListener {
                            myDbRef.child("chats").child(receiverRoom!!).child("messages").push()
                                .setValue(messageObject)
                        }
                    messageBox.setText("")
                }
            }
            chat_delete.setOnClickListener {

                var ad = AlertDialog.Builder(this)
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
    }
}

