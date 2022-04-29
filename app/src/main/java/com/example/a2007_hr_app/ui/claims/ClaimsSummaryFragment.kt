package com.example.a2007_hr_app.ui.claims

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2007_hr_app.NavigationActivity
import com.example.a2007_hr_app.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ClaimsSummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ClaimsSummaryFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var claimsSummaryViewModel: ClaimsSummaryViewModel
    lateinit var expandableView : View
    var countryStateExpandableAdapter : ClaimsSummaryAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        initViewModel()
        (activity as NavigationActivity).supportFragmentManager.setFragmentResultListener("edit",this,
        ){
                key, bundle->
            val result = bundle.getBoolean("editChoice")
            if (result){
                claimsSummaryViewModel.obtainClaimTypes()
                observeViewModelResponse()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        expandableView = inflater.inflate(R.layout.fragment_claims_summary, container, false)

        return expandableView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        claimsSummaryViewModel.obtainClaimTypes()
        observeViewModelResponse()
    }

    private fun observeViewModelResponse(){
        claimsSummaryViewModel.obtainClaimsResponse.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it){
                    is ResultOf.Success -> {
                        val countryStateInfo =  claimsSummaryViewModel.prepareDataForExpandableAdapter(it.value)
                        populateAdapterWithInfo(countryStateInfo)
                    }

                    is ResultOf.Failure -> {
                        val failedMessage =  it.message ?: "Unknown Error"
                        println("Failed Message $failedMessage")
                    }

                }
            }
        })
    }

    private fun populateAdapterWithInfo(expandableCountryStateList : MutableList<ExpandableClaimTypeModel>){
        countryStateExpandableAdapter = ClaimsSummaryAdapter(this@ClaimsSummaryFragment.requireActivity(),
            expandableCountryStateList,(activity as NavigationActivity).supportFragmentManager)
        countryStateExpandableAdapter?.let {
            val layoutManager = LinearLayoutManager(context)
            val recyclerView = view?.findViewById<RecyclerView>(R.id.claim_types_rv)

            recyclerView?.layoutManager = layoutManager
            recyclerView?.adapter = it
            recyclerView?.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
            it.notifyDataSetChanged()
        }

    }

    private fun initViewModel(){
        var claimsSummaryViewModelFactory = ClaimsSummaryViewModelFactory()
        claimsSummaryViewModel = ViewModelProvider(this,claimsSummaryViewModelFactory).get(ClaimsSummaryViewModel::class.java)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ClaimsSummaryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}