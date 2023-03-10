package com.example.myapplication

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.cardview.widget.CardView

class Setting : AppCompatActivity() {
    companion object{
        lateinit var editTextDatabaseServer: EditText
        lateinit var editTextDatabaseName: EditText
        lateinit var editTextDatabaseUser: EditText
        lateinit var editTextDatabasePassword: EditText
        lateinit var buttonSave: Button
        lateinit var cardViewBack : CardView
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        editTextDatabaseServer = findViewById(R.id.editText_database_server)
        editTextDatabaseName = findViewById(R.id.editText_database_name)
        editTextDatabaseUser = findViewById(R.id.editText_database_user)
        editTextDatabasePassword = findViewById(R.id.editText_database_password)
        cardViewBack = findViewById(R.id.cardView_back)
        buttonSave = findViewById(R.id.button_save)

        editTextDatabaseServer.setText(Gvariable.databaseServer)
        editTextDatabaseName.setText(Gvariable.databaseName)
        editTextDatabaseUser.setText(Gvariable.databaseUser)
        editTextDatabasePassword.setText(Gvariable.databasePassword)

        buttonSave.setOnClickListener{
            when{
                editTextDatabaseServer.text.toString() == "" -> {
                    Toast.makeText(this,"Please Enter Database Server", Toast.LENGTH_LONG).show()}
                editTextDatabaseName.text.toString() == "" -> {
                    Toast.makeText(this,"Please Enter Database Name", Toast.LENGTH_LONG).show()}
                editTextDatabaseUser.text.toString() == "" -> {
                    Toast.makeText(this,"Please Enter Database User", Toast.LENGTH_LONG).show()}
                editTextDatabasePassword.text.toString() == "" -> {
                    Toast.makeText(this,"Please Enter Database Password", Toast.LENGTH_LONG).show()}
                else -> {
                    setDatabaseServer(editTextDatabaseServer.text.toString())
                    setDatabaseName(editTextDatabaseName.text.toString())
                    setDatabaseUser(editTextDatabaseUser.text.toString())
                    setDatabasePassword(editTextDatabasePassword.text.toString())
                    Toast.makeText(this,"Save Successful", Toast.LENGTH_LONG).show()
                    loadDatabaseServer()
                    loadDatabaseName()
                    loadDatabaseUser()
                    loadDatabasePassword()
                }
            }
        }

        cardViewBack.setOnClickListener {
            finish();
            super.onBackPressed();
        }
    }

    private fun setDatabaseServer(v: String){
        var editor = getSharedPreferences("databaseServer", Activity.MODE_PRIVATE).edit()
        editor.putString("valDatabaseServer", v)
        editor.apply()
    }

    private fun setDatabaseName(v: String){
        var editor = getSharedPreferences("databaseName", Activity.MODE_PRIVATE).edit()
        editor.putString("valDatabaseName", v)
        editor.apply()
    }

    private fun setDatabaseUser(v: String){
        var editor = getSharedPreferences("databaseUser", Activity.MODE_PRIVATE).edit()
        editor.putString("valDatabaseUser", v)
        editor.apply()
    }

    private fun setDatabasePassword(v: String){
        var editor = getSharedPreferences("databasePassword", Activity.MODE_PRIVATE).edit()
        editor.putString("valDatabasePassword", v)
        editor.apply()
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