package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony.Mms.Part
import android.text.InputFilter
import android.text.Spanned
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
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
        lateinit var buttonSaveCase: Button
        lateinit var buttonListCase: Button
        lateinit var buttonListOrder: Button
        lateinit var buttonUp: Button
        lateinit var buttonDown: Button
        var countCaseNo = 0
        var date = ""
        var currentDate =""
        var PartNo = ""
        var OrderNo = ""
        var fullOrder = ""
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
        textViewBarcodeType = findViewById(R.id.textview_barcode_type)
        buttonSaveCase = findViewById(R.id.btn_save_case)
        buttonListCase = findViewById(R.id.btn_list_case)
        buttonListOrder = findViewById(R.id.btn_list_order)
        buttonSave = findViewById(R.id.btn_save)
        buttonUp = findViewById(R.id.button_up)
        buttonDown = findViewById(R.id.button_down)


        asyncOnLoad()

        editTextQty.setText("1")
        editTextQty.filters = arrayOf<InputFilter>(MinMaxFilter(1,10000))

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
                                            buttonSave.isVisible = true
                                            editTextQty.isEnabled = true
                                            editTextCheckOrder.isEnabled = false
                                            // lblW not visible
                                        }
                                        asyncCheckSKB()
                                    }

                                    "PHT" -> {
                                        val partNo = editTextKB.text.toString().trim().uppercase()
                                        PartNo = partNo.substring(0,10)
                                        buttonSave.isVisible = true
                                        editTextQty.isEnabled = true
                                        editTextCheckOrder.isEnabled = false
                                        // lblW not visible
                                        asyncCheckSKB()
                                    }

                                    "SKB" -> {
                                        asyncSKB()
                                    }
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
                                editTextKB.nextFocusDownId = editTextKB.id
                            }
                        } else {
                            Gvariable().messageAlertDialog(
                                this,
                                "กรุณาแสกนเอกสาร Serial Kanban เท่านั้น",
                                layoutInflater
                            )
                            Gvariable().alarm(this)
                            editTextKB.requestFocus()
                            editTextKB.nextFocusDownId = editTextKB.id
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
                        if(editTextSticker.text.toString().substring(0,10) == (PartNo.substring(0,5)+ PartNo.substring(6,11))){
                            val barType = textViewBarcodeType.text.toString().trim()
                            val partNo = editTextKB.text.toString().trim()
                            textViewPartNo.text = partNo
                            if(barType == "2D" || barType == "PHT"){
//                                editTextCheckOrder.isEnabled = false
                                //label4 invisible
                                //buttonviewfullorder disable
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
                            editTextSticker.nextFocusDownId = editTextSticker.id
                        }
                    } else{
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "Error: Qty <= 0", layoutInflater)
                        editTextQty.requestFocus()
                        editTextQty.selectAll()
                        editTextSticker.nextFocusDownId = editTextQty.id
                    }
                }else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "กรุณา Scan Sticker", layoutInflater)
                    editTextSticker.requestFocus()
                    editTextSticker.nextFocusDownId = editTextSticker.id
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

        editTextCheckOrder.setOnKeyListener(View.OnKeyListener {_,_,event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(editTextSticker.text.toString().trim() != ""){
                    if(editTextSticker.text.toString().substring(1,11) == (PartNo.substring(1,6)+ PartNo.substring(6,12))){
                        val serialOrder = editTextCheckOrder.text.toString()
                        if(serialOrder.length == 3){
                            asyncCheckOrder()
                        }else{
                            Gvariable().alarm(this)
                            Gvariable().messageAlertDialog(this, "กรณาใส่เลข 3 หลักบน Sticker", layoutInflater)
                            editTextCheckOrder.selectAll()
                            editTextCheckOrder.requestFocus()
                        }
                    }
                    else{
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "เอกสาร Sticker ไม่ตรงกับข้อมูล", layoutInflater)
                        editTextSticker.selectAll()
                        editTextSticker.requestFocus()
                    }
                }
                else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "กรุณา Scan Sticker", layoutInflater)
                    editTextSticker.selectAll()
                    editTextSticker.requestFocus()
                }
            }

            false
        })

        buttonSaveCase.setOnClickListener {
            val sCaseNo = spinnerCaseNo.selectedItem.toString()

            if(sCaseNo.isNotEmpty()){
                messageDialog(sCaseNo)
            }else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "กรุณาเลือก CaseNo", layoutInflater)
            }
        }

        buttonUp.setOnClickListener {
            if(editTextQty.text.toString().isEmpty()){
                editTextQty.setText("1")
            }
            else{
                val qty = Integer.parseInt(editTextQty.text.toString())
                if(qty >= 10000){
                    editTextQty.setText("10000")
                }
                else{
                    editTextQty.setText("${ (Integer.parseInt(editTextQty.text.toString()) + 1) }")
                }
            }
        }

        buttonDown.setOnClickListener {
            if(editTextQty.text.toString().isEmpty()){
                editTextQty.setText("1")
            }
            else{
                val qty = Integer.parseInt(editTextQty.text.toString())
                if(qty <= 1){
                    editTextQty.setText("1")
                }
                else{
                    editTextQty.setText("${ (Integer.parseInt(editTextQty.text.toString()) - 1) }")
                }
            }
        }

        buttonListCase.setOnClickListener {
            asyncListCase(spinnerCaseNo.selectedItem.toString())
        }

        buttonListOrder.setOnClickListener {
            asyncListOrderNo(PartNo)
        }

        cardViewBack.setOnClickListener {
            finish()
            super.onBackPressed()
        }

    }

    private fun asyncListCase(sCaseNo:String){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            ListCase.caseList.clear()
            ListCase.caseList = PackingQuery().getListPackingInfoByCaseNo(sCaseNo)
            ListCase.total = 0
            ListCase.total = PackingQuery().getTotalPackQtyByCaseNo(sCaseNo)
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Packing)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    val intent = Intent(this@Packing, ListCase::class.java)
                    startActivity(intent)
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun asyncCheckSKB(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            checkSKB()
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
                loadSpinnerOrderNo()
                spinnerOrderNo.requestFocus()
                Gvariable().alarm(this@Packing)
                Gvariable().messageAlertDialog(this@Packing, "กรุณาเลือก Order No.", layoutInflater)
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
                fullOrder = orderNo
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
            checkSKB()
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
            val calendarDate = getDate.split("|").toTypedArray()[0]
            val date1 = getDate.split("|").toTypedArray()[1]
            currentDate = calendarDate
//            date = date1
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
                textViewDate.text = currentDate
                loadSpinnerCaseNo()
                loadSpinnerOrderNo()

            }
        }
    }

    private fun asyncSaveCaseNo(sCaseNo: String){
        var sFlag = false
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            sFlag = PackingQuery().updateCaseFlag(sCaseNo, Gvariable.userName.toString())
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Packing)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    progressDialogBuilder.cancel()
                    if(sFlag){
                        Gvariable().messageOkDialog(this@Packing, "บันทึก CaseNo เรียบร้อย", layoutInflater)
                        orderNoList.clear()
                        loadSpinnerOrderNo()
                        editTextKB.setText("")
                        editTextSticker.setText("")
                    }
                }
            } else {
                deferred.await()

            }
        }
    }
    private fun asyncCheckOrder(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            checkOrder()
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

    // TO DO SHOW COLOR

    private fun checkOrder(){
        val subStringFullOrder = fullOrder.substring(4,8)
        val serialOrder = editTextCheckOrder.text.toString().trim()
        var orderNo = ""
        var orderQty = 0
        var orderProcessQty = 0
        var newQty = 0
        var strTotal = ""
        if(serialOrder == subStringFullOrder){
            //Save
            orderNo = fullOrder
            var orderList = OrderProcessQuery().getOrder(PartNo, orderNo, date)
            orderQty = OrderProcessQuery().getSumQtyByPid(orderList[0].pId!!, "ReceiveQty")
            if(orderQty != 0){
                try{
                    orderProcessQty = OrderProcessQuery().getSumQtyByPid(orderList[0].pId!!, "PackQty")
                }catch (e:Exception){
                    orderProcessQty = 0
                    e.printStackTrace()
                }

                newQty = orderProcessQty + Integer.parseInt(editTextQty.text.toString())

                when {
                    newQty < 0 -> {
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "Error: Qty < 0", layoutInflater)
                        Handler(Looper.getMainLooper()).post(){
                            editTextQty.requestFocus()
                            editTextQty.selectAll()
                        }
                    }
                    newQty > orderQty -> {
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "Error: Over Qty \n ($newQty / $orderQty", layoutInflater)
                        Handler(Looper.getMainLooper()).post(){
                            editTextQty.requestFocus()
                            editTextQty.selectAll()
                        }
                    }
                    else -> {
                        var dCaseNo = spinnerCaseNo.selectedItem.toString().substring(1,5)
                        var dOrderNo = spinnerOrderNo.selectedItem.toString().substring(1,5)
                        if(dCaseNo == dOrderNo){
                            if(orderList.isNotEmpty()){
                                var chk = false
                                var edpPart = false
                                for (i in 0 until orderList.size){
                                    edpPart = PackingQuery().isEDP(orderList[i].partNo!!)
                                    if(chk){
                                        break
                                    }else{
                                        when {
                                            orderList[i].receiveDate.isNullOrEmpty() -> {
                                                Gvariable().alarm(this)
                                                Gvariable().messageAlertDialog(this, "Serial No. ยังไม่ผ่านการรับ Parts", layoutInflater)
                                                Handler(Looper.getMainLooper()).post(){
                                                    editTextKB.setText("")
                                                    editTextSticker.setText("")
                                                    editTextQty.setText("1")
                                                    textViewPartNo.text = orderList[i].partNo
                                                    editTextKB.requestFocus()
                                                }
                                                break
                                            }
                                            edpPart && orderList[i].edpQualityCheckDate.isNullOrEmpty() -> {
                                                Gvariable().alarm(this)
                                                Gvariable().messageAlertDialog(this, "ชิ้นงานยังไม่ผ่านการตรวจสอบ QC-EDP (No QC from EDP Process)", layoutInflater)
                                                Handler(Looper.getMainLooper()).post(){
                                                    editTextKB.selectAll()
                                                    editTextKB.requestFocus()
                                                }
                                                break
                                            }
                                            else -> {
                                                if(orderList[i].packingDate!!.isNullOrEmpty()){
                                                    var sCaseNo = spinnerCaseNo.selectedItem.toString()
                                                    var sSerialNo = editTextKB.text.toString()
                                                    var flag = false
                                                    flag = PackingQuery().saveCaseNo(orderList[i].pId!!, sCaseNo, editTextQty.text.toString(), Gvariable.userName.toString())
                                                    if(flag){
                                                        OrderProcessQuery().updateOrderProcessPacking(sCaseNo, orderList[i].pId!!, editTextQty.text.toString(), Gvariable.userName.toString())
                                                        OrderProcessQuery().checkUpdateStatus(orderList[i].pId!!)
                                                        chk = true
                                                    }
                                                }
                                                strTotal = OrderProcessQuery().getSumQty(orderList[i].orderDetailId!!, "PackQty").toString()
                                                Handler(Looper.getMainLooper()).post(){
                                                    //lblW.Visible = False
                                                    editTextKB.setText("")
                                                    editTextSticker.setText("")
                                                    editTextQty.setText("1")
//                                    orderList.clear()
//                                    loadSpinnerOrderNo()
                                                    textViewPartNo.text = orderList[i].partNo
                                                    textViewBarcodeType.text = ""
                                                    editTextKB.selectAll()
                                                    editTextKB.requestFocus()
//                                                    btSave.Enabled = True
                                                }
                                            }
                                        }
                                    }
                                }
                            }else{
                                Gvariable().alarm(this)
                                Gvariable().messageAlertDialog(this, "ไม่พบ Serial No. ใน Order", layoutInflater)
                                Handler(Looper.getMainLooper()).post(){
                                    editTextKB.selectAll()
                                    editTextKB.requestFocus()
                                }
                            }
                        }else{
                            Gvariable().alarm(this)
                            Gvariable().messageAlertDialog(this, "Error: CaseNo ไม่ตรงกับ Orderno", layoutInflater)
                        }
                    }
                }

            }
            else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "ไม่พบ Part ใน Order!", layoutInflater)
                Handler(Looper.getMainLooper()).post(){
                    editTextSticker.setText("")
                    editTextKB.selectAll()
                    editTextKB.requestFocus()
                }
            }
        }
        else{
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "Sticker ไม่ตรงกับ Order", layoutInflater)
            Handler(Looper.getMainLooper()).post(){
                editTextCheckOrder.selectAll()
                editTextCheckOrder.requestFocus()
            }
        }
    }

    private fun  buttonSave(){
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
                            if(edpPart && orderList[i].edpQualityCheckDate.isNullOrEmpty()){
                                Gvariable().alarm(this)
                                Gvariable().messageAlertDialog(this, "ชิ้นงานยังไม่ผ่านการตรวจสอบ QC-EDP (No QC from EDP Process", layoutInflater)
                                Handler(Looper.getMainLooper()).post(){
                                    editTextKB.selectAll()
                                    editTextKB.requestFocus()
                                }
                            }
                            else{
                                var sCaseNo = spinnerCaseNo.selectedItem.toString()
                                var sCaseYear = PackingQuery().getMaxYearByCaseNo(sCaseNo)
                                var serialNo = editTextKB.text.toString()
                                var flag = false
                                PackingQuery().updateCaseStatus(sCaseNo, sCaseYear, "O")
                                if(textViewBarcodeType.text.toString() == "2D" || textViewBarcodeType.text.toString() == "PHT"){
                                    flag = PackingQuery().saveCaseNo(orderList[i].pId!!, sCaseNo, editTextQty.text.toString(), Gvariable.userName.toString())
                                    if(flag){
                                        OrderProcessQuery().updateOrderProcessPacking(sCaseNo, orderList[i].pId!!, newQty.toString(), Gvariable.userName.toString())
                                        chk = true
                                    }
                                }
                                strTotal = OrderProcessQuery().getSumQty(orderList[i].orderDetailId!!, "PackQty").toString()
                                Handler(Looper.getMainLooper()).post(){
                                    //lblW.Visible = False
                                    editTextKB.setText("")
                                    editTextSticker.setText("")
                                    editTextQty.setText("1")
//                                    orderList.clear()
//                                    loadSpinnerOrderNo()
                                    textViewPartNo.text = orderList[i].partNo
                                    textViewBarcodeType.text = ""
                                    editTextKB.selectAll()
                                    editTextKB.requestFocus()
                                }
                            }
                        }
                    }
                    if(chk){
                        //buttonSave is visible
                    }else{
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "แสกน Serial No. นี้เรียบร้อยแล้ว", layoutInflater)
                        Handler(Looper.getMainLooper()).post(){
                            //lblW.Visible = False
                            editTextKB.setText("")
                            editTextSticker.setText("")
                            editTextQty.setText("1")
                            orderNoList.clear()
                            loadSpinnerOrderNo()
                            textViewPartNo.text = orderList[0].partNo
                            editTextKB.selectAll()
                            editTextKB.requestFocus()
                        }
                    }
                    //lbTotal.Text = Strtotal '& " / " & ds.Rows(0).Item("Qty")
                    orderList.clear()
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
                    Handler(Looper.getMainLooper()).post(){
                        editTextKB.setText(PartNo)
                        editTextSticker.selectAll()
                        editTextSticker.requestFocus()
                        editTextKB.nextFocusDownId = editTextSticker.id
                    }
                }
                else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "ไม่พบ Part No. หรือ Serial KB สำหรับ Packing", layoutInflater)
                    Handler(Looper.getMainLooper()).post(){
                        editTextKB.selectAll()
                        editTextKB.requestFocus()
                        textViewPartNo.text = ""
                        editTextQty.isEnabled = true
                        buttonSave.isVisible = false
                    }
                }
            }
            else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "Part No. เป็น SKB \n กรุณาใช้เอกสาร Serial Kanban", layoutInflater)
                Handler(Looper.getMainLooper()).post(){
                    editTextKB.setText("")
                    editTextKB.requestFocus()
                    editTextKB.nextFocusDownId = editTextKB.id
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

    private fun messageDialog(sCaseNo:String){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.alert_dialog   , null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        val buttonYes = view.findViewById<Button>(R.id.button_yes)
        val buttonNo = view.findViewById<Button>(R.id.button_no)
        val textView = view.findViewById<TextView>(R.id.txt_text)

        textView.text = "บันทึก CaseNo: \n $sCaseNo?"

        buttonYes.setOnClickListener {
            dialog.dismiss()
            asyncSaveCaseNo(sCaseNo)
        }
        buttonNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        // Initialized
        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        // Check if input c is in between min a and max b and
        // returns corresponding boolean
        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }


}