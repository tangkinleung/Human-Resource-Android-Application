package com.example.a2007_hr_app.ui.claims


import android.content.Context
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
import com.example.a2007_hr_app.Utils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ClaimsSummaryAdapter(
    var context: Context,
    var expendableClaimsTypeModelList: MutableList<ExpandableClaimTypeModel>,
    var supportFragmentManager: FragmentManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isFirstItemExpanded: Boolean = true
    private var actionLock = false
    private var TAG = "claimAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ExpandableClaimTypeModel.PARENT -> {
                ClaimsTypeParentViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.claims_expendable_parent_item, parent, false
                    )
                )
            }

            ExpandableClaimTypeModel.CHILD -> {
                ClaimsTypeChildViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.claims_expendable_child_item, parent, false
                    )
                )
            }

            else -> {
                ClaimsTypeParentViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.claims_expendable_parent_item, parent, false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int = expendableClaimsTypeModelList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val row = expendableClaimsTypeModelList[position]

        when (row.type) {
            ExpandableClaimTypeModel.PARENT -> {
                (holder as ClaimsTypeParentViewHolder).countryName.text =
                    row.claimsTypeParent.claimType
                holder.layout.setOnClickListener {
                    if (!row.isExpanded) {
                        row.isExpanded = true
                        expandRow(position)
                        holder.openImage.visibility = View.GONE
                        holder.closeImage.visibility = View.VISIBLE

                        holder.amountHeader.visibility = View.VISIBLE
                        holder.historyHeader.visibility = View.VISIBLE
                        holder.statusHeader.visibility = View.VISIBLE

                    }else {
                        row.isExpanded = false
                        collapseRow(position)
                        holder.openImage.visibility = View.VISIBLE
                        holder.closeImage.visibility = View.GONE

                        holder.amountHeader.visibility = View.GONE
                        holder.historyHeader.visibility = View.GONE
                        holder.statusHeader.visibility = View.GONE
                    }
                }

            }

            ExpandableClaimTypeModel.CHILD -> {
                (holder as ClaimsTypeChildViewHolder).claimsAmount.text =
                    "$" + String.format("%.2f", row.claimsTypeChild.claimAmount)
                var dateString = row.claimsTypeChild.claimDateTime
                var dateLong = Utils().convertDateTimeToLong(dateString)
                var date = Utils().getTimeAgo(dateLong)
                (holder).claimsHistory.text = date
                (holder).claimsStatus.text = row.claimsTypeChild.claimStatus


                (holder).claimsAmend.setOnClickListener {
                    ClaimsApplicationDialogFragment.OPERATION_MODE = ClaimsApplicationDialogFragment.EDIT_MODE //EDIT MODE (for amendment)
                    ClaimsApplicationDialogFragment.claimData = row.claimsTypeChild
                    ClaimsApplicationDialogFragment().show(supportFragmentManager, "this")
                }


                (holder).claimsDelete.setOnClickListener {
                     MaterialAlertDialogBuilder(context)
                        .setTitle("Are you sure")
                        .setMessage("You would like to delete?")
                        .setNeutralButton("No") {dialog, which ->
                        }
                        .setNegativeButton("Yes"){dialog, which ->
                            ClaimsRepo().setDeleteClaim(row.claimsTypeChild)
                            expendableClaimsTypeModelList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .show()
                }
                if(row.claimsTypeChild.claimStatus=="Approved"){
                    (holder).claimsAmend.isEnabled = false
                    (holder).claimsDelete.isEnabled = false
                    (holder).claimsDelete.backgroundTintList = context.resources.getColorStateList(R.color.grey)
                }
                else{
                    (holder).claimsAmend.isEnabled = true
                    (holder).claimsDelete.isEnabled = true
                    (holder).claimsDelete.backgroundTintList = context.resources.getColorStateList(R.color.red)
                }
            }
        }
    }


    override fun getItemViewType(position: Int): Int = expendableClaimsTypeModelList[position].type

    private fun expandRow(position: Int) {
        val row = expendableClaimsTypeModelList[position]
        var nextPosition = position
        when (row.type) {
            ExpandableClaimTypeModel.PARENT -> {
                for (child in row.claimsTypeParent.claimDetails) {
                    expendableClaimsTypeModelList.add(
                        ++nextPosition,
                        ExpandableClaimTypeModel(ExpandableClaimTypeModel.CHILD, child)
                    )
                }
                notifyDataSetChanged()
            }
            ExpandableClaimTypeModel.CHILD -> {
                notifyDataSetChanged()
            }
        }
    }

    private fun collapseRow(position: Int) {
        val row = expendableClaimsTypeModelList[position]
        var nextPosition = position + 1
        when (row.type) {
            ExpandableClaimTypeModel.PARENT -> {
                outerloop@ while (true) {
                    if (nextPosition == expendableClaimsTypeModelList.size || expendableClaimsTypeModelList[nextPosition].type == ExpandableClaimTypeModel.PARENT) {
                        break@outerloop
                    }
                    expendableClaimsTypeModelList.removeAt(nextPosition)
                }
                notifyDataSetChanged()
            }
        }
    }

    class ClaimsTypeParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var layout =
            itemView.findViewById<ConstraintLayout>(R.id.claimtypes_item_parent_container)
        internal var countryName: TextView = itemView.findViewById(R.id.claimtypes_name)
        internal var openImage = itemView.findViewById<ImageView>(R.id.down_arrow)
        internal var closeImage = itemView.findViewById<ImageView>(R.id.up_arrow)
        internal var amountHeader = itemView.findViewById<TextView>(R.id.claims_header_amount)
        internal var historyHeader = itemView.findViewById<TextView>(R.id.claims_header_history)
        internal var statusHeader = itemView.findViewById<TextView>(R.id.claims_header_status)

    }

    class ClaimsTypeChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var layout =
            itemView.findViewById<ConstraintLayout>(R.id.claimtypes_item_child_container)
        internal var claimsAmount: TextView = itemView.findViewById(R.id.claims_amount)
        internal var claimsHistory: TextView = itemView.findViewById(R.id.claims_history)
        internal var claimsStatus: TextView = itemView.findViewById(R.id.claims_status)
        internal var claimsAmend: Button = itemView.findViewById(R.id.buttonClaimsAmend)
        internal var claimsDelete: Button = itemView.findViewById(R.id.buttonClaimsDelete)

        //Insert buttons here maybe (I think)
    }
}