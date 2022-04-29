package com.example.a2007_hr_app.ui.claims

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.lang.Exception
class ClaimsSummaryViewModel (private val dispatcher: CoroutineDispatcher, private val claimSummaryRepository: ClaimsRepo) : ViewModel(),
    LifecycleObserver {

    var loading: MutableLiveData<Boolean> = MutableLiveData()
    private val _obtainClaimsSummaryResponse= MutableLiveData<ResultOf<ClaimsModel>>()
    val obtainClaimsResponse: LiveData<ResultOf<ClaimsModel>> = _obtainClaimsSummaryResponse

    fun obtainClaimTypes(){
        loading.postValue(true)

        viewModelScope.launch(dispatcher){
            var  errorCode = -1
            try{
                var stateCapitalResponse =  claimSummaryRepository.fetchClaimTypes()
                stateCapitalResponse?.let {
                    loading.postValue(false)
                    _obtainClaimsSummaryResponse.postValue(ResultOf.Success(it))
                }

            }catch (e : Exception){
                loading.postValue(false)
                e.printStackTrace()
                if(errorCode != -1){
                    _obtainClaimsSummaryResponse.postValue(ResultOf.Failure("Failed with Error Code $errorCode ", e))
                }else{
                    _obtainClaimsSummaryResponse.postValue(ResultOf.Failure("Failed with Exception ${e.message} ", e))
                }
            }
        }
    }

    fun prepareDataForExpandableAdapter(stateCapital: ClaimsModel) : MutableList<ExpandableClaimTypeModel>{
        var expandableCountryModel = mutableListOf<ExpandableClaimTypeModel>()
        for (states in stateCapital.claimTypes) {
            expandableCountryModel.add(ExpandableClaimTypeModel(ExpandableClaimTypeModel.PARENT,states))
        }
        return expandableCountryModel
    }

}