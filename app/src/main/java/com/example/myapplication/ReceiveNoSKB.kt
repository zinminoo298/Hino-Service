package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.Spanned
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.isEmpty
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
        lateinit var buttonUp: Button
        lateinit var buttonDown: Button
        lateinit var cardViewBack: CardView
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
        buttonSave = findViewById(R.id.btn_save)
        buttonList = findViewById(R.id.button_list)
        buttonUp = findViewById(R.id.imageButton_up)
        buttonDown = findViewById(R.id.imageButton_down)
        cardViewBack = findViewById(R.id.cardView_back)

        asyncGetDate()
        editTextScanKanban.requestFocus()

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
            if(spinnerOrderNo.selectedItem.toString() == "Select"){
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "โปรดเลือก Order No.", layoutInflater)
            }
            else{
                asyncSave()
            }
        }

        buttonList.setOnClickListener {
            if(orderNoList.isEmpty()){
                Gvariable().messageAlertDialog(
                    this,
                    "โปรดเลือก Order No.",
                    layoutInflater
                )
                Gvariable().alarm(this)
            }else{
                asyncOrderList()
            }
        }

        cardViewBack.setOnClickListener{
            finish()
            super.onBackPressed()
        }

        editTextReceiveQty.setText("1")
        editTextReceiveQty.filters = arrayOf<InputFilter>(MinMaxFilter(1,10000))

        buttonUp.setOnClickListener {
            if(editTextReceiveQty.text.toString().isEmpty()){
                editTextReceiveQty.setText("1")
            }
            else{
                val qty = Integer.parseInt(editTextReceiveQty.text.toString())
                if(qty >= 100000){
                    editTextReceiveQty.setText("100000")
                }
                else{
                    editTextReceiveQty.setText("${ (Integer.parseInt(editTextReceiveQty.text.toString()) + 1) }")
                }
            }
        }

        buttonDown.setOnClickListener {
            if(editTextReceiveQty.text.toString().isEmpty()){
                editTextReceiveQty.setText("1")
            }
            else{
                val qty = Integer.parseInt(editTextReceiveQty.text.toString())
                if(qty <= 1){
                    editTextReceiveQty.setText("1")
                }
                else{
                    editTextReceiveQty.setText("${ (Integer.parseInt(editTextReceiveQty.text.toString()) - 1) }")
                }
            }
        }

        spinnerOrderNo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(spinnerOrderNo.selectedItem.toString() != "Select"){
                    asyncSpinnerChange()
                }
            }
        }
    }

    private fun asyncSpinnerChange(){
        var recQty = 0
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            var orderNo = ""
            if(orderNoList.isNotEmpty()){
                orderNo = spinnerOrderNo.selectedItem.toString()
            }
            val inputBarcode = editTextScanKanban.text.toString().split("|").toTypedArray()
            var partNo = ""
            partNo = if(inputBarcode.isNotEmpty()){
                inputBarcode[0].substring(2, 7) +"-"+ inputBarcode[0].substring(7, 12)
            } else{
                editTextScanKanban.text.toString()
            }
            recQty = OrderProcessQuery().getQtyOrder(orderNo, partNo)
            if(recQty != 0){
                Handler(Looper.getMainLooper()).post(){
                    editTextReceiveQty.setText(recQty.toString())
                    editTextReceiveQty.requestFocus()
                }
            }
            else{
                Gvariable().alarm(this@ReceiveNoSKB)
                Gvariable().messageAlertDialog(this@ReceiveNoSKB,"ไม่พบ Part No. ใน Order นี้", layoutInflater)
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveNoSKB)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
//                    if(recQty != 0){
//                        editTextReceiveQty.setText(recQty)
//                        editTextReceiveQty.requestFocus()
//                    }
//                    else{
//                        Gvariable().alarm(this@ReceiveNoSKB)
//                        Gvariable().messageAlertDialog(this@ReceiveNoSKB,"ไม่พบ Part No. ใน Order นี้", layoutInflater)
//                    }
                    progressDialogBuilder.cancel()

                }
            } else {
                deferred.await()
            }
        }
    }

    private fun asyncOrderList(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            ListOrder.orderList.clear()
            ListOrder.orderList = OrderDetailQuery().loadDataOrderDetail(spinnerOrderNo.selectedItem.toString())
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ReceiveNoSKB)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    progressDialogBuilder.cancel()
                    val intent = Intent(this@ReceiveNoSKB, ListOrder::class.java)
                    startActivity(intent)
                }
            } else {
                deferred.await()
            }
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
//                    if(orderNoList.isNotEmpty()){
//                        asyncSpinnerChange()
//                    }
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun scanKanBan(){
        var inputBarcode = editTextScanKanban.text.toString().split("|").toTypedArray()
        if(inputBarcode.isNotEmpty()){
            partNo = inputBarcode[0].substring(2, 7) +"-"+ inputBarcode[0].substring(7, 12)
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
                    Handler(Looper.getMainLooper()).post {
                        editTextReceiveQty.setText("1")
                    }
                    listOrderNoByPHT(partNo)
                }

            }catch (e:Exception){
                e.printStackTrace()
                listOrderNoByPHT(partNo)
                Handler(Looper.getMainLooper()).post {
                    editTextReceiveQty.setText("1")
                }
            }

            if(spinnerOrderNo.isEmpty()){
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
        Handler(Looper.getMainLooper()).post(){
            setSpinnerOrderNo()
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
                    editTextScanKanban.selectAll()
                    editTextScanKanban.requestFocus()
                    progressDialogBuilder.cancel()
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
                        partNo = "${inputBarcode[0].substring(2,7)}-${inputBarcode[0].substring(7,12)}"
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
                                    Handler(Looper.getMainLooper()).post(){
                                        textViewTotalReceive.text = orderProcessQty.toString()
                                    }
                                }
                                else -> {
                                    if(getOrderList.isEmpty()){
                                        if(OrderProcessQuery().save(Gvariable.uniqueId, orderDetailId, orderNo, docSerial)){
                                            OrderProcessQuery().updateOrderProcessReceive(Gvariable.uniqueId, Integer.parseInt(editTextReceiveQty.text.toString()))
                                            Handler(Looper.getMainLooper()).post {
                                                textViewPartNo.text = partNo
                                                editTextReceiveQty.setText("1")
                                                Handler(Looper.getMainLooper()).post(){
                                                    textViewTotalReceive.text = newQty.toString()
                                                }
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
                                                        textViewTotalReceive.text = strTotal
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
                                                textViewTotalReceive.text = strTotal
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