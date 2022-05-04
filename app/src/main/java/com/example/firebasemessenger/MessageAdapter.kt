package com.example.firebasemessenger

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>, val senderUid: String, val receiverUid: String):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_RECEIVED = 1
    val ITEM_SENT = 2

    private lateinit var myDbRef: DatabaseReference
    var senderRoom: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
            //inflate item received
            val view: View = LayoutInflater.from(context).inflate(R.layout.received_message,parent,false)
            return ReceivedViewHolder(view)
        }else{
            //inflate item sent
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent_message,parent,false)
            return SentViewHolder(view)
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        myDbRef = FirebaseDatabase.getInstance().reference

        val currentMessage = messageList[position]

        if (holder.javaClass == SentViewHolder::class.java){

            //for sent message view holder
            val viewHolder = holder as SentViewHolder
            holder.sentMessage.text = currentMessage.message

//######################### DELETE MESSAGE ################################################################
            holder.sentMessage.setOnLongClickListener{

                var ad = AlertDialog.Builder(context)
                ad.setTitle("Delete")
                ad.setMessage("Do you want to delete all chat messages ? $position")

                ad.setPositiveButton("Yes",DialogInterface.OnClickListener { _, _ ->

                    myDbRef.child("chats").child(receiverUid + senderUid).child("messages").child("message")
                        .removeValue().addOnSuccessListener {
                                Toast.makeText(context, "Chat Deleted", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(context, "Try Again", Toast.LENGTH_SHORT).show()
                            }

                })
                ad.setNegativeButton("No",null)
                ad.show()

                true
            }

//######################### FETCH DATE ####################################################################
            val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm a")  //("dd/MMMM/yyyy hh:mm a")
            val netDate = Date(currentMessage.timestamp!!)
            val date =sdf.format(netDate)
            holder.sent_timestamp.text = date

        }
        else{
            //for received message view holder
            val viewHolder = holder as ReceivedViewHolder
            holder.receivedMessage.text = currentMessage.message

            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a")  //("dd/MMMM/yyyy hh:mm a")
            val netDate = Date(currentMessage.timestamp!!)
            val date =sdf.format(netDate)
            holder.received_timestamp.text = date                   //fetch date & time from database
        }

    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }else{
            return ITEM_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val sentMessage = itemView.findViewById<TextView>(R.id.sent_message)
        var sent_timestamp = itemView.findViewById<TextView>(R.id.sent_messageTime)


    }
    class ReceivedViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val receivedMessage = itemView.findViewById<TextView>(R.id.received_message)
        val received_timestamp = itemView.findViewById<TextView>(R.id.received_messageTime)

    }
}