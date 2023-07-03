package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataQuery.GetTimeQuery
import com.example.myapplication.DataQuery.PackingQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val linearLayoutReceiveSkb = findViewById<LinearLayout>(R.id.linearLayout_receive_skb)
        val linearLayoutReceiveNoSkb = findViewById<LinearLayout>(R.id.linearLayout_receive_no_skb)
        val linearLayoutPacking = findViewById<LinearLayout>(R.id.linearLayout_packing)
        val textViewSKB = findViewById<TextView>(R.id.textview_skb)
        val textViewNoSKB = findViewById<TextView>(R.id.textview_no_skb)
        val textViewPacking = findViewById<TextView>(R.id.textview_packing)
        val textViewDelivery = findViewById<TextView>(R.id.textview_delivery)
        val textViewStatus = findViewById<TextView>(R.id.textview_status)
        val linearLayoutCheck = findViewById<LinearLayout>(R.id.linearLayout_check)
        val cardViewBack = findViewById<CardView>(R.id.cardView_back)

        linearLayoutReceiveSkb.isEnabled = false
        linearLayoutReceiveNoSkb.isEnabled = false
        linearLayoutPacking.isEnabled = false
        textViewDelivery.isEnabled = false

        for(i in 0 until Gvariable.menuList.size){
            when(Gvariable.menuList[i]){
                "PDA_1" -> {
                    linearLayoutReceiveSkb.isEnabled = true
                    linearLayoutReceiveNoSkb.isEnabled = true
                    textViewSKB.setBackgroundColor(resources.getColor(R.color.tea_50))
                    textViewNoSKB.setBackgroundColor(resources.getColor(R.color.tea_50))
                }
                "PDA_2" -> {
                    linearLayoutPacking.isEnabled = true
                    textViewPacking.setBackgroundColor(resources.getColor(R.color.tea_50))
                }
                "PDA_3" -> {
                    textViewDelivery.isEnabled = true
                    textViewDelivery.setBackgroundColor(resources.getColor(R.color.tea_50))
                }
                "PDA_4" -> {
                    //special packing
                }
                "PDA_5" -> {
                    //special delivery
                }
            }
        }
        linearLayoutReceiveSkb.setOnClickListener {
            startActivity(ReceiveSKB::class.java)
        }

        linearLayoutReceiveNoSkb.setOnClickListener {
            startActivity(ReceiveNoSKB::class.java)
        }

        linearLayoutPacking.setOnClickListener {
            asyncOpenCasePacking()
        }

        textViewDelivery.setOnClickListener {
            asyncGetDate()
        }

        textViewStatus.setOnClickListener {
            startActivity(SkbStatus::class.java)
        }

        linearLayoutCheck.setOnClickListener {
            startActivity(CheckListCase::class.java)
        }

        cardViewBack.setOnClickListener {
            finish()
            super.onBackPressed()
        }
    }

    private fun startActivity(cls:Class<*>){
        val intent = Intent(this,cls)
        startActivity(intent)
    }

    private fun asyncGetDate(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            Delivery.date = GetTimeQuery().timeServer().split("|").toTypedArray()[0]
            Delivery.roundList.clear()
            for(i in 31 downTo 1){
                Delivery.roundList.add(i.toString())
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@MainActivity)

                try {
                    progressDialogBuilder.show()
                    deferred.await()
                } finally {
                    progressDialogBuilder.cancel()
                    startActivity(Delivery::class.java)
                }
            } else {
                deferred.await()
            }
        }
    }

    private fun asyncOpenCasePacking(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            OpenCasePacking.caseList = PackingQuery().getCaseNoByUser(Gvariable.userName.toString())
            OpenCasePacking.totalCase = PackingQuery().getCountCaseByUser(Gvariable.userName.toString())
        }

        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@MainActivity)

                try {
                    progressDialogBuilder.show()
                    deferred.await()
                } finally {
                    progressDialogBuilder.cancel()
                    startActivity(OpenCasePacking::class.java)
                }
            } else {
                deferred.await()
            }
        }
    }
}