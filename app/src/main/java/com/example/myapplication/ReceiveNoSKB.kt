package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataQuery.DeliveryQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ReceiveNoSKB : AppCompatActivity() {
    companion object {
        lateinit var ediTextScanKanban: EditText
        lateinit var editTextReceiveQty: EditText
        lateinit var textViewPds: TextView
        lateinit var textViewSerial: TextView
        lateinit var textViewOrderDate: TextView
        lateinit var spinnerOrderNo: Spinner
        lateinit var textViewPartNo: TextView
        lateinit var spinnerTotalReceive: Spinner
        lateinit var buttonList: Button
        lateinit var buttonSave: Button
        var date = ""
        var currentDate = ""
        var partNo = ""
        var pds = ""
        var receiveQty = 0
        var orderNoList = ArrayList<String>()
        var skbStatusList = ArrayList<String>()
        var orderNoListByPdsList = ArrayList<String>()
        var orderByOrderList = ArrayList<String>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reveive_no_skb)

        ediTextScanKanban = findViewById(R.id.editText_serial_kanban)
        editTextReceiveQty = findViewById(R.id.editText_receiveQty)
        spinnerTotalReceive = findViewById(R.id.spinner_totalReceive)
        spinnerOrderNo = findViewById(R.id.spinner_orderNo)
        textViewPartNo = findViewById(R.id.textView_partNo)
        textViewPds = findViewById(R.id.textview_pds)
        buttonSave = findViewById(R.id.button_save)

        ediTextScanKanban.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (ediTextScanKanban.text.toString().isNotEmpty()) {
                    if (ediTextScanKanban.text.toString().length != 15) {
                        ediTextScanKanban.setText(ediTextScanKanban.text.toString().uppercase())
                        asyncScanKanban()
                    } else {
                        //Serial KB cannot be used in this screen..
                        Gvariable().messageAlertDialog(
                            this,
                            "ไม่สามารถใช้ Serial KB ในหน้าจอนี้ได้",
                            layoutInflater
                        )
                        Gvariable().alarm(this)
                        ediTextScanKanban.text.clear()
                        ediTextScanKanban.requestFocus()
                    }
                } else {
                    ediTextScanKanban.requestFocus()
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "กรุณาแสกนเพื่อรับชิ้นส่วน!  ", layoutInflater)
                }
            }
            false
        })

        buttonSave.setOnClickListener {
            asyncSave()
        }


    }

    private fun setSpinnerOrderNo(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orderNoList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderNo.adapter = arrayAdapter
    }

    private fun setSpinnerTotalReceive(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, skbStatusList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTotalReceive.adapter = arrayAdapter
    }

    private fun asyncListOrderNo(orderDate:String){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            orderNoList.clear()
            skbStatusList.clear()
            var showOrderList = DeliveryQuery().showOrder( "", partNo, "RECEIVE")
            for(i in 0 until showOrderList.size){
                orderNoList.add(showOrderList[i].substringBefore("|"))
                skbStatusList.add(showOrderList[i].substringAfter("|"))
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveNoSKB)

                try {
                    progressDialogBuilder.show()
                    deferred.await()
                } finally {
                    setSpinnerOrderNo()
                    setSpinnerTotalReceive()
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()
            }
        }
    }

    private fun asyncScanKanban() {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            scanKanBan()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveNoSKB)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    progressDialogBuilder.cancel()
                    ediTextScanKanban.selectAll()
                    ediTextScanKanban.requestFocus()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun scanKanBan(){
        var inputBarcode = ediTextScanKanban.text.toString().split("|").toTypedArray()
        if(inputBarcode.isNotEmpty()){
            partNo = inputBarcode[0].substring(3, 8) +"-"+ inputBarcode[0].substring(8, 12)
            pds = inputBarcode[11]
            receiveQty = Integer.parseInt(inputBarcode[13])
        }else{
            partNo = ediTextScanKanban.text.toString().uppercase()
            pds = "PHOTOSPEC"
            receiveQty = 1
        }

        if(inputBarcode.isNotEmpty()){
            try{
                Handler(Looper.getMainLooper()).post {
                    editTextReceiveQty.setText(receiveQty.toString())
                    textViewPartNo.text = partNo
                    textViewPds.text = "PDS : $pds"
                }
                if(DeliveryQuery().checkOrderByPds(pds, partNo)){
                    orderNoListByPdsList.clear()
                    orderNoListByPdsList = DeliveryQuery().showOrderByPds( pds, partNo, "")
                }else{
                    orderByOrderList.clear()
                    orderByOrderList = DeliveryQuery().showOrderByOrder(partNo)
                }

            }catch (e:Exception){
                e.printStackTrace()
                //setQty = 1
                orderByOrderList.clear()
                orderByOrderList = DeliveryQuery().showOrderByOrder(partNo)
            }
        }
        else{
            Handler(Looper.getMainLooper()).post{
                textViewPds.text = "PDS : $pds"
                textViewPartNo.text = partNo
                ediTextScanKanban.setText(editTextReceiveQty.text.toString().uppercase())
            }
            orderByOrderList.clear()
            orderByOrderList = DeliveryQuery().showOrderByOrder(partNo)
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "กรุณาเลือก Order", layoutInflater)
            //order spinner req focus
        }
    }

    private fun asyncSave() {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            save()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveNoSKB)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    progressDialogBuilder.cancel()
                    ediTextScanKanban.selectAll()
                    ediTextScanKanban.requestFocus()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun save(){
        if(orderNoList.isEmpty()){
           Gvariable().alarm(this)
           Gvariable().messageAlertDialog(this, "กรุณาเลือก Order!", layoutInflater)
        }
        else{
            if(editTextReceiveQty.text.toString() == "0"){
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "กรุณาใส่จำนวนรับ!", layoutInflater)
                Handler(Looper.getMainLooper()).post{
                    editTextReceiveQty.requestFocus()
                }
            }
        }
    }

}