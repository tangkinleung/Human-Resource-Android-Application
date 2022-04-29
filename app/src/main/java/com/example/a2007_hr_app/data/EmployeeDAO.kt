package com.example.a2007_hr_app.data

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


/**
 * Data Access Object to communicate with Firebase database
 */
class EmployeeDAO {

    val database: FirebaseDatabase = Firebase.database(
        "https://mad-hr-default-rtdb.asia-southeast1.firebasedatabase.app/")

    /**
     * Add a new employee to the database
     * @param name, email and password
     */
    fun setEmployeeLogin(username: String, password: String) {

        var ref = database.getReference("Users/${username}/UserInfo/Email")
        ref.setValue(username)

        ref = database.getReference("Users/${username}/UserInfo/Password")
        ref.setValue(password)
    }
}