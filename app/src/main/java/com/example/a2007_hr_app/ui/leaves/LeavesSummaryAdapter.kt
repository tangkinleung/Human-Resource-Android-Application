package com.example.a2007_hr_app.ui.leaves

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2007_hr_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LeavesSummaryAdapter(
    var context: Context,
    var expendableLeaveTypesModelList: MutableList<ExpandableLeaveTypeModel>,
    var fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isFirstItemExpanded: Boolean = true
    private var actionLock: Boolean = false

    companion object{
        private val TAG = LeavesSummaryAdapter::class.java.simpleName
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ExpandableLeaveTypeModel.PARENT -> {
                LeaveParentViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.leave_expandable_parent_item, parent, false
                    )
                )
            }
            ExpandableLeaveTypeModel.CHILD -> {
                LeaveChildViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.leave_expandable_child_item, parent, false
                    )
                )
            }
            else -> {
                LeaveParentViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.leave_expandable_parent_item, parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = expendableLeaveTypesModelList[position]
        Log.d(TAG, expendableLeaveTypesModelList.size.toString())
        when (row.type) {
            ExpandableLeaveTypeModel.PARENT -> {
                Log.d(TAG, row.leaveTypeParent.leaveType)
                (holder as LeaveParentViewHolder).leaveType.text =
                    row.leaveTypeParent.leaveType
                holder.layout.setOnClickListener {
                    if (!row.isExpanded) {
                        row.isExpanded = true
                        expandRow(position)
                        holder.openImage.visibility = View.GONE
                        holder.closeImage.visibility = View.VISIBLE
                        holder.startHeader.visibility = View.VISIBLE
                        holder.endHeader.visibility = View.VISIBLE
                        holder.statusHeader.visibility = View.VISIBLE
                    }else{
                        row.isExpanded = false
                        collapseRow(position)
                        holder.openImage.visibility = View.VISIBLE
                        holder.closeImage.visibility = View.GONE
                        holder.startHeader.visibility = View.GONE
                        holder.endHeader.visibility = View.GONE
                        holder.statusHeader.visibility = View.GONE
                    }
                }
            }

            ExpandableLeaveTypeModel.CHILD -> {
                var dateStringStart = row.leaveTypeChild.leaveStartDate
                var datestart =
                    LocalDate.parse(dateStringStart, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                (holder as LeaveChildViewHolder).start.text = datestart.toString()
                var dateStringEnd = row.leaveTypeChild.leaveEndDate
                var dateEnd =
                    LocalDate.parse(dateStringEnd, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                (holder).end.text = dateEnd.toString()
                (holder).status.text = row.leaveTypeChild.CoveringPerson

                //set the images to do things accordingly
                (holder).editImage.setOnClickListener{
                    LeaveApplicationDialogFragment.OPERATION_MODE = LeaveApplicationDialogFragment.EDIT_MODE
                    LeaveApplicationDialogFragment.leaveItem = row.leaveTypeChild
                    LeaveApplicationDialogFragment().show(fragmentManager,"Leave edit" )

                }
                (holder).deleteImage.setOnClickListener{
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Are you sure")
                        .setMessage("You would like to delete?")
                        .setNeutralButton("No") {dialog, which ->
                        }
                        .setNegativeButton("Yes"){dialog, which ->
                            LeaveRepo().removeLeave(row.leaveTypeChild)
                            expendableLeaveTypesModelList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .show()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return expendableLeaveTypesModelList[position].type
    }

    private fun expandRow(position: Int) {
        val row = expendableLeaveTypesModelList[position]
        var nextPosition = position
        when (row.type) {
            ExpandableLeaveTypeModel.PARENT -> {
                for (child in row.leaveTypeParent.leaveColumns) {
                    expendableLeaveTypesModelList.add(
                        ++nextPosition,
                        ExpandableLeaveTypeModel(ExpandableLeaveTypeModel.CHILD, child)
                    )
                }
                notifyDataSetChanged()
            }
            ExpandableLeaveTypeModel.CHILD -> {
                notifyDataSetChanged()
            }
        }
    }

    private fun collapseRow(position: Int) {
        val row = expendableLeaveTypesModelList[position]
        var nextPosition = position + 1
        when (row.type) {
            ExpandableLeaveTypeModel.PARENT -> {
                outerloop@ while (true) {
                    //  println("Next Position during Collapse $nextPosition size is ${shelfModelList.size} and parent is ${shelfModelList[nextPosition].type}")
                    if (nextPosition == expendableLeaveTypesModelList.size || expendableLeaveTypesModelList[nextPosition].type == ExpandableLeaveTypeModel.PARENT) {
                        /* println("Inside break $nextPosition and size is ${closedShelfModelList.size}")
                         closedShelfModelList[closedShelfModelList.size-1].isExpanded = false
                         println("Modified closedShelfModelList ${closedShelfModelList.size}")*/
                        break@outerloop
                    }
                    expendableLeaveTypesModelList.removeAt(nextPosition)
                }
                notifyDataSetChanged()
            }

        }
    }

    class LeaveParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var layout =
            itemView.findViewById<ConstraintLayout>(R.id.leavetypes_item_parent_container)
        internal var leaveType: TextView = itemView.findViewById(R.id.leaveType_name)
        internal var openImage = itemView.findViewById<ImageView>(R.id.leave_down_arrow)
        internal var closeImage = itemView.findViewById<ImageView>(R.id.leave_up_arrow)
        internal var startHeader = itemView.findViewById<TextView>(R.id.leaveHeaderStartTextView)
        internal var endHeader = itemView.findViewById<TextView>(R.id.leaveHeaderEndTextView)
        internal var statusHeader = itemView.findViewById<TextView>(R.id.leaveHeaderStatusTextView)
    }

    class LeaveChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var layout =
            itemView.findViewById<ConstraintLayout>(R.id.leavetypes_item_child_container)
        internal var start: TextView = itemView.findViewById(R.id.leaveListStartTextView)
        internal var end: TextView = itemView.findViewById(R.id.leaveListEndTextView)
        internal var status: TextView = itemView.findViewById(R.id.leaveListStatusTextView)
        internal var editImage: Button = itemView.findViewById(R.id.leaveListEdit)
        internal var deleteImage: Button = itemView.findViewById(R.id.leaveListDelete)
    }

    override fun getItemCount(): Int {
        return expendableLeaveTypesModelList.size
    }

}