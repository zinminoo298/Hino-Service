package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView

class ViewFullOrder : AppCompatActivity() {
    companion object{
        lateinit var textViewDate : TextView
        lateinit var textViePartNo : TextView
        lateinit var spinnerOrderNo : Spinner
        lateinit var cardViewBack : CardView
        var date = ""
        var partNo = ""
        var orderList = ArrayList<String>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_order)

        textViewDate = findViewById(R.id.textView_orderDate)
        textViePartNo = findViewById(R.id.textView_partNo)
        spinnerOrderNo = findViewById(R.id.spinner_orderNo)
        cardViewBack = findViewById(R.id.cardView_back)

        textViewDate.text= date
        textViePartNo.text = partNo
        setSpinnerOrderNo()

        cardViewBack.setOnClickListener {
            finish()
            super.onBackPressed()
        }
        
    }

    private fun setSpinnerOrderNo(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orderList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderNo.adapter = arrayAdapter
    }
}