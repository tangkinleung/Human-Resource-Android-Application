package com.example.a2007_hr_app

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.a2007_hr_app.databinding.ActivityNavigationBinding
import com.example.a2007_hr_app.ui.claims.ClaimsApplicationDialogFragment
import com.example.a2007_hr_app.ui.leaves.LeaveApplicationDialogFragment
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_navigation)
        setContentView(binding.root)

//        val name = intent.extras?.getString("Name").toString()

        val bottomNavbar = binding.bottomNavbar
        val navController = findNavController(R.id.navigation_fragment_container)

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_leaves, R.id.nav_claims)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        bottomNavbar.setupWithNavController(navController)

        //Removes the title bars for all fragments.
        supportActionBar?.hide()

        /**
         *  Floating Speed Dial
         */
        val speedDial = binding.speedDial
        speedDial.addActionItem(
            //Add apply leave button
            SpeedDialActionItem.Builder(R.id.new_leave, R.drawable.ic_leaves)
                .setFabBackgroundColor(Color.WHITE)
                .setLabel("Apply leave")
                .setLabelBackgroundColor(Color.WHITE)
                .setLabelColor(Color.BLACK)
                .create()
        )
        speedDial.addActionItem(
            //Add apply claim button
            SpeedDialActionItem.Builder(R.id.new_claim, R.drawable.ic_claims)
                .setFabBackgroundColor(Color.WHITE)
                .setLabel("Apply claim")
                .setLabelBackgroundColor(Color.WHITE)
                .setLabelColor(Color.BLACK)
                .create()
        )

        speedDial.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.new_leave -> {
                    LeaveApplicationDialogFragment.OPERATION_MODE = 0
                    LeaveApplicationDialogFragment().show(supportFragmentManager,"This")
                    speedDial.close()
                    return@OnActionSelectedListener true
                }
                R.id.new_claim -> {
                    ClaimsApplicationDialogFragment.OPERATION_MODE = 0
                    ClaimsApplicationDialogFragment().show(supportFragmentManager, "This")
                    speedDial.close()
                    return@OnActionSelectedListener true
                }
            }
            false
        })
    }
}