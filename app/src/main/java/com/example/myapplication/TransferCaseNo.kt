package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class TransferCaseNo : AppCompatActivity() {
    companion object{
        lateinit var buttonConfirm:Button
        lateinit var buttonCancel: Button
        lateinit var editTextTransferFrom: EditText
        lateinit var editTextTransferTo: EditText
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_case_no)
    }
}