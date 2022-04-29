package com.example.a2007_hr_app.ui.claims

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlin.properties.Delegates

class ClaimsRepo {

    companion object {
        private val TAG = ClaimsRepo::class.simpleName
    }

    private var database: FirebaseDatabase = Firebase.database(
        "https://mad-hr-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    private var user = Firebase.auth.currentUser
    private var userID: String = ""

    init {
        user?.let {
            userID = user!!.uid
        }
    }

    /**
     * Methods
     */

    /**
     * This method is invoked when users have no existing claims balance amount data.
     */
    fun seedData() {
        val data = ClaimsBalanceAmount(500.0, 500.0, 500.0)

        val ref = database.getReference("Users/$userID/Claims/ClaimsBalanceAmount/")
        ref.get().addOnSuccessListener {
            Log.d(TAG, "$it")
            //Name is KEY
            ref.setValue(data)
        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Attendance History")
        }
    }


    /**
     * Write Claims Data to database
     * For use with create claims page
     * @param: claimsDetail: ClaimsDetail
     */
    fun writeClaim(claimsDetail: ClaimsModel.ClaimType.ClaimDetail) {
        val startDate = claimsDetail.claimDateTime.replace("/", "-")

        val refClaims = database.getReference("Users/$userID/Claims/ClaimsDetail/$startDate")
        updateAmount(claimsDetail)
        refClaims.setValue(claimsDetail)
    }


    /**
     * Updates Claims' status to 'Deleted' in database
     * For use with claims summary page
     * @param: claimsDetail: ClaimsDetail
     */
    fun setDeleteClaim(claimsDetail: ClaimsModel.ClaimType.ClaimDetail) {
        val startDate = claimsDetail.claimDateTime.replace("/", "-")
        val ref = database.getReference("Users/$userID/Claims")
        val claimType = claimsDetail.claimType
        lateinit var claimTypeName: String
        when (claimType) {
            "Medical" -> {
                claimTypeName = "medicalBalance"
            }
            "Transport" -> {
                claimTypeName = "transportBalance"
            }
            "Others" -> {
                claimTypeName = "othersBalance"
            }
        }

        ref.get().addOnSuccessListener {
            // DELETE FIRST
            var amountToReturn =
                it.child("/ClaimsDetail/${startDate}/claimAmount").value.toString().toDouble()
            var balanceAmount =
                it.child("ClaimsBalanceAmount/$claimTypeName").value.toString().toDouble()

            // RETURN BACK MONEY
            Log.d(TAG, "adding back $amountToReturn from $balanceAmount")
            balanceAmount += amountToReturn
            ref.child("/ClaimsBalanceAmount/$claimTypeName").setValue(balanceAmount)
            ref.child("/ClaimsDetail/${startDate}/claimStatus").setValue("Deleted")
        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Attendance History")
        }
    }

    private fun updateAmount(claimsDetail: ClaimsModel.ClaimType.ClaimDetail) {
        var balanceAmount = 0.0
        val claimType = claimsDetail.claimType
        val amount = claimsDetail.claimAmount
        lateinit var claimTypeName: String
        when (claimType) {
            "Medical" -> {
                claimTypeName = "medicalBalance"
            }
            "Transport" -> {
                claimTypeName = "transportBalance"
            }
            "Others" -> {
                claimTypeName = "othersBalance"
            }
        }
        val ref = database.getReference("Users/$userID/Claims/")

        ref.get().addOnSuccessListener {
            balanceAmount =
                it.child("ClaimsBalanceAmount/$claimTypeName").value.toString().toDouble()
            when (ClaimsApplicationDialogFragment.OPERATION_MODE) {
                ClaimsApplicationDialogFragment.APPLY_MODE -> {
                    if (balanceAmount > amount) {
                        Log.d(TAG, "deducting $amount from $balanceAmount")
                        balanceAmount -= amount
                        ref.child("ClaimsBalanceAmount/$claimTypeName").setValue(balanceAmount)
                    } else {
                        Log.d(TAG, "Insufficient balance.")
                    }
                }
                ClaimsApplicationDialogFragment.EDIT_MODE -> {
                    var oldAmount =
                        it.child("ClaimsDetail/${claimsDetail.claimDateTime}/claimAmount").value.toString()
                            .toDouble()
                    var newAmount = oldAmount - amount
                    if (balanceAmount > amount) {
                        balanceAmount += newAmount
                        Log.d(TAG, "$oldAmount, $newAmount, $balanceAmount")
                        ref.child("ClaimsBalanceAmount/$claimTypeName").setValue(balanceAmount)
                    } else {
                        //FAIL. Theoretically should not come here. EVER.
                        Log.d(TAG, "Insufficient balance.")
                    }
                }
            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Attendance History")
        }
    }


    fun getAmountBalanceByType(claimType: String): MutableLiveData<Double> {
        lateinit var claimTypeName: String
        var balanceAmount by Delegates.notNull<Double>()
        when (claimType) {
            "Medical" -> {
                claimTypeName = "medicalBalance"
            }
            "Transport" -> {
                claimTypeName = "transportBalance"
            }
            "Others" -> {
                claimTypeName = "othersBalance"
            }
        }

        val mutableLiveData = MutableLiveData<Double>()
        val ref = database.getReference("Users/$userID/Claims/")
        ref.get().addOnSuccessListener {
            balanceAmount =
                it.child("ClaimsBalanceAmount/$claimTypeName").value.toString().toDouble()

            if (balanceAmount != null){
                mutableLiveData.postValue(balanceAmount!!)
            } 

        }.addOnFailureListener {
            Log.e(TAG, "Failed to Get Claims History")
        }
        return mutableLiveData
    }

    private fun getClaimsList(claimType: String): MutableList<ClaimsModel.ClaimType.ClaimDetail> {
        var claimsList = mutableListOf<ClaimsModel.ClaimType.ClaimDetail>()
        val mutableLiveData = MutableLiveData<MutableList<ClaimsModel.ClaimType.ClaimDetail>>()

        val ref = database.getReference("Users/$userID/Claims/ClaimsDetail")
        ref.get().addOnSuccessListener {
            for (data in it.children) {
                var texts = data.child("claimStatus").getValue<String>()
                var deleted = "Deleted"
                Log.d(TAG, "$texts, $deleted")
                if (texts != deleted) {
                    if (data.child("claimType").value == claimType) {
                        var detail = ClaimsModel.ClaimType.ClaimDetail(
                            data.child("claimAmount").value.toString().toDouble(),
                            data.child("claimDateTime").value.toString(),
                            data.child("claimStatus").value.toString(),
                            data.child("claimType").value.toString(),
                            data.child("claimReason").value.toString(),
                            data.child("claimFile").value.toString()
                        )
                        claimsList.add(detail)
                    }
                }
            }
        }.addOnFailureListener {
            Log.d(TAG, "Failed to Get Claims History")
        }
        mutableLiveData.postValue(claimsList)


        claimsList.sortByDescending { T -> T.claimDateTime }

        return claimsList
    }

    fun fetchClaimTypes(): ClaimsModel = ClaimsModel(
        claimTypes = mutableListOf(
            ClaimsModel.ClaimType("Medical", getClaimsList("Medical")),
            ClaimsModel.ClaimType("Transport", getClaimsList("Transport")),
            ClaimsModel.ClaimType("Others", getClaimsList("Others"))
        )
    )

}