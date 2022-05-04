package com.example.firebasemessenger

class Message {
    var message: String? = null
    var senderId: String? = null
    var timestamp: Long? = null

    constructor(){}

    constructor(message1: String?, senderId1: String?, timestamp1: Long){
        this.message = message1
        this.senderId = senderId1
        this.timestamp = timestamp1
    }
}