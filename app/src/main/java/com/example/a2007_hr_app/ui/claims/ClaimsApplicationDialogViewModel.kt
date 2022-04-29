package com.example.a2007_hr_app.ui.claims

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

class ClaimsApplicationDialogViewModel (
    private val repository: ClaimsRepo = ClaimsRepo()
): ViewModel() {

    fun getResponseUsingLiveData(claimType: String) : LiveData<Double> {
        return repository.getAmountBalanceByType(claimType)
    }
}