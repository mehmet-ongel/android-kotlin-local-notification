package com.techmania.localnotification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.techmania.localnotification.databinding.ActivityMainBinding
import com.techmania.localnotification.databinding.BottomSheetLayoutBinding

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding : ActivityMainBinding
    lateinit var bottomSheetLayoutBinding: BottomSheetLayoutBinding

    var counter = 0
    val CHANNEL_ID = "1"
    val CHANNEL_NAME = "Counter Notification"
    val NOTIFICATION_ID = 1
    lateinit var builder : NotificationCompat.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        mainBinding.buttonCounter.setOnClickListener {

            counter++
            mainBinding.buttonCounter.text = counter.toString()

            if (counter == 5){

                sendNotification()

            }

        }

    }

    fun sendNotification(){

        //version control for creating the channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val channel = NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        builder = NotificationCompat.Builder(this,CHANNEL_ID)

        //customize notification
        builder.apply {
            setSmallIcon(R.drawable.small_icon)
            setContentTitle("Notification Title")
            setContentText("Notification text")
            priority = NotificationCompat.PRIORITY_HIGH
        }

        NotificationManagerCompat.from(this).apply {

            //version control
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

                //if SDK version is 33 or above, check POST_NOTIFICATION permission
                if(ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){

                    /*
                    In an educational UI, explain to the user why your app requires this
                    permission for a specific feature to behave as expected, and what
                    features are disabled if it's declined. In this UI, include a
                    "cancel" or "no thanks" button that lets the user continue
                    using your app without granting the permission. So we show a bottom sheet dialog for this. If you want you can create dialog window or snackbar message
                    */
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){

                        showBottomSheetDialog()

                        /*
                        Snackbar.make(
                            mainBinding.constraintLayout,
                            "Please allow the permission to take notification",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK"){
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
                        }.show()

                         */

                    }else{
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
                    }
                }else{
                    notify(NOTIFICATION_ID,builder.build())
                }
            }else{
                notify(NOTIFICATION_ID,builder.build())
            }

        }

    }

    //create bottom sheet dialog
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showBottomSheetDialog() {

        //inflate the bottom_sheet_layout.xml
        val dialog = Dialog(this@MainActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bottomSheetLayoutBinding = BottomSheetLayoutBinding.inflate(layoutInflater)
        val view = bottomSheetLayoutBinding.root
        dialog.setContentView(view)

        bottomSheetLayoutBinding.buttonAllow.setOnClickListener {
            //cancel the dialog
            dialog.dismiss()

            //request permission again
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
        }

        bottomSheetLayoutBinding.buttonNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) //set height and width of the dialog window
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) //make it transparent
        dialog.window?.setWindowAnimations(R.style.BottomSheetDialogAnimation) //add the animation_in and animation_out style to it
        dialog.window?.setGravity(Gravity.BOTTOM) //show it on the bottom

        dialog.show()

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //if the user clicks the Allow button first time, this if block works
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            NotificationManagerCompat.from(this@MainActivity).apply {

                notify(NOTIFICATION_ID,builder.build())
            }

        }else {

            //if the user clicks the Don't Allow button first time, this else block works and shows the bottom sheet dialog
            showBottomSheetDialog()
            /*
            Snackbar.make(
                mainBinding.constraintLayout,
                "Please allow the permission to take notification",
                Snackbar.LENGTH_INDEFINITE).setAction("OK"){
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
            }.show()

             */

        }

    }
}