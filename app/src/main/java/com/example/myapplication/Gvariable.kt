package com.example.myapplication

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class Gvariable {
    companion object {
        var databaseServer = ""
        var databaseName = ""
        var databaseUser = ""
        var databasePassword = ""
        var userRealName:String? = null
        var userName: String? = null
        var properties:Properties? = null
        var conn: Connection? = null
        var driver = "net.sourceforge.jtds.jdbc.Driver"
        var connURL = ""
    }

    fun startConn(){
        Class.forName(driver)
        properties = Properties()
        properties!!["connectTimeout"] = "2000"
        DriverManager.setLoginTimeout(3)
        conn = DriverManager.getConnection(connURL, properties)
    }

    fun alarm(context: Context){
        Handler(Looper.getMainLooper()).post {
            val afd: AssetFileDescriptor = context.assets.openFd("buzz.wav")
            val player = MediaPlayer()
            player.setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )
            player.prepare()
            player.start()
            val handler = Handler()
            handler.postDelayed({ player.stop() }, 1 * 1000.toLong())
            afd.close()
        }
    }

    fun createProgressDialog(context:Context): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Loading data ....")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        return progressDialog
    }

    fun messageAlertDialog(context: Context, text:String, layoutInflater: LayoutInflater) {
        Handler(Looper.getMainLooper()).post {
            val builder = AlertDialog.Builder(context)
            val view = layoutInflater.inflate(R.layout.message_alert_dialog, null)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()
            dialog.setCancelable(false)
            val buttonOk = view.findViewById<Button>(R.id.btn_ok)
            val textViewText = view.findViewById<TextView>(R.id.txt_text)

            textViewText.text = text
            buttonOk.requestFocus()
            buttonOk.setOnClickListener {
                dialog.dismiss()
            }
        }

    }

    fun messageOkDialog(context: Context, text:String, layoutInflater: LayoutInflater) {
        Handler(Looper.getMainLooper()).post {
            val builder = AlertDialog.Builder(context)
            val view = layoutInflater.inflate(R.layout.message_ok_dialog, null)
            builder.setView(view)
            val dialog = builder.create()
            dialog.show()
            dialog.setCancelable(false)
            val buttonOk = view.findViewById<Button>(R.id.btn_ok)
            val textViewText = view.findViewById<TextView>(R.id.txt_text)

            textViewText.text = text
            buttonOk.requestFocus()
            buttonOk.setOnClickListener {
                dialog.dismiss()
            }
        }

    }

}