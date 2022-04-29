package com.example.a2007_hr_app.ui.leaves

class ExpandableLeaveTypeModel {
    companion object {
        const val PARENT = 1
        const val CHILD = 2
    }

    lateinit var leaveTypeParent: LeavesModel.LeaveType
    var type: Int
    lateinit var leaveTypeChild: LeavesModel.LeaveType.LeaveDetails
    var isExpanded: Boolean
    private var isCloseShown: Boolean

    constructor(
        type: Int,
        leaveTypeParent: LeavesModel.LeaveType,
        isExpanded: Boolean = false,
        isCloseShown: Boolean = false
    ) {
        this.type = type
        this.leaveTypeParent = leaveTypeParent
        this.isExpanded = isExpanded
        this.isCloseShown = isCloseShown
    }

    constructor(
        type: Int,
        leaveTypeChild: LeavesModel.LeaveType.LeaveDetails,
        isExpanded: Boolean = false,
        isCloseShown: Boolean = false
    ) {
        this.type = type
        this.leaveTypeChild = leaveTypeChild
        this.isExpanded = isExpanded
        this.isCloseShown = isCloseShown
    }
}