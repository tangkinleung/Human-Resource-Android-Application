package com.example.a2007_hr_app.ui.claims

class ExpandableClaimTypeModel {
    companion object{
        const val PARENT = 1
        const val CHILD = 2
    }
    lateinit var claimsTypeParent: ClaimsModel.ClaimType
    var type : Int
    lateinit var claimsTypeChild : ClaimsModel.ClaimType.ClaimDetail
    var isExpanded : Boolean
    private var isCloseShown : Boolean


    constructor(type : Int,
                claimsTypeParent: ClaimsModel.ClaimType,
                isExpanded : Boolean = false,
                isCloseShown : Boolean = false ){
        this.type = type
        this.claimsTypeParent = claimsTypeParent
        this.isExpanded = isExpanded
        this.isCloseShown = isCloseShown
    }


    constructor(type : Int,
                claimsTypeChild : ClaimsModel.ClaimType.ClaimDetail,
                isExpanded : Boolean = false,
                isCloseShown : Boolean = false){
        this.type = type
        this.claimsTypeChild = claimsTypeChild
        this.isExpanded = isExpanded
        this.isCloseShown = isCloseShown
    }
}