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

    constructor(name1: String, gender1:String, age1:String, maritalStatus1:String, email1:String, mobile1:String,
                uid1:String ,profileImageUri1:String){
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