package com.example.a2007_hr_app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.a2007_hr_app.data.AttendanceData
import com.example.a2007_hr_app.data.AttendanceREPO
import com.example.a2007_hr_app.ui.claims.ClaimsRepo
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {


    private val _welcomeText = MutableLiveData<String>().apply {
        value = "Welcome, <Name>"
    }
    val welcomeText: LiveData<String> = _welcomeText

    private val _checkedText = MutableLiveData<String>().apply {
        value = "You're not checked-in!"
    }
    val checkedText: LiveData<String> = _checkedText

    private val _checkedClickable = MutableLiveData<String>().apply {
        value = "Click to Check-in"
    }
    val checkedClickable: LiveData<String> = _checkedClickable

    private val _checkedStatus = MutableLiveData<Boolean>().apply {
        value = false
    }
    val checkedStatus: LiveData<Boolean> = _checkedStatus

    private val _dateTimeText = MutableLiveData<String>().apply {
        value = "Datetime: None"
    }
    val dateTimeText: LiveData<String> = _dateTimeText

    private val _qrLocation = MutableLiveData<String>().apply {
        value = "Location: None"
    }
    val qrLocation: LiveData<String> = _qrLocation

    private val _dateTimeIn = MutableLiveData<String>().apply {
        value = "null"
    }
    val dateTimeIn: LiveData<String> = _dateTimeIn

    private val _dateTimeOut = MutableLiveData<String>().apply {
        value = "null"
    }
    val dateTimeOut: LiveData<String> = _dateTimeOut

    private val _locationValue = MutableLiveData<String>().apply {
        value = "null"
    }
    val locationValue: LiveData<String> = _locationValue

    /**
     * checkOut of location and Update Firebase Data and End time.
     */
    fun checkOut() {
        /**
         * Datetime formatting
         */
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        /**
         * Update Ui Elements
         */
        _checkedStatus.value = false
        _checkedText.value = "Your Not Checked-in!"
        _checkedClickable.value = "Click to Check-in"
        _dateTimeOut.value = "${sdf.format(cal.time)}"
        _dateTimeText.value = "Datetime: None"
        _qrLocation.value = "Location: None"

        /**
         * Save to firebase
         */
        val dataValue = AttendanceData(_locationValue.value, _dateTimeIn.value, _dateTimeOut.value)
        AttendanceREPO().writeAttendance(dataValue)
        //TODO Use WorkManager to write to firebase so information is not missed
    }

    /**
     * checkIn of location and Update Firebase Data and Start time.
     */
    fun checkIn(qrLocation: String) {

        /**
         * Datetime formatting
         */
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        /**
         * Update Ui Elements
         */
        _checkedStatus.value = true
        _checkedText.value = "Your Checked-in!"
        _checkedClickable.value = "Click to Check-Out"
        _dateTimeIn.value = "${sdf.format(cal.time)}"
        _dateTimeText.value = "Datetime-In: ${_dateTimeIn.value}"
        _qrLocation.value = "Location: $qrLocation"
        _locationValue.value = qrLocation

        /**
         * Save to firebase
         */
        val dataValue = AttendanceData(_locationValue.value, _dateTimeIn.value, _dateTimeOut.value)
        AttendanceREPO().writeAttendance(dataValue)
        //TODO Use WorkManager to write to firebase so information is not missed
    }

    /**
     * setWelcomeText: set display name to welcome message
     * @param: name: String
     */
    fun setWelcomeText(name: String) {
        _welcomeText.value = "Welcome, $name"
    }

    /**
     * Claims Amount display
     */
    fun getResponseUsingLiveData(claimType: String) : LiveData<Double> {
        return ClaimsRepo().getAmountBalanceByType(claimType)
    }
}