package com.example.a2007_hr_app.ui.leaves

data class LeavesModel(val leaveTypes: MutableList<LeaveType>) {
    data class LeaveType(
        var leaveType: String, //Medical, Annual, etc
        var leaveColumns: MutableList<LeaveDetails>
    ) {
        data class LeaveDetails(
            var leaveType: String,
            var leaveStartDate: String,
            var leaveStartTime: String,
            var leaveEndDate: String,
            var leaveEndTime: String,
            var leaveSupervisor: String,
            var CoveringPerson: String,
            var leaveReason: String
        )
    }
}