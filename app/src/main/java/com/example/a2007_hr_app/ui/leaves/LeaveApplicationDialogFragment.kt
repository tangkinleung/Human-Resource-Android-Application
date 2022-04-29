package com.example.a2007_hr_app.ui.leaves

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.a2007_hr_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*


class LeaveApplicationDialogFragment : DialogFragment() {

    private lateinit var viewModel: LeaveApplicationDialogViewModel

    companion object {
        fun newInstance() = LeaveApplicationDialogFragment()
        const val EDIT_MODE = 1
        var OPERATION_MODE = 0
        var TAG = LeaveApplicationDialogFragment::javaClass
        lateinit var leaveItem: LeavesModel.LeaveType.LeaveDetails
    }

    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var supervisorText: EditText
    private lateinit var coveringText: EditText
    private lateinit var reasonText: EditText
    private val myCalendar: Calendar = Calendar.getInstance()
    private val leaveRepo = LeaveRepo()

    var previousStart = ""
    var previousEnd = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leave_application_dialog, container, false)

        // Leave Type selection
        val leaveType = view.findViewById<Spinner>(R.id.spinnerLeaveTypesDialog)
        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.LeaveTypes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            leaveType.adapter = adapter
        }

        // Date selections
        startDate = view.findViewById(R.id.editTextLeaveStartDate)
        endDate = view.findViewById(R.id.editTextLeaveEndDate)
        val startAM = view.findViewById<ToggleButton>(R.id.toggleButtonLeaveStartAM)
        val startPM = view.findViewById<ToggleButton>(R.id.toggleButtonLeaveStartPM)
        val endAM = view.findViewById<ToggleButton>(R.id.toggleButtonLeaveEndAM)
        val endPM = view.findViewById<ToggleButton>(R.id.toggleButtonLeaveEndPM)
        var startTime = "AM"
        var endTime = "AM"

        // Toggle between either AM or PM selections
        // Disable the other button when selected
        startAM.setOnClickListener {
            startAM.isChecked = true
            startPM.isChecked = false
            startTime = "AM"
        }
        startPM.setOnClickListener {
            startAM.isChecked = false
            startPM.isChecked = true
            startTime = "PM"
        }

        endAM.setOnClickListener {
            endAM.isChecked = true
            endPM.isChecked = false
            endTime = "AM"
        }
        endPM.setOnClickListener {
            endAM.isChecked = false
            endPM.isChecked = true
            endTime = "PM"
        }

        // Start Date's Datetime picker
        val startDatePicker =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateStartDate()
            }
        // End Date's Datetime picker
        val endDatePicker =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, month)
                myCalendar.set(Calendar.DAY_OF_MONTH, day)
                updateEndDate()
            }

        // Start date's edit text onclick listener
        startDate.setOnClickListener(View.OnClickListener {
            context?.let { it1 ->
                DatePickerDialog(
                    it1,
                    startDatePicker,
                    myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
                ).show()
            }
        })

        // End date's edit text onclick listener
        endDate.setOnClickListener(View.OnClickListener {
            context?.let { it1 ->
                DatePickerDialog(
                    it1,
                    endDatePicker,
                    myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
                ).show()
            }
        })

        // Other text inputs
        supervisorText = view.findViewById(R.id.editTextLeaveSupervisor)
        coveringText = view.findViewById(R.id.editTextLeaveCovering)
        reasonText = view.findViewById(R.id.editTextLeaveReason)

        // Submit and clear buttons
        val submitButton = view.findViewById<Button>(R.id.buttonSubmitLA)
        val clearButton = view.findViewById<Button>(R.id.buttonClearLA)

        if (OPERATION_MODE == EDIT_MODE) {//check if invoked from edit
            //fill in the previous values
            val typeList = resources.getStringArray(R.array.LeaveTypes)
            val posit = typeList.indexOf(leaveItem.leaveType)
            leaveType.setSelection(posit)
            startDate.setText(leaveItem.leaveStartDate)
            endDate.setText(leaveItem.leaveEndDate)
            coveringText.setText(leaveItem.CoveringPerson)
            reasonText.setText(leaveItem.leaveReason)
            supervisorText.setText(leaveItem.leaveSupervisor)
            //to be used in the updaterepo method
            previousStart = leaveItem.leaveStartDate
            previousEnd = leaveItem.leaveEndDate
        }

        submitButton.setOnClickListener {
            if (checkInputsFilled()) {
                // Instantiate a new leave object to submit
                leaveItem = LeavesModel.LeaveType.LeaveDetails(
                    leaveType.selectedItem.toString(), startDate.text.toString(),
                    startTime, endDate.text.toString(), endTime, supervisorText.text.toString(),
                    coveringText.text.toString(), reasonText.text.toString()
                )
                showValuesDialog(view, leaveItem) // Application confirmation
            }
        }

        clearButton.setOnClickListener {
            startDate.setText("")
            endDate.setText("")
            supervisorText.setText("")
            coveringText.setText("")
            reasonText.setText("")
            startAM.isChecked = true
            startPM.isChecked = false
            endAM.isChecked = true
            endPM.isChecked = false
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        dialog!!.window?.setLayout(width, height)
    }

    /**
     * External functions for backend logic
     */

    private fun updateStartDate() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        startDate.setText(dateFormat.format(myCalendar.time))
    }

    private fun updateEndDate() {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        endDate.setText(dateFormat.format(myCalendar.time))
    }

    /**
     * Function to check if editText boxes are filled
     * and to check if date is correctly formatted to dd/mm/yyyy
     * @return boolean
     */
    private fun checkInputsFilled(): Boolean {

        var correctInput = true
        // Check Edittexts are filled
        if (supervisorText.text.isEmpty()) {
            Toast.makeText(context, "Missing fields!", Toast.LENGTH_SHORT).show()
            supervisorText.error = "Missing field!"
            correctInput = false
        }
        if (coveringText.text.isEmpty()) {
            Toast.makeText(context, "Missing fields!", Toast.LENGTH_SHORT).show()
            coveringText.error = "Missing field!"
            correctInput = false
        }
        if (reasonText.text.isEmpty()) {
            Toast.makeText(context, "Missing fields!", Toast.LENGTH_SHORT).show()
            reasonText.error = "Missing field!"
            correctInput = false
        }

        // Check dates are filled and are correct format
        // Make sure there is /s
        if (startDate.text.isEmpty()) {
            Toast.makeText(context, "Missing fields!", Toast.LENGTH_SHORT).show()
            startDate.error = "Missing field!"
            correctInput = false
        } else {
            if (!startDate.text.contains("/")) {
                // When dates have no slashes
                Toast.makeText(context, "Invalid date format!", Toast.LENGTH_SHORT).show()
                startDate.error = "Invalid date format!"
                correctInput = false
            }
        }
        if (endDate.text.isEmpty()) {
            Toast.makeText(context, "Missing fields!", Toast.LENGTH_SHORT).show()
            endDate.error = "Missing field!"
            correctInput = false
        } else {
            if (!endDate.text.contains("/")) {
                // When dates have no slashes
                Toast.makeText(context, "Invalid date format!", Toast.LENGTH_SHORT).show()
                endDate.error = "Invalid date format!"
                correctInput = false
            }
        }

        // Make sure there is 3 /s and length of each date element is enough
        // DD/MM/YYYY
        val startdates = startDate.text.split("/")
        val enddates = endDate.text.split("/")

        if (startdates.size != 3) {
            // When the number of slashes is not 2
            Toast.makeText(context, "Not enough slashes!", Toast.LENGTH_SHORT).show()
            startDate.error = "Not enough slashes!"
            correctInput = false
        } else {
            if (startdates[0].length != 2 || startdates[1].length != 2 || startdates[2].length != 4) {
                // When there is no 2 digits for day, 2 digits for month and 4 digits for year
                // Basically incorrect formatting
                Toast.makeText(context, "Invalid date format!", Toast.LENGTH_SHORT).show()
                startDate.error = "DD/MM/YYYY"
                correctInput = false
            }
        }
        if (enddates.size != 3) {
            // When the number of slashes is not 2
            Toast.makeText(context, "Not enough slashes!", Toast.LENGTH_SHORT).show()
            endDate.error = "Not enough slashes!"
            correctInput = false
        } else {
            if (enddates[0].length != 2 || enddates[1].length != 2 || enddates[2].length != 4) {
                // When there is no 2 digits for day, 2 digits for month and 4 digits for year
                // Basically incorrect formatting
                Toast.makeText(context, "Invalid date format!", Toast.LENGTH_SHORT).show()
                endDate.error = "DD/MM/YYYY"
                correctInput = false
            }
        }

        // Escape when even one edit text is empty
        return correctInput
    }

    /**
     * Warning popup for confirmation
     */
    private fun showValuesDialog(view: View, leave: LeavesModel.LeaveType.LeaveDetails) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Leave Application Confirmation")
            .setMessage("Would you like to submit?")
            .setNeutralButton("No") { dialog, which ->
                // When no is pressed
//                Toast.makeText(context, "Amending", Toast.LENGTH_LONG).show()
            }
            .setPositiveButton("Yes") { dialog, which ->
                // When yes is pressed
                showConfirmationDialog(view, leave)
            }
            .show()
    }

    /**
     * Second confirmation check
     */
    private fun showConfirmationDialog(view: View, leave: LeavesModel.LeaveType.LeaveDetails) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Are you sure?")
            .setMessage("Would you like to submit?")
            .setNegativeButton("No") { dialog, which ->
                // When no is pressed
//                showValuesDialog(view)
//                Toast.makeText(context, "Reached No", Toast.LENGTH_LONG).show()
                setFragmentResult("delete", bundleOf("editChoice" to false))
                dismiss()
            }
            .setPositiveButton("Yes") { dialog, which ->
                // When yes is pressed
                if (OPERATION_MODE == EDIT_MODE) {
                    leaveRepo.updateLeave(leave, previousStart, previousEnd)
                    setFragmentResult("edit", bundleOf("editChoice" to true))
                } else {
                    //Submit leave
                    leaveRepo.addLeave(leave)
                }
                dismiss()
                Toast.makeText(context, "Application submitted", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}