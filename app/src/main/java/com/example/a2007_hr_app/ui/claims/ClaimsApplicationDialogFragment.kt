package com.example.a2007_hr_app.ui.claims

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import com.example.a2007_hr_app.BuildConfig
import com.example.a2007_hr_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ClaimsApplicationDialogFragment : DialogFragment() {

    private lateinit var viewModel: ClaimsApplicationDialogViewModel

    companion object {
        fun newInstance() = ClaimsApplicationDialogFragment()
        var TAG = ClaimsApplicationDialogFragment::class.simpleName
        const val APPLY_MODE = 0
        const val EDIT_MODE = 1
        const val CAPTURE_PHOTO = 1
        const val CHOOSE_PHOTO = 2
        var OPERATION_MODE = 0
        lateinit var claimData: ClaimsModel.ClaimType.ClaimDetail
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    lateinit var mainDialog: Dialog
    lateinit var btnSubmit: Button
    lateinit var btnClear: Button
    lateinit var claimAmount: EditText
    lateinit var claimReason: EditText
    lateinit var claimType: Spinner
    lateinit var claimBalance: TextView
    lateinit var claimValues: StringBuilder
    private lateinit var btnCapture: Button
    private lateinit var btnChoose : Button
    private lateinit var titleHeader: TextView

    private val claimsRepo = ClaimsRepo()
    private var mImageView: ImageView? = null
    private var mUri: Uri? = null
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference
    lateinit var photoUUID: UUID
    lateinit var photoFileName: String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_claims_application, container, false)
        viewModel = ViewModelProvider(this).get(ClaimsApplicationDialogViewModel::class.java)

        titleHeader = view.findViewById(R.id.textViewClaimsApplyTitle)
        btnSubmit = view.findViewById(R.id.buttonClaimsSubmitDialog)
        btnClear = view.findViewById(R.id.buttonClaimsClearDialog)
        claimAmount = view.findViewById(R.id.editTextClaimAmountDialog)
        claimReason = view.findViewById(R.id.editTextClaimsReasonDialog)
        claimBalance = view.findViewById(R.id.textViewAmountLeftDialog)


        btnCapture = view.findViewById(R.id.buttonUploadDialog)
        btnChoose = view.findViewById(R.id.buttonGalleryDialog)
        mImageView = view.findViewById(R.id.imageViewPreview)

        claimValues = StringBuilder()

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        photoUUID = UUID.randomUUID()


        /**
         * Request camera permissions if not granted.
         */
        if (!allPermissionsGranted()) {
            requestPermissions(REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS)
        }


        claimType = view.findViewById(R.id.spinnerClaimTypesDialog)
        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.ClaimTypes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            claimType.adapter = adapter
         }

        claimType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                claimType.setSelection(0)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var temp = claimType.selectedItem
                getResponseUsingLiveData(temp.toString())
            }
        }

        if(OPERATION_MODE==EDIT_MODE){
            titleHeader.text = "Amending Claim"
            val typeList = resources.getStringArray(R.array.ClaimTypes)
            val posit = typeList.indexOf(claimData.claimType)

            photoFileName = claimData.claimFile

            storageReference = storage.reference.child("myImages/$photoFileName")

            val localFile = File.createTempFile("tempImage","jpg")
            storageReference.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                mImageView?.setImageBitmap(bitmap)

            }.addOnFailureListener{
                show("Image get failed")
            }

            claimType.setSelection(posit)
            claimAmount.setText(claimData.claimAmount.toString())
            claimReason.setText(claimData.claimReason)
            claimType.isEnabled = false
        }
        else{
            titleHeader.text = "Apply for new Claim"
            Log.d(TAG, "APPLY MODE")
        }

        btnSubmit.setOnClickListener {
            if(checkIfFilled(claimAmount, claimReason) && checkAmountBalance()){
                when(OPERATION_MODE){
                    APPLY_MODE -> {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss")
                        val formatted = current.format(formatter)

                        val claim = ClaimsModel.ClaimType.ClaimDetail(
                            claimAmount.text.toString().toDouble(),
                            formatted.toString(),
                            "Pending",
                            claimType.selectedItem.toString(),
                            claimReason.text.toString(),
                            photoFileName,
                        )

                        claimValues.append("Claim Type: ${claimType.selectedItem}\n" +
                                "Amount to claim: ${claimAmount.text}\n" +
                                "Claim reason: ${claimReason.text}"
                        )
                        showValuesDialog(claim)
                    }
                    EDIT_MODE -> {
                        val claim = ClaimsModel.ClaimType.ClaimDetail(
                            claimAmount.text.toString().toDouble(),
                            claimData.claimDateTime,
                            "Pending",
                            claimType.selectedItem.toString(),
                            claimReason.text.toString(),
                            photoFileName,
                        )

                        claimValues.append("Claim Type: ${claimType.selectedItem}\n" +
                                "Amount to claim: ${claimAmount.text}\n" +
                                "Claim reason: ${claimReason.text}"
                        )
                        showValuesDialog(claim)
                    }
                }
            }
        }

        btnClear.setOnClickListener {
            clearFields()
        }

        btnCapture.setOnClickListener{capturePhoto()}
        btnChoose.setOnClickListener{
            //check permission at runtime
            val checkSelfPermission = context?.let { it1 ->
                ContextCompat.checkSelfPermission(
                    it1,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED){
                //Requests permissions to be granted to this application at runtime
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
            else{
                openGallery()
            }
        }
        return view
    }

    /*******************************************
     * Camera/Gallery Upload Functions
     *******************************************/


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun uploadImage(){
        if(mUri != null){
            val ref = storageReference?.child("myImages/claims-$photoUUID")
            ref?.putFile(mUri!!)

        }else{
            Toast.makeText(context, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun show(message: String) {
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }

    private fun capturePhoto(){
        val capturedImage = File(context?.externalCacheDir, "claims-$photoUUID")
        if(capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if(Build.VERSION.SDK_INT >= 24){
            context?.let {
                FileProvider.getUriForFile(
                    it, BuildConfig.APPLICATION_ID + ".fileprovider",
                    capturedImage)
            }
        } else {
            Uri.fromFile(capturedImage)
        }

        Log.d(TAG, "URI = $mUri")
        Log.d(TAG, "capturedImage = $capturedImage")
        photoFileName = capturedImage.toString().substringAfterLast('/')
        Log.d(TAG, "File Name = $photoFileName")


        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, CAPTURE_PHOTO)
    }
    private fun openGallery(){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, CHOOSE_PHOTO)
    }

    //Display image preview
    private fun renderImage(imagePath: String?){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            mImageView?.setImageBitmap(bitmap)
            show("$imagePath")
        }
        else {
            show("ImagePath is null")
        }
    }

    @SuppressLint("Range")
    private fun getImagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = uri?.let { context?.contentResolver?.query(it, null, selection, null, null ) }


        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        mUri = uri

        photoFileName = "claims-$photoUUID"

        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(context, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri?.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri?.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse(
                    "content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = getImagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri?.scheme, ignoreCase = true)){
            imagePath = getImagePath(uri, null)
        }
        else if ("file".equals(uri?.scheme, ignoreCase = true)){
            imagePath = uri?.path
        }
        renderImage(imagePath)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAPTURE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeStream(
                        mUri?.let { context?.contentResolver?.openInputStream(it) })
                    mImageView!!.setImageBitmap(bitmap)
                }
            CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data)
                    }
                }
        }
    }

    /*******************************************
     * Claims Logic Functions
     *******************************************/


    private fun getResponseUsingLiveData(claimType: String) {
        viewModel.getResponseUsingLiveData(claimType).observe(this) {
            claimBalance.text = it.toString()
        }
    }

    private fun checkAmountBalance() : Boolean{
        val amountToCheck = claimBalance.text.toString().toDouble()
        val input = claimAmount.text.toString().toDouble()

        return if(amountToCheck>input){
            //Success
            true
        } else{
            claimAmount.error = "Insufficient balance amount."
            false
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()
        mainDialog = dialog!!
        mainDialog.window?.setLayout(width, height)
    }

    private fun checkIfFilled(amount: EditText, reason: EditText): Boolean{
        var amountBool = false
        var reasonBool = false
        var photoBool = false

        if(photoFileName!=null){
            photoBool = true
        }
        else{
            show("No file uploaded. Please upload a file of proof.")
        }
        if(amount.text.isNullOrEmpty()){
            amount.error = "Please enter an amount."
        }
        else{
            amountBool = true
        }

        if(reason.text.isNullOrEmpty()){
            reason.error = "Please enter a valid reason."
        }
        else{
            reasonBool = true
        }
        return amountBool && reasonBool && photoBool
    }


    private fun clearFields(){
        if(claimAmount.text.isNotEmpty()
            || claimReason.text.isNotEmpty() || claimType.selectedItemId>0){
            claimAmount.setText("")
            claimReason.setText("")
            claimType.setSelection(0)
        }
    }

    private fun showValuesDialog(claim: ClaimsModel.ClaimType.ClaimDetail){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Claims Application Confirmation")
            .setMessage("${claimValues}\nWould you like to submit?")
            .setNeutralButton("Amend") {dialog, which ->
                //DO SOMETHING
                Toast.makeText(context, "Amending", Toast.LENGTH_LONG).show()
            }
            .setPositiveButton("Yes"){dialog, which ->
                uploadImage()
                claimsRepo.writeClaim(claim)
                claimValues.clear()
                if(OPERATION_MODE == EDIT_MODE){
                    setFragmentResult("edit", bundleOf("editChoice" to true))
                }
                show("Application submitted!")
                dismiss()
                Log.d(TAG, "Complete")
            }
            .show()
    }
}