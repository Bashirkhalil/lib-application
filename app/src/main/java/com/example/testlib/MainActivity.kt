package com.example.testlib

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.myapplicationtest.R
import com.example.myapplicationtest.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class MainActivity : AppCompatActivity() {


    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (isDeviceAbove()) {
        arrayListOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }


    companion object {
        private const val STORAGE_PERMISSION_CODE = 1000 // Define your request code here
        private lateinit var bindingTst: ActivityMainBinding
        private var isLibLoaded = false
    }

    private lateinit var binding: ActivityMainBinding


    private var context : Context = this
    private var validURL = ""

    private fun isDeviceAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun requestManageExternalStoragePermission() {
        if (isDeviceAbove()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + applicationContext.packageName)
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, 2296)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2296)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        bindingTst = binding
        setContentView(binding.root)

        binding.title.text = "Device architecture : ${getArchType()}"

        binding.buttonDatePicker.setOnClickListener {
            showDatePickerDialog()
        }

        requestManageExternalStoragePermission()


        binding.download.setOnClickListener {

//            if (binding.liburl.text.isEmpty()){
//                showMessage("URL is empty please add valid url !")
//                return@setOnClickListener
//            }
//
//            if (isValidHttpsUrl(binding.liburl.text.toString()).not()){
//                showMessage("URL is Not Valid please enter valid url ! ")
//                return@setOnClickListener
//            }
            var url = when (getArchType()) {
                "arm64-v8a" -> "https://drive.google.com/uc?export=download&id=1pA9cPk-FDVeTSoyFGh7_w3ET8CAj3tLD"
                "armeabi-v7a" -> "https://drive.google.com/uc?export=download&id=1vwSZU_Z5cKxo4K3lO1w8LxEoDtgm6hOJ"
                "x86_64" -> "https://drive.google.com/uc?export=download&id=1a0DUHz6AtQkQHFkM317KCzmwFaNPSNwn"
                else -> ""
            }


            validURL =url //  binding.liburl.text.toString();

            if (isDeviceAbove()){
                val mlist = checkMultiplePermission()
                if (mlist.isEmpty()) {
                    doOperation()
                }else{
                    ActivityCompat.requestPermissions(this, mlist.toTypedArray(), multiplePermissionId)
                }
            }else{
                requestStoragePermissions(this)
            }



        }
    }


    private fun showDatePickerDialog() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(binding.textViewDate.text.toString()) ?: Calendar.getInstance().time
        val calendar = Calendar.getInstance().apply { time = date }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            binding.textViewDate.text = selectedDate

            if (isLibLoaded){
                val dateOfb = "$selectedYear-${(selectedMonth+1)}-$selectedDay"
                Log.e("TAG","date is please check $dateOfb")
                // binding.sampleText.text = HelperQuestion.stringFromJNI()
                val answer_is = HelperQuestion.enterBornYear(dateOfb)
                // val answer_is = HelperQuestion.enterBornYear("1995-12-30");
                bindingTst.sampleText.text = "Answer is : $answer_is"
            }

        }, year, month, day)

        datePickerDialog.show()
    }

    private fun doOperation() {

        if (isDeviceAbove() && Environment.isExternalStorageManager().not()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + applicationContext.packageName)
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, 2296)
            }
            return
        }

        checkLiv()
        showMessage("All Permission Granted Successfully!")
    }

    fun isValidHttpsUrl(url: String): Boolean {
        val regex = Regex("^(https?://)([\\w\\-]+\\.)+[\\w\\-]+(/\\S*)?$")
        return regex.matches(url) && url.startsWith("https://")
    }


    private fun  showMessage(msg : String ) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    private fun checkMultiplePermission(): ArrayList<String> {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            return listPermissionNeeded
        }
        return listPermissionNeeded
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (isDeviceAbove()){

            if (requestCode == multiplePermissionId) {
                if (grantResults.isNotEmpty()) {
                    var isGrant = true
                    for (element in grantResults) {
                        if (element == PackageManager.PERMISSION_DENIED) {
                            isGrant = false
                        }
                    }
                    if (isGrant) {
                        if(checkMultiplePermission().isEmpty()){
                            doOperation()
                        }
                    } else {
                        var someDenied = false
                        for (permission in permissions) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                    this,
                                    permission
                                )
                            ) {
                                if (ActivityCompat.checkSelfPermission(
                                        this,
                                        permission
                                    ) == PackageManager.PERMISSION_DENIED
                                ) {
                                    someDenied = true
                                }
                            }
                        }
                        if (someDenied) {
                            // here app Setting open because all permission is not granted
                            // and permanent denied
                            appSettingOpen(this)
                        } else {
                            // here warning permission show
                            warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                                when (which) {
                                    DialogInterface.BUTTON_POSITIVE ->
                                        checkMultiplePermission()
                                }
                            }
                        }
                    }
                }
            }

        }else {

            if (requestCode == STORAGE_PERMISSION_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    doOperation()
                } else {
                    Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT
                    ).show()
                }}
        }

    }

    fun appSettingOpen(context: Context){
        showMessage("Go to Setting and Enable All Permission")
        val settingIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        settingIntent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(settingIntent)
    }

    fun warningPermissionDialog(context: Context,listener : DialogInterface.OnClickListener) =
        MaterialAlertDialogBuilder(context)
            .setMessage("All Permission are Required for this app")
            .setCancelable(false)
            .setPositiveButton("Ok",listener)
            .create()
            .show()



    private fun checkLiv() {

        //  normal()

        val sourceFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "libraryfile.so")
        if (sourceFile.exists()){
             showMessage("File is already downloaded ")
            sourceFile.delete()
        }

        // if(sourceFile.exists().not()){

            bindingTst.layoutData.visibility =View.GONE
            bindingTst.layoutB.visibility = View.VISIBLE

            val downloader=AndroidDownloader(context)
                downloader.downloadFile(validURL)
            // downloader.downloadFile("https://drive.google.com/uc?export=download&id=1a0DUHz6AtQkQHFkM317KCzmwFaNPSNwn")
            // downloader.downloadFile("https://drive.google.com/uc?export=download&id=1teGpbSZM5sBWOPCX0fs0EbUlvybN1kxA")

      //  }else{
        //    showMessage("File is already downloaded ")
           // loadtheLib()
        //}



    }

    private fun loadtheLib() {
        val sourceFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "libraryfile.so")

        val destinationFile = File(context!!.filesDir, "libLibraryDownload.so")  // Internal storage path

        if (sourceFile.exists() ) {
            sourceFile.copyTo(destinationFile, overwrite = true)
        }

        bindingTst.layoutData.visibility = View.VISIBLE
        bindingTst.layoutB.visibility = View.GONE

        //  try {

        System.load(destinationFile.absolutePath)

        // binding.sampleText.text = HelperQuestion.stringFromJNI()
        //val answer_is = HelperQuestion.enterBornYear(bindingTst.textViewDate.toString().trim())
        val answer_is = HelperQuestion.enterBornYear("1995-12-30");
        bindingTst.sampleText.text = "Answer is : $answer_is"

        isLibLoaded = true ;
        bindingTst.buttonDatePickerLayout.visibility =View.VISIBLE
    }

    fun getArchType(): String {
        return when (Build.SUPPORTED_ABIS[0]) {
            "arm64-v8a" -> "arm64-v8a"
            "armeabi-v7a" -> "armeabi-v7a"
            "x86_64" -> "x86_64"
            else -> throw IllegalArgumentException("Unsupported architecture")
        }
    }

    private fun downloadImage() {
        // Create request for Android DownloadManager
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request("https://drive.google.com/uc?export=download&id=1VV5wdoqYKyjD1QZUOfJDkndV06V_jocS".toUri())
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("DataDownloadTest")
        request.setDescription("Android Data download using DownloadManager.")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloadfileName.png") // Ensure the filename ends with .png
        request.setMimeType("image/png") // Set MIME type to PNG
        downloadManager.enqueue(request)
    }

    private fun downloadSofile() {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request("https://drive.google.com/uc?export=download&id=1teGpbSZM5sBWOPCX0fs0EbUlvybN1kxA".toUri())
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("LibraryDownload")
        request.setDescription("Downloading compiled C++ library (.so file).")
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "libraryfile.so") // Ensure the filename ends with .so
        request.setMimeType("application/octet-stream") // Set MIME type to binary file
        downloadManager.enqueue(request)
    }

    private fun normal(){
        HelperQuestion.ini()
        //System.load(destination.absolutePath)
        // Example of a call to a native method
        // binding.sampleText.text = HelperQuestion.stringFromJNI()
        val answer_is = HelperQuestion.enterBornYear("1995-05-05");
        binding.sampleText.text = "Answer is : $answer_is"
    }

    private fun requestStoragePermissions(activity: Activity) {

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            doOperation()
        }else{
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        }
    }



    interface Downloader { fun downloadFile(url:String,fileSubPath:String? = null ):Long }

    class AndroidDownloader(context: Context):Downloader {

        private val downloadManager = context.getSystemService(DownloadManager::class.java)
        override fun downloadFile(url: String,fileSubPath:String?): Long {
            val request = DownloadManager.Request(url.toUri())
                .setMimeType("application/octet-stream") // Set MIME type to binary file
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setTitle("LibraryDownload")
                .setDescription("Downloading compiled C++ library (.so file).")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "libraryfile.so") // Ensure the filename ends with .so
            return downloadManager.enqueue(request)
        }
    }

    class DownloadCompleteReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)

                Log.e("TAG","informatn $id")

                if(id != -1L) {
                    println("Download with ID $id finished!")

                    showMessage(context!!,"C++ library file downloaded successfully")


                    val sourceFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "libraryfile.so")
                    val destinationFile = File(context!!.filesDir, "libLibraryDownload.so")  // Internal storage path

                    if (sourceFile.exists() ) {
                        sourceFile.copyTo(destinationFile, overwrite = true)
                    }

                    bindingTst.layoutData.visibility = View.VISIBLE
                    bindingTst.layoutB.visibility = View.GONE

                   try {

                        System.load(destinationFile.absolutePath)

                        // binding.sampleText.text = HelperQuestion.stringFromJNI()
                     //   val answer_is = HelperQuestion.enterBornYear(bindingTst.textViewDate.toString().trim())
                        val answer_is = HelperQuestion.enterBornYear("1995-12-30");
                        bindingTst.sampleText.text = "Answer is : $answer_is"

                        isLibLoaded = true ;
                        bindingTst.buttonDatePickerLayout.visibility =View.VISIBLE

                    }catch (e: UnsatisfiedLinkError) {
                        bindingTst.sampleText.text = "Library loading error: ${e.message}"
                        showMessage(context,"Error occurred during loading the library")
                    }catch (e :Exception) {
                        bindingTst.sampleText.text = e.message
                        showMessage(context,"Error occur during load the library ")
                    }

                }
            }
        }


        private fun  showMessage(context: Context,msg : String ){
            Toast.makeText(
                context,
                msg,
                Toast.LENGTH_LONG
            ).show()

            /*
                 return
                 val inflater = layoutInflater
                 val layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container))

                 val toast = Toast(applicationContext)
                 toast.duration = Toast.LENGTH_LONG
                 toast.view = layout
                 toast.show()
             */
        }

    }



}