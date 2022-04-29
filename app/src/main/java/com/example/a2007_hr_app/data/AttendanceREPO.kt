/**
 * API Methods for Read and Write of data to Firebase Realtime Database
 * Author: Wong Jun Hao
 * References: https://firebase.google.com/docs/database/android/
 */
package com.example.a2007_hr_app.data

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

val TAG: String = "AttendanceREPO"
class AttendanceREPO {

    private var database: FirebaseDatabase = Firebase.database(
        "https://mad-hr-default-rtdb.asia-southeast1.firebasedatabase.app/")

    private var user = Firebase.auth.currentUser
    private var userID: String = ""

    init {
        user?.let {
            userID = user!!.uid
            Log.d(TAG,"UserID: $userID")
        }
    }

    /**
     * Methods
     */

    /**
     * Write Attendance Data to database
     * @param: attendanceData: AttendanceData
     */
    fun writeAttendance(attendanceData: AttendanceData){
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = "${sdf.format(cal.time)}"

        val ref = database.getReference("Users/$userID/Attendance/$date/")
        ref.setValue(attendanceData)
    }

    /**
     * Read Attendance History from database
     */
    fun readAttendance(){
        val ref = database.getReference("Users/$userID/Attendance/")
        ref.get().addOnSuccessListener {
            Log.d(TAG, "$it")
        }.addOnFailureListener {
            Log.d(TAG,"Failed to Get Attendance History")
        }

    }

}