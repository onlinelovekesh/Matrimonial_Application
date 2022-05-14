package com.example.firebasemessenger

class Message {
    var uniqueId: String? = null
    var message: String? = null
    var senderId: String? = null
    var timestamp: Long? = null

    constructor(){}

    constructor(uniqueId1: String?,message1: String?, senderId1: String?, timestamp1: Long){
        this.uniqueId = uniqueId1
        this.message = message1
        this.senderId = senderId1
        this.timestamp = timestamp1
    }
}