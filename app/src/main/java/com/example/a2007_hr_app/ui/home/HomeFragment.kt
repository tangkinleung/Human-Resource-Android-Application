/**
 * Home Screen to display summary of information
 * Author: WongJunHao
 * References: CodeLabs and Android documentation
 */
package com.example.a2007_hr_app.ui.home


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.a2007_hr_app.LoginScreenActivity
import com.example.a2007_hr_app.R
import com.example.a2007_hr_app.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


const val TAG = "HomeFragment"

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    private var userID: String = ""
    private var database: FirebaseDatabase = Firebase.database(
        "https://mad-hr-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val homeView: HomeViewModel by viewModels()

    //SharePref to be init during onCreateView
    private var sharedPref: SharedPreferences? = null
    private var sharedPrefListener: OnSharedPreferenceChangeListener? = null

    /**
     * Get Shared Preference from Context. Must be declare before onCreateView
     * @param: Context
     * @return: SharedPreferences
     * @note: I tried to the normal way and it didn't work so i reference one from stackoverflow
     */
    private fun getPrefs(context: Context): SharedPreferences? {
        return context.getSharedPreferences(
            "com.example.a2007_hr_app.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        val user = auth.currentUser
        sharedPref = context?.let { it1 -> getPrefs(it1) }

        /**
         * Listener executes when sharedPref data change.
         * Mainly calling check-in and check-out based the value stored in the sharedPreferences key
         */
        sharedPrefListener =
            OnSharedPreferenceChangeListener { sharedPref, key ->
                if (key.equals("QR_Location")) {
                    Log.d("HomeFragment_PrefListener", "Fired")
                    val qrText = sharedPref.getString("QR_Location", "None")
                    if (qrText == "None") {
                        homeView.checkOut()
                    } else {
                        qrText?.let { it ->
                            homeView.checkIn(it)
                        }
                    }

                }
            }

        /**
         * Depending on the value in the sharedPref key "QR_Location" either call the QR_Scanner to
         * check in or Change the value in the key to "None" to check-out
         *
         * SharedPrefListener will be invoked to handle the checked status
         */
        binding.textViewCheckAttendance.setOnClickListener {
            val qrText = sharedPref?.getString("QR_Location", "None")

            /**
             * Call QRCameraActivity if not checked-in
             */
            if (qrText == "None") {
                findNavController().navigate(R.id.action_nav_home_to_nav_camera)
            }
            /**
             * Set Value in sharedPref as "None" to checked-out
             */
            else {
                with(sharedPref!!.edit()) {
                    putString("QR_Location", "None")
                    apply()
                }
            }
        }

        /**
         * Replace current fragment with AttendanceActivity when History-> is clicked.
         */
        binding.textViewAttendanceHistory.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_nav_attendance)
        }

        /**
         * Logout user to LoginScreenActivity and Clear navigation stack
         */
        binding.textViewLogout.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(activity, LoginScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        /**
         * Get User display name from fire base and set it to welcome message
         */
        user?.let {
            userID = user.uid
            val userName = user.displayName.toString()
            homeView.setWelcomeText(userName)
        }


        /**
         * Database Listener
         * Leave Balance
         */
        val leaveBalanceRef = database.getReference("Users/$userID/Leaves/LeavesBalanceAmount/")
        val leaveBalanceListener = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _binding?.let {
                    binding.textViewAnnualBalance.text =
                        "${dataSnapshot.child("annualBalance").value} days left"
                    binding.textViewMedicalLeaveBalance.text =
                        "${dataSnapshot.child("medicalBalance").value} days left"
                    binding.textViewCompassionateLeaveBalance.text =
                        "${dataSnapshot.child("compassionateBalance").value} days left"
                    binding.textViewChildcareLeaveBalance.text =
                        "${dataSnapshot.child("childcareBalance").value} days left"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        leaveBalanceRef.addValueEventListener(leaveBalanceListener)
        /**
         * Claims Balance
         */
        val claimsBalanceRef = database.getReference("Users/$userID/Claims/ClaimsBalanceAmount/")
        val claimsBalanceListener = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _binding?.let {
                    binding.textViewMedicalClaimsAmount.text =
                        "$${dataSnapshot.child("medicalBalance").value}"
                    binding.textViewTransportClaimsAmount.text =
                        "$${dataSnapshot.child("transportBalance").value}"
                    binding.textViewOtherClaimsAmount.text =
                        "$${dataSnapshot.child("othersBalance").value}"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        claimsBalanceRef.addValueEventListener(claimsBalanceListener)

        /**
         * LiveData Observers
         */
        val welcomeObserver = Observer<String> {
            binding.textViewWelcome.text = it
        }
        homeView.welcomeText.observe(viewLifecycleOwner, welcomeObserver)

        val checkedInObserver = Observer<String> {
            binding.textViewSigned.text = it
        }
        homeView.checkedText.observe(viewLifecycleOwner, checkedInObserver)

        val checkedClickableObserver = Observer<String> {
            binding.textViewCheckAttendance.text = it
        }
        homeView.checkedClickable.observe(viewLifecycleOwner, checkedClickableObserver)

        val checkedStatusObserver = Observer<Boolean> {
            if (it) {
                //Change the Color of the card bar and checked-in text
                binding.textViewSigned.setTextColor(Color.parseColor("#FF669900"))
                binding.viewRedBar.setBackgroundColor(Color.parseColor("#FF669900"))
            } else {
                //Change the Color of the card bar and checked-in text
                binding.textViewSigned.setTextColor(Color.parseColor("#FFCC0000"))
                binding.viewRedBar.setBackgroundColor(Color.parseColor("#FFCC0000"))

            }
        }
        homeView.checkedStatus.observe(viewLifecycleOwner, checkedStatusObserver)

        val dateTimeObserver = Observer<String> {
            binding.textViewAttendanceDate.text = it
        }
        homeView.dateTimeText.observe(viewLifecycleOwner, dateTimeObserver)

        val qrLocationObserver = Observer<String> {
            binding.textViewLocation.text = it
        }
        homeView.qrLocation.observe(viewLifecycleOwner, qrLocationObserver)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        sharedPref?.registerOnSharedPreferenceChangeListener(sharedPrefListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPref?.unregisterOnSharedPreferenceChangeListener(sharedPrefListener)
        _binding = null
    }
}