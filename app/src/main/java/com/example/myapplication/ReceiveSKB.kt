package com.example.myapplication

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataQuery.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class ReceiveSKB : AppCompatActivity() {
    companion object {
        lateinit var ediTextScanKanban: EditText
        lateinit var textViewSerial: TextView
        lateinit var textViewOrderDate: TextView
        lateinit var spinnerOrderNo: Spinner
        lateinit var textViewPartNo: TextView
        lateinit var textViewTotalReceive: TextView
        lateinit var buttonList: Button
        var date = ""
        var currentDate = ""
        var orderNoList = ArrayList<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_skb)

        ediTextScanKanban = findViewById(R.id.editText_serial_kanban)
        textViewSerial = findViewById(R.id.textView_serialNo)
        textViewOrderDate = findViewById(R.id.textView_orderDate)
        spinnerOrderNo = findViewById(R.id.spinner_orderNo)
        textViewPartNo = findViewById(R.id.textView_partNo)
        textViewTotalReceive = findViewById(R.id.textView_totalReceive)
        buttonList = findViewById(R.id.button_list)

        onLoad()

        buttonList.setOnClickListener {

        }

        ediTextScanKanban.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (ediTextScanKanban.text.toString().isNotEmpty()) {
                    if (ediTextScanKanban.text.toString().length == 15) {
                        ediTextScanKanban.setText(ediTextScanKanban.text.toString().uppercase())
                        asyncScanKanban()
                    } else {
                        //Please scan only Serial Kanban documents.
                        Gvariable().messageAlertDialog(
                            this,
                            "กรุณาแสกนเอกสาร Serial Kanban เท่านั้น",
                            layoutInflater
                        )
                        Gvariable().alarm(this)
                        ediTextScanKanban.selectAll()
                        ediTextScanKanban.requestFocus()
                    }
                } else {
                    ediTextScanKanban.requestFocus()
                }
            }
            false
        })

        spinnerOrderNo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                ediTextScanKanban.selectAll()
                ediTextScanKanban.requestFocus()
            }
        }

        textViewOrderDate.setOnClickListener {
            datePicker()
        }

    }

    private fun onLoad(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val getDate = GetTimeQuery().timeServer()
            val date = getDate.split("|").toTypedArray()[0]
            val date1 = getDate.split("|").toTypedArray()[1]
            currentDate = date
            orderNoList.clear()
            val showOrderList = DeliveryQuery().showOrder( date1, "", "")
            for(i in 0 until showOrderList.size){
                orderNoList.add(showOrderList[i].substringBefore("|"))
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveSKB)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    loadSpinnerOrderNo()
                    textViewOrderDate.text = currentDate
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
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveSKB)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    loadSpinnerOrderNo()
                    progressDialogBuilder.cancel()
                    ediTextScanKanban.selectAll()
                    ediTextScanKanban.requestFocus()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun scanKanBan() {
        var partNo = ediTextScanKanban.text.toString().substring(0, 11)
        var docSerial = ediTextScanKanban.text.toString()
        Handler(Looper.getMainLooper()).post {
            textViewSerial.text = docSerial
        }

        var orderId = ""
        var orderDetail = ""
        var orderNo = ""
        var arrayOrderDetail = ArrayList<String>()
        var orderQty = 0
        var orderProcessQty = 0
        var newQty = 0

        orderDetail = OrderDetailQuery().getOrderIdBySerial("RECEIVE", docSerial).trim()
        arrayOrderDetail.add(orderDetail.split("|").toTypedArray()[0])
        arrayOrderDetail.add(orderDetail.split("|").toTypedArray()[1])
        arrayOrderDetail.add(orderDetail.split("|").toTypedArray()[2])
        if (arrayOrderDetail[0] != "") {
            date = arrayOrderDetail[2].trim()
            //set date time picker
            currentDate = "${date.substring(6,8)}-${date.substring(4,6)}-${date.substring(0,4)}"
            Handler(Looper.getMainLooper()).post {
                textViewOrderDate.text = currentDate
            }
            orderId = arrayOrderDetail[0]
            orderNo = arrayOrderDetail[1]
            orderNoList.clear()
            orderNoList.add(orderNo)

            orderQty = OrderDetailQuery().getOrderQty(orderId)
            if (orderQty != 0) {
                var total = ""
                var getOrderList = OrderProcessQuery().getOrder(docSerial, orderNo, date)
                orderProcessQty = if (getOrderList.isEmpty()) {
                    0
                } else {
                    getOrderList[0].receiveQty!!
                }
                newQty = orderProcessQty + 1 // 1 is fixed qty for Receive SKB
                when {
                    newQty < 0 -> {
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "Error: Qty < 0", layoutInflater)
                    }
                    newQty > orderQty -> {
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(
                            this,
                            "Error: Over Qty ($newQty / $orderQty)",
                            layoutInflater
                        )
                        Handler(Looper.getMainLooper()).post {
                            textViewTotalReceive.text = orderProcessQty.toString()
                        }
                    }
                    else -> {
                        if (getOrderList.isEmpty()) {
                            Gvariable().alarm(this)
                            Gvariable().messageAlertDialog(
                                this,
                                "ไม่พบ Serial No. หมายเลขนี้ใน Order.",
                                layoutInflater
                            )
                        } else {
                            if (getOrderList.isNotEmpty()) {
                                var check: Boolean = false
                                for (i in 0 until getOrderList.size) {
                                    if (!check) {
                                        if (getOrderList[i].receiveDate.isNullOrEmpty()) {
                                            OrderProcessQuery().updateOrderProcessReceive(
                                                getOrderList[i].pId!!,
                                                newQty
                                            )
                                            OrderDetailQuery().writeLog(
                                                "Scan Receive PDA Service Program",
                                                "OrderProcess",
                                                "Scan Receive Serial Kanban : ${docSerial.trim()}",
                                                docSerial.trim(),
                                                Gvariable.userName!!
                                            )
                                            OrderProcessQuery().checkUpdateStatus(getOrderList[i].pId!!)
                                            Handler(Looper.getMainLooper()).post {
                                                textViewPartNo.text = getOrderList[i].partNo
                                            }
                                            check = true
                                            total = OrderProcessQuery().getSumQty(
                                                getOrderList[i].orderDetailId!!,
                                                "ReceiveQty"
                                            ).toString()
                                            //play sound ok
                                        }
                                    } else {
                                        break
                                    }
                                }
                                if (!check) {
                                    total = OrderProcessQuery().getSumQty(
                                        getOrderList[0].orderDetailId!!,
                                        "ReceiveQty"
                                    ).toString()
                                    Handler(Looper.getMainLooper()).post {
                                        textViewPartNo.text = getOrderList[0].partNo
                                    }
                                    Gvariable().alarm(this)
                                    Gvariable().messageAlertDialog(
                                        this,
                                        "แสกน Serial No. นี้เรียบร้อยแล้",
                                        layoutInflater
                                    )
                                }
                                Handler(Looper.getMainLooper()).post {
                                    textViewTotalReceive.text = total
                                }
                            }
                        }

                    }
                }
            } else {
                Gvariable().messageAlertDialog(
                    this,
                    "ไม่พบ Part No. ใน OrderNo นี้",
                    layoutInflater
                )
                Gvariable().alarm(this)
            }
        } else {
            Gvariable().messageAlertDialog(
                this,
                "ไม่พบ Serial KB หมายเลขนี้ใน Order.",
                layoutInflater
            )
            Gvariable().alarm(this)
        }
    }

    private fun loadSpinnerOrderNo(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orderNoList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderNo.adapter = arrayAdapter
    }

    private fun asyncListOrderNo(orderDate:String){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            orderNoList.clear()
            var showOrderList = DeliveryQuery().showOrder( orderDate, "", "")
            for(i in 0 until showOrderList.size){
                orderNoList.add(showOrderList[i].substringBefore("|"))
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveSKB)

                try {
                    progressDialogBuilder.show()
                    deferred.await()
                } finally {
                    loadSpinnerOrderNo()
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()
            }
        }

    }

    private fun datePicker(){
        val arrayList = currentDate.split("-").toTypedArray()
        val year = Integer.parseInt(arrayList[2])
        val month = Integer.parseInt(arrayList[1]) - 1
        val day = Integer.parseInt(arrayList[0])
        var oldDate = currentDate
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val mth = monthOfYear + 1

                currentDate = "${String.format("%02d",dayOfMonth)}-${String.format("%02d",mth)}-$year"
                textViewOrderDate.text = currentDate
                if(currentDate != oldDate){
                    asyncListOrderNo("$year${String.format("%02d",mth)}${String.format("%02d",dayOfMonth)}")
                }
            },
            year,
            month,
            day
        )
        dpd.show()
    }
}