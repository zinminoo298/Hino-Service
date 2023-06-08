package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony.Mms.Part
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataQuery.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class Packing : AppCompatActivity() {
    companion object{
        lateinit var cardViewBack: CardView
        lateinit var spinnerCaseNo: Spinner
        lateinit var editTextKB: EditText
        lateinit var editTextQty: EditText
        lateinit var editTextSticker: EditText
        lateinit var editTextCheckOrder: EditText
        lateinit var textViewDate: TextView
        lateinit var spinnerOrderNo: Spinner
        lateinit var textViewPartNo: TextView
        lateinit var textViewBarcodeType: TextView
        lateinit var buttonSave: Button
        var countCaseNo = 0
        var date = ""
        var currentDate =""
        var PartNo = ""
        var OrderNo = ""
        var orderNoList = ArrayList<String>()
        var caseNoList = ArrayList<String>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_packing)

        cardViewBack = findViewById(R.id.cardView_back)
        spinnerCaseNo = findViewById(R.id.spinner_case_no)
        spinnerOrderNo = findViewById(R.id.spinner_order_no)
        editTextKB = findViewById(R.id.editText_kb)
        editTextQty = findViewById(R.id.editText_qty)
        editTextSticker = findViewById(R.id.editText_sticker)
        editTextCheckOrder = findViewById(R.id.editText_check_order)
        textViewDate = findViewById(R.id.textview_date)
        textViewPartNo = findViewById(R.id.textView_part_no)

        asyncOnLoad()

        editTextKB.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(editTextQty.text.toString().isEmpty()) {

                    if (caseNoList.isNotEmpty()) {
                        if (editTextKB.text.toString().isNotEmpty()) {
                            PartNo = ""
                            //check serial Barcode
                            if (checkSerialBarcode()) {
                                var barcodeType = textViewBarcodeType.text.toString()
                                when (barcodeType) {
                                    "2D" -> {
                                        val inputBarcode = editTextKB.text.toString().split("|").toTypedArray()
                                        if(inputBarcode.isNotEmpty()){
                                            editTextQty.setText(inputBarcode[13])
                                            val kb = editTextKB.text.toString().trim()
                                            PartNo = kb.substring(2,7)+"-"+kb.substring(7,12)
                                            // button save visible
                                            // edittextQty visible
                                            //spinnerOrder not visible
                                            // lblW not visible
                                        }
                                    }

                                    "PHT" -> {
                                        val partNo = editTextKB.text.toString().trim().uppercase()
                                        PartNo = partNo.substring(0,10)
                                        // button save visible
                                        // edittextQty visible
                                        //spinnerOrder not visible
                                        // lblW not visible
                                    }

                                    "SKB" -> {
                                        asyncSKB()
                                    }
                                }

                                checkSKB()
                                Handler(Looper.getMainLooper()).post(){
                                    editTextKB.setText(PartNo)
                                    editTextSticker.selectAll()
                                    editTextSticker.requestFocus()
                                }
                            } else {
                                Gvariable().messageAlertDialog(
                                    this,
                                    "กรุณาแสกนเอกสาร Kanban",
                                    layoutInflater
                                )
                                Gvariable().alarm(this)
                                editTextKB.selectAll()
                                editTextKB.requestFocus()
                            }
                        } else {
                            Gvariable().messageAlertDialog(
                                this,
                                "กรุณาแสกนเอกสาร Serial Kanban เท่านั้น",
                                layoutInflater
                            )
                            Gvariable().alarm(this)
                            editTextKB.requestFocus()
                        }
                    } else {
                        Gvariable().messageAlertDialog(
                            this,
                            "กรุณาเลือก CaseNo",
                            layoutInflater
                        )
                        Gvariable().alarm(this)
                    }
                }
                else{
                    Gvariable().messageAlertDialog(
                        this,
                        "Error: Qty < 0!!",
                        layoutInflater
                    )
                    Gvariable().alarm(this)
                    editTextQty.requestFocus()
                }
            }
            false
        })

        editTextQty.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                editTextSticker.requestFocus()
                editTextSticker.selectAll()
            }
            false
        })

        editTextSticker.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(editTextSticker.text.toString().isNotEmpty()){
                    if(Integer.parseInt(editTextQty.text.toString()) > 0){
                        if(editTextSticker.text.toString().substring(0,9) == (PartNo.substring(0,4)+ PartNo.substring(6,11))){
                            var barType = textViewBarcodeType.text.toString().trim()
                            var partNo = editTextKB.text.toString().trim()
                            textViewPartNo.text = partNo
                            if(barType == "2D" || barType == "PHT"){
                                editTextCheckOrder.isEnabled = false
                                //date time picker invisible
                                //label4 invisible
                                //buttonviewfullorder invisible
                               asyncListOrderNo(partNo)
                            }
                            else{
                                editTextCheckOrder.requestFocus()
                            }
                        }else{
                            Gvariable().alarm(this)
                            Gvariable().messageAlertDialog(this, "เอกสาร Sticker ไม่ตรงกับ PartNo", layoutInflater)
                            editTextSticker.requestFocus()
                            editTextSticker.selectAll()
                        }
                    } else{
                                                                                                                                            Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "Error: Qty <= 0", layoutInflater)
                        editTextQty.requestFocus()
                        editTextQty.selectAll()
                    }
                }else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "กรุณา Scan Sticker", layoutInflater)
                    editTextSticker.requestFocus()
                }
            }
            false
        })

        buttonSave.setOnClickListener {
            if(editTextKB.text.toString().isNotEmpty()){
                if(editTextSticker.text.toString().isNotEmpty()){
                    if(Integer.parseInt(editTextQty.text.toString()) > 0){
                        if(orderNoList.isNotEmpty()){
                            if(caseNoList.isNotEmpty()){
                                val caseOrder = spinnerCaseNo.selectedItem.toString().substring(0,3)
                                val drpOrder = spinnerOrderNo.selectedItem.toString().substring(0,3)
                                if(caseOrder == drpOrder){
                                    asyncButtonSave()
                                }
                                else{
                                    Gvariable().alarm(this)
                                    Gvariable().messageAlertDialog(this, "ประเภท Order ไม่ถูกต้อง", layoutInflater)
                                }
                            }else{
                                Gvariable().alarm(this)
                                Gvariable().messageAlertDialog(this, "กรุณาเลือก Case No.", layoutInflater)
                            }
                        }else{
                            Gvariable().alarm(this)
                            Gvariable().messageAlertDialog(this, "กรุณาเลือก Order No.", layoutInflater)
                        }
                    }
                    else{
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "กรุณากรอกข้อมูล Pack Qty ต้องมากกว่า 0", layoutInflater)
                        editTextQty.requestFocus()
                    }
                }else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "กรุณาแสกน Sticker TMT", layoutInflater)
                    editTextSticker.requestFocus()
                }
            }else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "กรุณาแสกน Scan KB", layoutInflater)
                editTextKB.requestFocus()
            }
        }


    }

    private fun asyncButtonSave(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            buttonSave()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Packing)
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

    private fun asyncListOrderNo(partNo:String){

        val deferred = lifecycleScope.async(Dispatchers.IO) {
            orderNoList.clear()
            listOrderNo("" , partNo, "PACKING")
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Packing)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    loadSpinnerOrderNo()
                    spinnerOrderNo.requestFocus()
                    Gvariable().alarm(this@Packing)
                    Gvariable().messageAlertDialog(this@Packing, "กรุณาเลือก Order No.", layoutInflater)
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun asyncSKB(){

        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val partNo = editTextKB.text.toString().trim().uppercase()
            PartNo = partNo.substring(0,10)
            editTextQty.isEnabled = false
            //btn save not visible
            editTextQty.setText("1")
            // txt corder is enabled
            //date time picker visible
            // label 4 visible

            var orderDetail = OrderDetailQuery().getOrderDetailBySerial("PACKING", PartNo)
            var orderDetailList = orderDetail.split("|").toTypedArray()
            var orderDetailId = ""
            var orderNo = ""
            var hiddOrderNo = ""
            if(orderDetailList.size == 3){
                //date = orderDetail[2].trim()
                //set date to datepicker
                orderDetailId = orderDetailList[0].trim()
                orderNo = orderDetailList[1].trim()

                hiddOrderNo = orderNo.substring(0,5)+"XXX"
                //clear spinner order
                //add hiddOrderNo to spinner orde

                // txtfullorder.text = orderNo
                textViewPartNo.text = PartNo

//                                            ViewFullOrder.txtCDate.Value = New Date(Mid(_date, 1, 4), Mid(_date, 5, 2), Mid(_date, 7, 2))
//                                            ViewFullOrder.CboOrder.Items.Clear()
//                                            ViewFullOrder.CboOrder.Items.Add(OrderNo)
//                                            ViewFullOrder.CboOrder.SelectedIndex = 0
//
//                                            ViewFullOrder.txtpartno.Text = _PartNo
                OrderNo = orderNo
                // show color part no
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Packing)
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

    private fun asyncOnLoad(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val getDate = GetTimeQuery().timeServer()
            val date = getDate.split("|").toTypedArray()[0]
            val date1 = getDate.split("|").toTypedArray()[1]
            currentDate = date
            listOrderNo(date1,"","")
            listCaseNo()

        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Packing)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    textViewDate.text = currentDate
                    loadSpinnerCaseNo()
                    loadSpinnerOrderNo()
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    // TO DO SHOW COLOR

    private fun buttonSave(){
        var sOrderNo = spinnerOrderNo.selectedItem.toString()
        var partNo = editTextKB.text.toString().trim().uppercase()
        if(PackingQuery().checkPartAvailable(partNo, sOrderNo)){
            var strTotal = ""
            var orderDetailId = ""
            var orderDetail = ""
            var orderNo = ""
            var orderQty = 0
            var orderProcessQty = 0
            var newQty = 0

            val orderList = OrderProcessQuery().getOrder(partNo, sOrderNo, "")
            if(orderList.isNotEmpty()){
                orderQty = OrderProcessQuery().getSumQtyByPid(orderList[0].pId!!, "ReceiveQty")
                orderProcessQty = try{
                    OrderProcessQuery().getSumQtyByPid(orderList[0].pId!!, "PackQty")
                }catch (e:Exception){
                    e.printStackTrace()
                    0
                }
                newQty = orderProcessQty + Integer.parseInt(editTextQty.text.toString())

                if(orderQty > newQty){
                    var chk = false
                    var edpPart = false
                    for(i in 0 until orderList.size){
                        edpPart = PackingQuery().isEDP(orderList[i].partNo!!)
                        if(chk){
                            break
                        }
                        else{

                        }
                    }
                }
                else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "Error: Over Qty \n $newQty/$orderQty", layoutInflater)
                    Handler(Looper.getMainLooper()).post() {
                        editTextQty.requestFocus()
                    }
                }
            }
            else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "ไม่พบ Serial No. ใน Order.", layoutInflater)
                Handler(Looper.getMainLooper()).post() {
                    editTextKB.selectAll()
                    editTextKB.requestFocus()
                }
            }
        }else{
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "ไม่พบ PartNo ใน OrderNo :\n $sOrderNo", layoutInflater)
            Handler(Looper.getMainLooper()).post(){
                editTextKB.selectAll()
                editTextSticker.text.clear()
                editTextQty.setText("1")
                //DateTimePicker1.Enabled = True
                editTextQty.isEnabled = true
                orderNoList.clear()
                loadSpinnerOrderNo()
            }
        }
    }

    private fun checkSerialBarcode() : Boolean {
        editTextKB.setText(editTextKB.text.toString().uppercase().trim())
        var inputBarcode = editTextKB.text.toString().split("|").toTypedArray()
        var partNo = ""
        var serialKanban = ""
        var docSerial = ""

        if(inputBarcode.size == 18){
            var pageKanban = inputBarcode[14].split("/").toTypedArray()
            serialKanban = String.format("%04d", Integer.parseInt(pageKanban[0]))
            partNo = inputBarcode[0].substring(3,8)+"-"+inputBarcode[0].substring(8,13)
            docSerial = partNo +"-"+inputBarcode[11]+"-"+serialKanban
            PartNo = docSerial
            textViewBarcodeType.text = "2D"
        }
        else if (editTextKB.text.toString().length == 15){
            PartNo = editTextKB.text.toString()
            textViewBarcodeType.text = "SKB"
        }

        else if(editTextKB.text.toString().length == 11){
            PartNo = editTextKB.text.toString()
            textViewBarcodeType.text = "PHT"
            docSerial = PartNo
        }
        else{
            return false
        }
        return true
    }


    private fun checkSKB(){
        if(PackingQuery().isSKB(PartNo)){
            if(editTextKB.text.toString().length == 15){
                if(PackingQuery().checkPartAvailable(PartNo, "")){
                    //OK
                }
                else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "ไม่พบ Part No. หรือ Serial KB สำหรับ Packing", layoutInflater)
                    Handler(Looper.getMainLooper()).post(){
                        editTextKB.selectAll()
                        editTextKB.requestFocus()
                        textViewPartNo.text = ""
                        editTextQty.isEnabled = true
                        //btn save visible
                    }
                }
            }
            else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "Part No. เป็น SKB \n กรุณาใช้เอกสาร Serial Kanban", layoutInflater)
                Handler(Looper.getMainLooper()).post(){
                    editTextKB.setText("")
                    editTextKB.requestFocus()
                }
            }
        }
    }

    fun listOrderNo(orderDate:String, partNo:String, process:String){
        orderNoList.clear()
        val showOrderList = DeliveryQuery().showOrder( orderDate, partNo, process)
        for(i in 0 until showOrderList.size){
            orderNoList.add(showOrderList[i].substringBefore("|"))
        }
    }

    fun listCaseNo(){
        caseNoList.clear()
        caseNoList = PackingQuery().getListCaseNoByUser(Gvariable.userName.toString())
    }

    private fun loadSpinnerOrderNo(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orderNoList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderNo.adapter = arrayAdapter
    }

    private fun loadSpinnerCaseNo(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, caseNoList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCaseNo.adapter = arrayAdapter
    }


}