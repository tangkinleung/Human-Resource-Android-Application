package com.example.a2007_hr_app.ui.claims

data class ClaimsModel (val claimTypes: MutableList<ClaimType>){
    data class ClaimType(
        var claimType: String, //Medical, Transport, Others
        var claimDetails: MutableList<ClaimDetail>
    ){
        data class ClaimDetail(
            var claimAmount: Double,
            var claimDateTime: String,
            var claimStatus: String,
            var claimType: String,
            var claimReason: String,
            var claimFile: String
        )
    }

}