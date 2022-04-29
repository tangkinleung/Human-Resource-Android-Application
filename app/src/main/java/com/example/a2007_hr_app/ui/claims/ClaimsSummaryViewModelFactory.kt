package com.example.a2007_hr_app.ui.claims

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers

class ClaimsSummaryViewModelFactory : ViewModelProvider.Factory{

    lateinit var countryStateRepository: ClaimsRepo
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        countryStateRepository = ClaimsRepo()
        if (modelClass.isAssignableFrom(ClaimsSummaryViewModel::class.java)) {
            return ClaimsSummaryViewModel(Dispatchers.IO, countryStateRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}