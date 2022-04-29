package com.example.a2007_hr_app


import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.a2007_hr_app", appContext.packageName)
    }

    @Test
    fun onLaunch_HomeFragment() {
        /**
         * Check Home Fragment Ui Elements and Clickable
         */
        launch(NavigationActivity::class.java).use {
            onView(withId(R.id.nav_home)).perform(click())
            onView(withId(R.id.textViewWelcome)).check(matches(isDisplayed()))
            onView(withId(R.id.cardViewLeave)).check(matches(isDisplayed()))
            onView(withId(R.id.viewBlueBar)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewLeaveBalance)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewAnnualLeaves)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewMedicalLeave)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewCompassionateLeave)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewChildcareLeave)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewAnnualBalance)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewMedicalLeaveBalance)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewCompassionateLeaveBalance)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewChildcareLeaveBalance)).check(matches(isDisplayed()))
            onView(withId(R.id.cardViewClaims)).check(matches(isDisplayed()))
            onView(withId(R.id.viewBlueBar2)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewClaims)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewMedicalClaims)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewTransportClaims)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewOtherClaims)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewMedicalClaimsAmount)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewTransportClaimsAmount)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewOtherClaimsAmount)).check(matches(isDisplayed()))
            onView(withId(R.id.cardViewAttendance)).check(matches(isDisplayed()))
            onView(withId(R.id.viewRedBar)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewAttendanceHeader)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewSigned)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewAttendanceHistory)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewAttendanceDate)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewCheckAttendance)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewLocation)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewLogout)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun onLaunch_LeaveApplicationDialog() {
        /**
         * Check Leave Application Popup UI Elements
         * and also perform application of leave
         */
        launch(NavigationActivity::class.java).use {
            //Launching Dialog
            onView(withId(R.id.speed_dial)).perform(click())
            onView(withId(R.id.new_leave)).perform(click())

            //Checking views exist
            onView(withId(R.id.textViewLeaveApplyTitle)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewLeaveTypeDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewLeaveStartDate)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewLeaveEndDate)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewSupervisor)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewCovering)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewReason)).check(matches(isDisplayed()))
            onView(withId(R.id.buttonClearLA)).check(matches(isDisplayed()))
            onView(withId(R.id.spinnerLeaveTypesDialog)).check(matches(isDisplayed()))

            //Able to choose between toggle buttons
            onView(withId(R.id.toggleButtonLeaveStartAM)).perform(click())
            onView(withId(R.id.toggleButtonLeaveStartPM)).perform(click())
            onView(withId(R.id.toggleButtonLeaveStartAM)).perform(click()) //Choose AM as final
            onView(withId(R.id.toggleButtonLeaveEndAM)).perform(click())
            onView(withId(R.id.toggleButtonLeaveEndPM)).perform(click())

            //Perform normal application
            onView(withId(R.id.editTextLeaveStartDate)).perform(click())
            onView(isAssignableFrom(DatePicker::class.java))
                .perform(setDate(2022, 4, 9))
            onView(withId(android.R.id.button1)).perform(click())
            onView(withId(R.id.editTextLeaveEndDate)).perform(click())
            onView(isAssignableFrom(DatePicker::class.java))
                .perform(setDate(2022, 4, 10))
            onView(withId(android.R.id.button1)).perform(click())
            onView(withId(R.id.editTextLeaveSupervisor)).perform(typeText("Supervisor"))
            onView(withId(R.id.editTextLeaveCovering)).perform(typeText("Cover"))
            onView(withId(R.id.editTextLeaveReason)).perform(typeText("Test"))
            Espresso.closeSoftKeyboard()
            onView(withId(R.id.buttonSubmitLA)).perform(click())
            onView(withText("Yes")).perform(click())
            onView(withText("Yes")).perform(click())
        }
    }

    @Test
    fun onLaunch_ClaimApplicationDialog() {
        /**
         * Check Claims Application Popup UI Elements
         */
        launch(NavigationActivity::class.java).use {
            onView(withId(R.id.speed_dial)).perform(click())
            onView(withId(R.id.new_claim)).perform(click())

            //Displays
            onView(withId(R.id.textViewClaimsApplyTitle)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewClaimTypeDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewAmountBalanceDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewAmountToClaimDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewClaimReasonDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewUploadDocDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.textViewClaimsFileURLDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.buttonClaimsSubmitDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.buttonClaimsClearDialog)).check(matches(isDisplayed()))
            onView(withId(R.id.spinnerClaimTypesDialog)).check(matches(isDisplayed()))

            //Clicks

            onView(withId(R.id.editTextClaimAmountDialog)).perform(typeText("15.5"))
            onView(withId(R.id.editTextClaimsReasonDialog)).perform(typeText("Polyclinic visit"))
            Espresso.closeSoftKeyboard()
            pressBack()
        }
    }

    @Test
    fun onLaunch_LeaveStatus() {
        /**
         * Check Leave Summary Fragment UI Elements
         */
        launch(NavigationActivity::class.java).use {
            //Switch to leave
            onView(withId(R.id.nav_leaves)).perform(click())

            //Check RecyclerView works by checking categories show up
//            onView(withText("Medical")).check(matches(isDisplayed()))
//            onView(withText("Annual")).check(matches(isDisplayed()))
//            onView(withText("Childcare")).check(matches(isDisplayed()))
//            onView(withText("Compassionate")).check(matches(isDisplayed()))

            //Open categories
            onView(withId(R.id.leaveRecyclerView))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Medical")), click()))
            onView(withId(R.id.leaveRecyclerView))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Annual")), click()))
            onView(withId(R.id.leaveRecyclerView))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Childcare")), click()))
            onView(withId(R.id.leaveRecyclerView))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Compassionate")), click()))

            //Close categories
            onView(withId(R.id.leaveRecyclerView))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Medical")), click()))
            onView(withId(R.id.leaveRecyclerView))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Annual")), click()))
            onView(withId(R.id.leaveRecyclerView))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Childcare")), click()))
            onView(withId(R.id.leaveRecyclerView))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Compassionate")), click()))
        }
    }

    @Test
    fun onLaunch_ClaimStatus() {
        /**
         * Check Leave Summary Fragment UI Elements
         */
        launch(NavigationActivity::class.java).use {
            //Switch to claim
            onView(withId(R.id.nav_claims)).perform(click())

            //Open then close categories
            onView(withId(R.id.claim_types_rv))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Medical")), click()))
            onView(withId(R.id.claim_types_rv))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Medical")), click()))

            onView(withId(R.id.claim_types_rv))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Transport")), click()))
            onView(withId(R.id.claim_types_rv))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Transport")), click()))

            onView(withId(R.id.claim_types_rv))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Others")), click()))
            onView(withId(R.id.claim_types_rv))
                .perform(actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Others")), click()))
        }
    }

    @Test
    fun onLaunch_QRScanner() {
        /**
         * Check QR Code Scanner Ui Elements and Clickable
         */
        launch(NavigationActivity::class.java).use {
            onView(withId(R.id.nav_home)).perform(click())
            onView(withId(R.id.textViewCheckAttendance)).perform(click())
            onView(withId(R.id.textViewQRHeader)).check(matches(isDisplayed()))
            onView(withId(R.id.viewQRBox)).check(matches(isDisplayed()))
            pressBack()
        }
    }

    @Test
    fun onLaunch_AttendanceHistory() {
        /**
         * Check Attendance History Ui Elements and Clickable
         */
        launch(NavigationActivity::class.java).use {
            onView(withId(R.id.nav_home)).perform(click())
            onView(withId(R.id.textViewAttendanceHistory)).perform(click())
            onView(withId(R.id.textViewAttendanceHistoryHeader)).check(matches(isDisplayed()))
            onView(withId(R.id.RecyclerViewAttendance)).check(matches(isDisplayed()))
            pressBack()
        }
    }

    @Test
    fun onLaunch_LogoutThenLogin() {
        /**
         * This test will fail if user is not logged in.
         * Check the Logout and Login Functions.
         */
        launch(NavigationActivity::class.java).use {
            onView(withId(R.id.nav_home)).perform(click())
            onView(withId(R.id.textViewLogout)).perform(click())
            onView(withId(R.id.editText_username)).perform(typeText("devUser@devmail.com"))
            onView(withId(R.id.editText_password)).perform(typeText("devUser"))
            onView(withId(R.id.button_login)).perform(click())
        }


    }
}
