package com.example.a2007_hr_app.ui.leaves

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import kotlin.properties.Delegates

class LeaveRepo {

    companion object {
        private val TAG = LeaveRepo::class.simpleName
    }

    private var database: FirebaseDatabase = Firebase.database(
        "https://mad-hr-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    private var user = Firebase.auth.currentUser
    private var userID: String = ""

    init {
        user?.let {
            userID = user!!.uid
            Log.d(TAG, "UserID: $userID")
        }
    }

    /**
     * Read Claims History from database
     * For Claims Summary page
     */
    fun readLeaves() {
        val ref = database.getReference("Users/$userID/Leaves/")
        ref.get().addOnSuccessListener {
            Log.d(TAG, "$it, ${it.children}")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Attendance History")
        }
    }

    fun getAmountBalanceByType(leaveType: String): MutableLiveData<Int> {
        lateinit var leaveTypeName: String
        var balanceAmount by Delegates.notNull<Int>()
        when (leaveType) {
            "Medical leave" -> {
                leaveTypeName = "medicalBalance"
            }
            "Annual leave" -> {
                leaveTypeName = "annualBalance"
            }
            "Childcare leave" -> {
                leaveTypeName = "childcareBalance"
            }
            "Compassionate leave" -> {
                leaveTypeName = "compassionateBalance"
            }
        }

        Log.d(TAG, "$leaveType, $leaveTypeName")

        val mutableLiveData = MutableLiveData<Int>()
        val ref = database.getReference("Users/$userID/Leaves/")
        ref.get().addOnSuccessListener {
            balanceAmount = it.child("LeavesBalanceAmount/$leaveTypeName").value.toString().toInt()
            Log.d(TAG, "$it")
            mutableLiveData.postValue(balanceAmount!!)
        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Claims History")
        }
        return mutableLiveData
    }

    // Applying Leave Application to database
    fun addLeave(leave: LeavesModel.LeaveType.LeaveDetails) {
        // Use start date as a key
        // Replace /s as it creates another branch in Firebase
        val startDate = leave.leaveStartDate.replace("/", "-")

        var balanceAmount = 0
        val inputType = leave.leaveType
        val df = DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter(Locale.ENGLISH)
        val leaveStartDate = LocalDate.parse(leave.leaveStartDate, df)
        val leaveEndDate = LocalDate.parse(leave.leaveEndDate, df)

        val inputAmount = (Period.between(leaveStartDate, leaveEndDate)).days
        lateinit var leaveTypeName: String

        //set leavetypename based on user input
        when (inputType) {
            "Medical" -> {
                leaveTypeName = "medicalBalance"
            }
            "Annual" -> {
                leaveTypeName = "annualBalance"
            }
            "Childcare" -> {
                leaveTypeName = "childcareBalance"
            }
            "Compassionate" -> {
                leaveTypeName = "compassionateBalance"
            }
        }

        //Making changes to leave balance in Firebase
        val ref = database.getReference("Users/$userID/Leaves/")
        ref.get().addOnSuccessListener {
            balanceAmount = it.child("LeavesBalanceAmount/$leaveTypeName").value.toString().toInt()

            //if leave balance is greater than input amount, minus the number of leave from balance.
            if (balanceAmount > inputAmount) {
                Log.d(TAG, "deducting $inputAmount from $balanceAmount")
                balanceAmount -= inputAmount
                ref.child("LeavesBalanceAmount/$leaveTypeName").setValue(balanceAmount)

            } else {
                //FAIL
                Log.d(TAG, "Insufficient balance.")
            }

        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Attendance History")
        }

        val ref1 = database.getReference("Users/$userID/Leaves/LeavesDetail/$startDate")
        ref1.setValue(leave)
    }

    // Get list of leave from database
    fun getLeaveList(leaveType: String): MutableList<LeavesModel.LeaveType.LeaveDetails> {
        var testList = mutableListOf<LeavesModel.LeaveType.LeaveDetails>() // Empty List


        val mutableLiveData = MutableLiveData<MutableList<LeavesModel.LeaveType.LeaveDetails>>()

        //call ref
        val ref = database.getReference("Users/$userID/Leaves/LeavesDetail/")

        //Succesful
        ref.get().addOnSuccessListener {
            Log.d(TAG, "$it")

            //
            for (data in it.children) {
                if (data.child("leaveType").value == leaveType) {
                    var detail = LeavesModel.LeaveType.LeaveDetails(
                        data.child("leaveType").value.toString(),
                        data.child("leaveStartDate").value.toString(),
                        data.child("leaveStartTime").value.toString(),
                        data.child("leaveEndDate").value.toString(),
                        data.child("leaveEndTime").value.toString(),
                        data.child("leaveSupervisor").value.toString(),
                        data.child("coveringPerson").value.toString(),
                        data.child("leaveReason").value.toString()
                    )
                    testList.add(detail)
                }


            }
            Log.d(TAG, "first $testList")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Attendance History")
        }
        Log.d(TAG, "second $testList")

        mutableLiveData.postValue(testList)
        return testList
    }


    fun fetchLeaveTypes(): LeavesModel = LeavesModel(
        leaveTypes = mutableListOf(
            LeavesModel.LeaveType(
                "Medical", getLeaveList("Medical")
            ),
            LeavesModel.LeaveType(
                "Annual", getLeaveList("Annual")
            ),
            LeavesModel.LeaveType(
                "Childcare", getLeaveList("Childcare")
            ),
            LeavesModel.LeaveType(
                "Compassionate", getLeaveList("Compassionate")
            )
        )
    )

    fun updateLeave(leave: LeavesModel.LeaveType.LeaveDetails,previousStart:String,previousEnd: String){
        val oldstartDate = previousStart.replace("/", "-")
        val newstartDate = leave.leaveStartDate.replace("/", "-")

        var balanceAmount = 0
        val inputType = leave.leaveType
        val df = DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter(Locale.ENGLISH)
        val leaveStartDate = LocalDate.parse(leave.leaveStartDate, df)
        val leaveEndDate = LocalDate.parse(leave.leaveEndDate, df)
        val previousStartDate = LocalDate.parse(previousStart, df)
        val previousEndDate = LocalDate.parse(previousEnd, df)

        val inputAmount = (Period.between(leaveStartDate, leaveEndDate)).days
        val oldAmount = (Period.between(previousStartDate, previousEndDate)).days
        lateinit var leaveTypeName: String

        //set leavetypename based on user input
        when (inputType) {
            "Medical" -> {
                leaveTypeName = "medicalBalance"
            }
            "Annual" -> {
                leaveTypeName = "annualBalance"
            }
            "Childcare" -> {
                leaveTypeName = "childcareBalance"
            }
            "Compassionate" -> {
                leaveTypeName = "compassionateBalance"
            }
        }

        //Making changes to leave balance in Firebase
        val ref = database.getReference("Users/$userID/Leaves/")
        ref.get().addOnSuccessListener {
            balanceAmount = it.child("LeavesBalanceAmount/$leaveTypeName").value.toString().toInt()
            balanceAmount += oldAmount
            //if leave balance is greater than input amount, minus the number of leave from balance.
            if (balanceAmount > inputAmount) {
                Log.d(TAG, "deducting $inputAmount from $balanceAmount")
                balanceAmount -= inputAmount
                ref.child("LeavesBalanceAmount/$leaveTypeName").setValue(balanceAmount)

            } else {
                //FAIL
                Log.d(TAG, "Insufficient balance.")
            }

        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Attendance History")
        }

        val ref1 = database.getReference("Users/$userID/Leaves/LeavesDetail/$oldstartDate")
        ref1.removeValue()
        val ref2 = database.getReference("Users/$userID/Leaves/LeavesDetail/$newstartDate")
        ref2.setValue(leave)
    }

    fun removeLeave(leave: LeavesModel.LeaveType.LeaveDetails){
        val startDate = leave.leaveStartDate.replace("/", "-")

        var balanceAmount = 0
        val inputType = leave.leaveType
        val df = DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy").toFormatter(Locale.ENGLISH)
        val leaveStartDate = LocalDate.parse(leave.leaveStartDate, df)
        val leaveEndDate = LocalDate.parse(leave.leaveEndDate, df)

        val inputAmount = (Period.between(leaveStartDate, leaveEndDate)).days
        lateinit var leaveTypeName: String

        //set leavetypename based on user input
        when (inputType) {
            "Medical" -> {
                leaveTypeName = "medicalBalance"
            }
            "Annual" -> {
                leaveTypeName = "annualBalance"
            }
            "Childcare" -> {
                leaveTypeName = "childcareBalance"
            }
            "Compassionate" -> {
                leaveTypeName = "compassionateBalance"
            }
        }

        //Making changes to leave balance in Firebase
        val ref = database.getReference("Users/$userID/Leaves/")
        ref.get().addOnSuccessListener {
            balanceAmount = it.child("LeavesBalanceAmount/$leaveTypeName").value.toString().toInt()
            balanceAmount += inputAmount
            ref.child("LeavesBalanceAmount/$leaveTypeName").setValue(balanceAmount)


        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Leaves")
        }
        val ref1 = database.getReference("Users/$userID/Leaves/LeavesDetail/$startDate")
        ref1.removeValue()
    }
}