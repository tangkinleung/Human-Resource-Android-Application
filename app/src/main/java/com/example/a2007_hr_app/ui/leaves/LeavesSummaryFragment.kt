package com.example.a2007_hr_app.ui.leaves

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2007_hr_app.NavigationActivity
import com.example.a2007_hr_app.R
import com.example.a2007_hr_app.ui.claims.ResultOf

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LeaveApplicationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LeavesSummaryFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    lateinit var leavesViewModel: LeavesViewModel
    lateinit var expandableView: View
    var expandableLeaveTypeAdapter: LeavesSummaryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(com.example.a2007_hr_app.ui.leaves.ARG_PARAM1)
            param2 = it.getString(com.example.a2007_hr_app.ui.leaves.ARG_PARAM2)
        }
        initVM()
        (activity as NavigationActivity).supportFragmentManager.setFragmentResultListener("edit",this,
        ){
            key, bundle->
            val result = bundle.getBoolean("editChoice")
            if (result){
                leavesViewModel.obtainLeaveTypes()
                observeViewModelResponse()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        expandableView = inflater.inflate(R.layout.fragment_leaves_summary,container,false)

        return expandableView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leavesViewModel.obtainLeaveTypes()
        observeViewModelResponse()
    }

    private fun observeViewModelResponse(){
        leavesViewModel.obtainLeaveSummaryResponse.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it){
                    is ResultOf.Success->{
                        val leaveInfo = leavesViewModel.prepareDataForExpandableAdapter(it.value)
                        populateAdapterWithInfo(leaveInfo)
                    }
                    is ResultOf.Failure -> {
                        val failedMessage =  it.message ?: "Unknown Error"
                        println("Failed Message $failedMessage")
                    }
                }
            }
        })
    }

    private fun populateAdapterWithInfo(expandableLeaveTypeList:MutableList<ExpandableLeaveTypeModel>){
        expandableLeaveTypeAdapter = LeavesSummaryAdapter(this@LeavesSummaryFragment.requireActivity(),
        expandableLeaveTypeList,(activity as NavigationActivity).supportFragmentManager)
        expandableLeaveTypeAdapter?.let {
            val layoutManager = LinearLayoutManager(context)
            val recyclerView = view?.findViewById<RecyclerView>(R.id.leaveRecyclerView)

            recyclerView?.layoutManager = layoutManager
            recyclerView?.adapter = it
            recyclerView?.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            it.notifyDataSetChanged()
        }
    }

    private fun initVM(){
        val leavesVMFactory = LeavesViewModelFactory()
        leavesViewModel = ViewModelProvider(this, leavesVMFactory).get(LeavesViewModel::class.java)
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LeavesSummaryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}