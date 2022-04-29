package com.example.a2007_hr_app.ui.leaves

import androidx.lifecycle.*
import com.example.a2007_hr_app.ui.claims.ResultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LeavesViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val LeaveRepo: LeaveRepo
) : ViewModel(), LifecycleObserver {

    var loading: MutableLiveData<Boolean> = MutableLiveData()
    private val _obtainLeaveSummaryResponse = MutableLiveData<ResultOf<LeavesModel>>()
    val obtainLeaveSummaryResponse: LiveData<ResultOf<LeavesModel>> = _obtainLeaveSummaryResponse

    fun obtainLeaveTypes() {
        loading.postValue(true)

        viewModelScope.launch(dispatcher) {
            var errorCode = -1
            try {
                var leaveTypeResponse = LeaveRepo.fetchLeaveTypes()
                leaveTypeResponse.let {
                    loading.postValue(false)
                    _obtainLeaveSummaryResponse.postValue(ResultOf.Success(it))
                }
            } catch (e: Exception) {
                loading.postValue(false)
                e.printStackTrace()
                if (errorCode != -1) {
                    _obtainLeaveSummaryResponse.postValue(
                        ResultOf.Failure(
                            "Failed with Error Code ${errorCode} ",
                            e
                        )
                    )
                } else {
                    _obtainLeaveSummaryResponse.postValue(
                        ResultOf.Failure(
                            "Failed with Exception ${e.message} ",
                            e
                        )
                    )
                }
            }
        }
    }

    fun prepareDataForExpandableAdapter(leaveType: LeavesModel): MutableList<ExpandableLeaveTypeModel> {
        var expandableLeaveModel = mutableListOf<ExpandableLeaveTypeModel>()
        for (leaveTypes in leaveType.leaveTypes) {
            expandableLeaveModel.add(
                ExpandableLeaveTypeModel(
                    ExpandableLeaveTypeModel.PARENT,
                    leaveTypes
                )
            )
        }
        return expandableLeaveModel
    }

}

class LeavesViewModelFactory : ViewModelProvider.Factory {
    lateinit var LeaveRepo: LeaveRepo
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        LeaveRepo = LeaveRepo()
        if (modelClass.isAssignableFrom(LeavesViewModel::class.java)) {
            return LeavesViewModel(Dispatchers.IO, LeaveRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}