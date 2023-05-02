package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataQuery.LoginQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextUsername = findViewById<EditText>(R.id.edt_user)
        val editTextPassword = findViewById<EditText>(R.id.edt_password)
        val buttonLogin = findViewById<Button>(R.id.button_login)
        val cardView:androidx.cardview.widget.CardView = findViewById(R.id.layout_setting)

        loadDatabaseServer()
        loadDatabaseName()
        loadDatabaseUser()
        loadDatabasePassword()

        cardView.setOnClickListener{
            val intent = Intent(this,Setting::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            if (editTextUsername.text.toString()
                    .replace(" ", "") != "" && editTextPassword.text.toString()
                    .replace(" ", "") != ""
            ) {
                val deferred = lifecycleScope.async(Dispatchers.IO) {
                    if (LoginQuery(this@Login, editTextUsername.text.toString(), editTextPassword.text.toString()).login()) {
                        val intent = Intent(this@Login, MainActivity::class.java)
                        startActivity(intent)
                    }
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    if (deferred.isActive) {
                        val progressDialogBuilder = createProgressDialog()

                        try {
                            progressDialogBuilder.show()
                            val result = deferred.await()

                        } finally {
                            progressDialogBuilder.cancel()
                        }
                    } else {
                        val result = deferred.await()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun createProgressDialog(): ProgressDialog {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Logging in ....")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        return progressDialog
    }

    private fun loadDatabaseServer() {
        var prefs = getSharedPreferences("databaseServer", Activity.MODE_PRIVATE)
        Gvariable.databaseServer = prefs.getString("valDatabaseServer", "").toString()
    }

    private fun loadDatabaseName() {
        var prefs = getSharedPreferences("databaseName", Activity.MODE_PRIVATE)
        Gvariable.databaseName = prefs.getString("valDatabaseName", "").toString()
    }

    private fun loadDatabaseUser() {
        var prefs = getSharedPreferences("databaseUser", Activity.MODE_PRIVATE)
        Gvariable.databaseUser = prefs.getString("valDatabaseUser", "").toString()
    }

    private fun loadDatabasePassword() {
        var prefs = getSharedPreferences("databasePassword", Activity.MODE_PRIVATE)
        Gvariable.databasePassword = prefs.getString("valDatabasePassword", "").toString()
    }

}