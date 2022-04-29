/**
 * For the display of attendance history
 * Author: wong Jun Hao
 */
package com.example.a2007_hr_app.ui.attendance

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a2007_hr_app.data.AttendanceData
import com.example.a2007_hr_app.databinding.ActivityAttendanceBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


val TAG = "AttendanceActivity"

class AttendanceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceBinding
    private lateinit var dataArrayList: ArrayList<AttendanceData>
    private lateinit var attendanceAdapter: AttendanceAdapter

    private var database: FirebaseDatabase = Firebase.database(
        "https://mad-hr-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    private var user = Firebase.auth.currentUser
    private var userID: String = ""

    init {
        user?.let {
            userID = user!!.uid
            Log.d(com.example.a2007_hr_app.data.TAG, "UserID: $userID")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        binding.RecyclerViewAttendance.layoutManager = linearLayoutManager
        binding.RecyclerViewAttendance.setHasFixedSize(true)


        dataArrayList = arrayListOf<AttendanceData>()

        getUserData()

        binding.RecyclerViewAttendance.addItemDecoration(
            DividerItemDecoration(
                binding.RecyclerViewAttendance.context,
                1
            )
        )


    }

    /**
     * Get user attendance history from Firebase Realtime Database and pass into the Adapter
     * References: https://www.youtube.com/watch?v=VVXKVFyYQdQ
     */
    private fun getUserData() {
        val ref = database.getReference("Users/$userID/Attendance/")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    dataArrayList.clear()
                    for (userSnapshot in snapshot.children) {

                        /**
                         * Note: in order for the immediate code below to work
                         * The Dataclass variable names must match the dict key in the database itself.
                         */
                        val attendanceData = userSnapshot.getValue(AttendanceData::class.java)
                        dataArrayList.add(attendanceData!!)

                    }
                    binding.RecyclerViewAttendance.adapter = AttendanceAdapter(dataArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}