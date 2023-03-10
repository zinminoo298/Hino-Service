package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val linearLayoutReceiveSkb = findViewById<LinearLayout>(R.id.linearLayout_receive_skb)
        val linearLayoutReceiveNoSkb = findViewById<LinearLayout>(R.id.linearLayout_receive_no_skb)
        val linearLayoutPacking = findViewById<LinearLayout>(R.id.linearLayout_packing)
        val textViewDelivery = findViewById<TextView>(R.id.textview_delivery)
        val textViewStatus = findViewById<TextView>(R.id.textview_status)
        val linearLayoutCheck = findViewById<LinearLayout>(R.id.linearLayout_check)

        linearLayoutReceiveSkb.setOnClickListener {
            val intent = Intent(this, ReceiveSKB::class.java)
            startActivity(intent)
        }

        linearLayoutReceiveNoSkb.setOnClickListener {

        }

        linearLayoutPacking.setOnClickListener {

        }

        textViewDelivery.setOnClickListener {

        }

        textViewStatus.setOnClickListener {

        }

        linearLayoutCheck.setOnClickListener {

        }

    }
}