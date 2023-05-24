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
import com.example.myapplication.DataQuery.GetTimeQuery
import com.example.myapplication.DataQuery.OrderDetailQuery
import com.example.myapplication.DataQuery.OrderProcessQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ReceiveNoSKB : AppCompatActivity() {
    companion object {
        lateinit var editTextScanKanban: EditText
        lateinit var editTextReceiveQty: EditText
        lateinit var textViewPds: TextView
        lateinit var textViewSerial: TextView
        lateinit var textViewOrderDate: TextView
        lateinit var spinnerOrderNo: Spinner
        lateinit var textViewPartNo: TextView
        lateinit var textViewTotalReceive: TextView
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

        editTextScanKanban = findViewById(R.id.editText_serial_kanban)
        editTextReceiveQty = findViewById(R.id.editText_receiveQty)
        textViewTotalReceive = findViewById(R.id.textview_totalReceive)
        spinnerOrderNo = findViewById(R.id.spinner_orderNo)
        textViewPartNo = findViewById(R.id.textView_partNo)
        textViewPds = findViewById(R.id.textview_pds)
        buttonSave = findViewById(R.id.button_save)

        asyncGetDate()

        editTextScanKanban.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (editTextScanKanban.text.toString().isNotEmpty()) {
                    if (editTextScanKanban.text.toString().length != 15) {
                        editTextScanKanban.setText(editTextScanKanban.text.toString().uppercase())
                        asyncScanKanban()
                    } else {
                        //Serial KB cannot be used in this screen..
                        Gvariable().messageAlertDialog(
                            this,
                            "ไม่สามารถใช้ Serial KB ในหน้าจอนี้ได้",
                            layoutInflater
                        )
                        Gvariable().alarm(this)
                        editTextScanKanban.text.clear()
                        editTextScanKanban.requestFocus()
                    }
                } else {
                    editTextScanKanban.requestFocus()
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

    private fun asyncGetDate(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            date = GetTimeQuery().timeServer().split("|").toTypedArray()[1]
        }

        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveNoSKB)

                try {
                    progressDialogBuilder.show()
                    deferred.await()
                } finally {
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
                    editTextScanKanban.selectAll()
                    editTextScanKanban.requestFocus()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun scanKanBan(){
        var inputBarcode = editTextScanKanban.text.toString().split("|").toTypedArray()
        if(inputBarcode.isNotEmpty()){
            partNo = inputBarcode[0].substring(3, 8) +"-"+ inputBarcode[0].substring(8, 13)
            pds = inputBarcode[11]
            receiveQty = Integer.parseInt(inputBarcode[13])
        }else{
            partNo = editTextScanKanban.text.toString().uppercase()
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
                    listOrderNoByPDS(pds, partNo)
                }else{
                    listOrderNoByPHT(partNo)
                }

            }catch (e:Exception){
                e.printStackTrace()
                listOrderNoByPHT(partNo)
                Handler(Looper.getMainLooper()).post {
                    editTextReceiveQty.setText("1")
                }
            }

            if(spinnerOrderNo.selectedItem.toString() == ""){
                Handler(Looper.getMainLooper()).post {
                    spinnerOrderNo.requestFocus()
                }
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "กรุณาเลือก Order", layoutInflater)
            }
        }
        else{
            Handler(Looper.getMainLooper()).post{
                textViewPds.text = "PDS : $pds"
                textViewPartNo.text = partNo
                editTextScanKanban.setText(editTextReceiveQty.text.toString().uppercase())
                spinnerOrderNo.requestFocus()
            }
            listOrderNoByPHT(partNo)
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
                    setSpinnerOrderNo()
                    progressDialogBuilder.cancel()
                    editTextScanKanban.selectAll()
                    editTextScanKanban.requestFocus()
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
            else{
                var orderDetail = ""
                var orderNo = spinnerOrderNo.selectedItem.toString().trim()
                var orderDetailId = ""
                var partNo = ""
                var pds = ""
                var orderQty = 0
                var orderProcessQty = 0
                var newQty = 0
                var docSerial = ""

                editTextScanKanban.setText(editTextScanKanban.text.toString().uppercase())
                try{
                    var inputBarcode = editTextScanKanban.text.split("|").toTypedArray()
                    if(inputBarcode.isNotEmpty()){
                        partNo = "${inputBarcode[0].substring(3,8)}-${inputBarcode[0].substring(8,13)}"
                        docSerial = partNo
                        pds = inputBarcode[11]
                    }
                    else{
                        partNo = editTextScanKanban.text.toString().uppercase()
                        docSerial = partNo
                        pds = ""
                    }

                    orderDetail = OrderDetailQuery().getOrderDetailByPartNo(orderNo, partNo)
                    var arrayOrderDetail = orderDetail.split("|").toTypedArray()
                    if(arrayOrderDetail.size == 4){
                        orderDetailId = arrayOrderDetail[0]
                        orderQty = Integer.parseInt(arrayOrderDetail[3])

                        if(orderQty != 0){
                            var strTotal = ""
                            var getOrderList = OrderProcessQuery().getOrder(docSerial, orderNo, date)
                            orderProcessQty = OrderProcessQuery().getSumQty(orderDetailId, "ReceiveQty")
                            newQty = orderProcessQty + Integer.parseInt(editTextReceiveQty.text.toString())

                            when {
                                newQty < 0 -> {
                                    Gvariable().alarm(this)
                                    Gvariable().messageAlertDialog(this, "Error: จำนวนน้อยกว่า 0", layoutInflater)
                                }
                                newQty > orderQty -> {
                                    Gvariable().alarm(this)
                                    Gvariable().messageAlertDialog(this, "Error: รับ Part เกินจำนวน (Receive:${newQty}/Order ${orderQty})", layoutInflater)
                                    //lbtotal.Text = OrderProcessQty.ToString
                                }
                                else -> {
                                    if(getOrderList.isEmpty()){
                                        if(OrderProcessQuery().save("", orderDetailId, orderNo, docSerial)){
                                            OrderProcessQuery().updateOrderProcessReceive("", Integer.parseInt(editTextReceiveQty.text.toString()))
                                            Handler(Looper.getMainLooper()).post {
                                                textViewPartNo.text = partNo
                                                editTextReceiveQty.setText("1")
                                                //lbtotal.Text = NewQty.ToString
                                            }
                                        }
                                    }
                                    else{
                                        var chk = false
                                        for(i in 0 until getOrderList.size){
                                            if(chk){
                                                break
                                            }else{
                                                if(getOrderList[i].receiveDate.isNullOrEmpty()){
                                                    orderProcessQty = OrderProcessQuery().getSumQtyByPid(getOrderList[i].pId!!, "ReceiveQty")
                                                    newQty = orderProcessQty + Integer.parseInt(editTextReceiveQty.text.toString())
                                                    OrderProcessQuery().updateOrderProcessReceive(getOrderList[i].pId!!, newQty)
                                                    OrderDetailQuery().writeLog("Scan Receive PDA Service Program","OrderProcess", "Scan Receive NSKB Kanban : ${editTextScanKanban.text.trim()}", editTextScanKanban.text.toString().trim(),  Gvariable.userName!!.trim())
                                                    chk = true
                                                    strTotal = OrderProcessQuery().getSumQty(getOrderList[i].orderDetailId!!, "ReceiveQty").toString()

                                                    Handler(Looper.getMainLooper()).post {
                                                        textViewPartNo.text = getOrderList[i].partNo
                                                        editTextReceiveQty.setText("1")
                                                        //lbtotal.Text = Strtotal
                                                    }
                                                }
                                            }
                                        }
                                        if(!chk){
                                            strTotal = OrderProcessQuery().getSumQty(getOrderList[0].orderDetailId!!, "ReceiveQty").toString()
                                            Gvariable().alarm(this)
                                            Gvariable().messageAlertDialog(this, "Serial No. นี้ถูกแสกนเรียบร้อยแล้ว", layoutInflater)
                                            Handler(Looper.getMainLooper()).post {
                                                textViewPartNo.text = getOrderList[0].partNo
                                                editTextReceiveQty.setText("1")
                                                //lbtotal.Text = Strtotal
                                            }
                                        }
                                    }
                                    if (pds != ""){
                                        listOrderNoByPDS(pds, partNo)
                                    }else{
                                        listOrderNoByPHT(partNo)
                                    }
                                }
                            }

                        }
                        else{
                            Gvariable().alarm(this)
                            Gvariable().messageAlertDialog(this, "ไม่พบข้อมูล Part ใน Order.", layoutInflater)
                        }
                    }
                    else{
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "ไม่พบข้อมูลการรับ Part.", layoutInflater)
                    }
                }catch (e:Exception){
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "ไม่สามารถใช้ Serial KB ในหน้าจอนี้ได้", layoutInflater)
                }
            }
        }
    }

    private fun listOrderNoByPDS(pds:String, partNo:String){
        orderNoList.clear()
        orderNoList = DeliveryQuery().showOrderByPds(pds, partNo, "")
        Handler(Looper.getMainLooper()).post{
            textViewPds.text = "PDS No : $pds"
        }
    }

    private fun listOrderNoByPHT(partNo:String){
        orderNoList.clear()
        orderNoList = DeliveryQuery().showOrderByOrder(partNo)
    }

}