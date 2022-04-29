package com.example.a2007_hr_app.ui.leaves

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers


class LeaveApplicationDialogViewModel (
    private val repository: LeaveRepo = LeaveRepo()
): ViewModel() {

    fun getResponseUsingLiveData(leaveType: String) : LiveData<Int> {
        return repository.getAmountBalanceByType(leaveType)
    }

    val responseLiveData = liveData(Dispatchers.IO) {
        emit(repository.getAmountBalanceByType("Medical"))
    }
}