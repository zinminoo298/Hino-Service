package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.Spanned
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.PackingInfoListAdapter
import com.example.myapplication.DataModel.GetPackingInfoListModel
import com.example.myapplication.DataQuery.PackingQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CheckListCase : AppCompatActivity() {
    companion object{
        lateinit var textviewTotal: TextView
        lateinit var textViewPartNo: TextView
        lateinit var editTextCaseNo: EditText
        lateinit var editTextQty: EditText
        lateinit var spinnerOrderNo: Spinner
        lateinit var buttonUp: Button
        lateinit var buttonDown: Button
        lateinit var buttonSave: Button
        lateinit var packingInfoList: ArrayList<GetPackingInfoListModel>
        lateinit var recyclerView: RecyclerView
        lateinit var viewAdapter: RecyclerView.Adapter<*>
        lateinit var viewManager: RecyclerView.LayoutManager
        lateinit var cardView: CardView
        var orderNoList = ArrayList<String>()
        var noSKBUpdate = false
        var selectedPartNo = ""
        var loadRecyclerCase = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_list_case)

        textviewTotal = findViewById(R.id.textview_total)
        editTextCaseNo = findViewById(R.id.editText_case)
        editTextQty = findViewById(R.id.editText_qty)
        spinnerOrderNo = findViewById(R.id.spinner_orderNo)
        textViewPartNo = findViewById(R.id.textview_part_no)
        buttonUp = findViewById(R.id.imageButton_up)
        buttonDown = findViewById(R.id.imageButton_down)
        buttonSave = findViewById(R.id.button_save)
        recyclerView = findViewById(R.id.recyclerview)
        cardView = findViewById(R.id.cardView_back)
        packingInfoList = ArrayList<GetPackingInfoListModel>()

        editTextQty.filters = arrayOf<InputFilter>(MinMaxFilter(0,999))

        editTextCaseNo.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val sCaseNo = editTextCaseNo.text.toString().trim()
                if(sCaseNo.isNotEmpty()){
                    if(sCaseNo.length < 11){
                        asyncScanCaseNo(sCaseNo)
                    }
                    else{
                        Gvariable().messageAlertDialog(this, "ไม่ใช่เอกสาร CaseNo", layoutInflater)
                        Gvariable().alarm(this)
                        editTextCaseNo.selectAll()
                        editTextCaseNo.requestFocus()
                    }
                }
                else{
                    Gvariable().messageAlertDialog(this, "กรุณาแสกน CaseNo", layoutInflater)
                    Gvariable().alarm(this)
                    editTextCaseNo.selectAll()
                    editTextCaseNo.requestFocus()
                }
            }
            false
        })

        buttonUp.setOnClickListener {
            if(editTextQty.text.toString().isEmpty()){
                editTextQty.setText("1")
            }
            else{
                val qty = Integer.parseInt(editTextQty.text.toString())
                if(qty >= 999){
                    editTextQty.setText("999")
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
                if(qty <= 0){
                    editTextQty.setText("0")
                }
                else{
                    editTextQty.setText("${ (Integer.parseInt(editTextQty.text.toString()) - 1) }")
                }
            }
        }

        buttonSave.setOnClickListener {
            if(editTextQty.text.toString().isEmpty()){
                editTextQty.setText("0")
            }
            var sCaseNo = editTextCaseNo.text.toString()

            if(sCaseNo.isNotEmpty()){
                if(selectedPartNo.isNotEmpty()){
                    alertDialog()
                }
                else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "โปรดเลือก Part No.", layoutInflater)
                }
            }else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "กรุณาแสกน CaseNo", layoutInflater)
            }
        }

        cardView.setOnClickListener {
            finish()
            super.onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun asyncScanCaseNo(caseNo: String){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val sCaseNoStatus = PackingQuery().getCaseNoStatus(caseNo)
            if(sCaseNoStatus != 1){
                //clear spinners
                //set qty = 0
                packingInfoList.clear()
                packingInfoList = PackingQuery().getListPackingInfoByCaseNo(caseNo)
                if(packingInfoList.isNotEmpty()){
                    val qty = PackingQuery().getTotalPackQtyByCaseNo(caseNo).toString()+" K/B "
                    Handler(Looper.getMainLooper()).post {
                        textviewTotal.text = qty
                    }
                }
            }else{
                Gvariable().messageAlertDialog(this@CheckListCase, "CaseNO: $caseNo ปิดเรียบร้อยแล้ว", layoutInflater)
                Gvariable().alarm(this@CheckListCase)
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@CheckListCase)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    loadRecyclerView()
                    if(packingInfoList.isEmpty()){
                        Gvariable().alarm(this@CheckListCase)
                        Gvariable().messageAlertDialog(this@CheckListCase, "ไม่พบข้อมูลการจัดชิ้นส่วน", layoutInflater)
                        textviewTotal.text = "0 K/B "
                    }
                    editTextCaseNo.requestFocus()
                    editTextCaseNo.selectAll()
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }
    private fun loadRecyclerView(){
        viewManager = LinearLayoutManager(this)
        viewAdapter = PackingInfoListAdapter(packingInfoList, this)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
    fun asyncRecyclerItemChange(context: Context, sPartNo: String, sQty: Int){
        selectedPartNo =  sPartNo
        var qty = 0
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val sCaseNo = editTextCaseNo.text.toString().trim()
            loadListOrderNo(sCaseNo, sPartNo)
            qty = if(sPartNo.length == 15){
                1
            }else{
                PackingQuery().getQtyByCaseNo(sCaseNo, orderNoList[0],sPartNo)
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(context)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    //reload Spinner
                    setOrderNoSpinner(context)
                    if(sPartNo.length == 15){
                        textViewPartNo.text = sPartNo.trimEnd().substring(0,11)
                    }else{
                        textViewPartNo.text = sPartNo.trimEnd()
                    }
                    editTextQty.setText(qty.toString())
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun loadListOrderNo(caseNo: String, partNo: String){
        orderNoList.clear()
        orderNoList = PackingQuery().getOrderNoByCaseNo(caseNo, partNo)

    }
    private fun setOrderNoSpinner(context: Context){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, orderNoList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderNo.adapter = arrayAdapter
    }

    private fun asyncUpdateNoSKB(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val sCaseNo = editTextCaseNo.text.toString()
            val sPartNo = textViewPartNo.text.toString()
            val sOrderNo = spinnerOrderNo.selectedItem.toString()
            val qty = Integer.parseInt(editTextQty.text.toString().trim())
            PackingQuery().updatePackingInformationNoSKB(sCaseNo, sOrderNo, sPartNo, qty, Gvariable.userName.toString())
            if(PackingQuery().updateOrderProcessNoSKB(sOrderNo, sPartNo, qty, Gvariable.userName.toString(), sCaseNo)){
                Gvariable().messageOkDialog(this@CheckListCase,"บันทึกข้อมูลเรียบร้อย", layoutInflater)
                Handler(Looper.getMainLooper()).post(){
                    textViewPartNo.text = ""
                    editTextQty.setText("0")
                    orderNoList.clear()
                    setOrderNoSpinner(this@CheckListCase)
                }
            }
            packingInfoList.clear()
            packingInfoList = PackingQuery().getListPackingInfoByCaseNo(sCaseNo)
            if(packingInfoList.isNotEmpty()){
                val qty = PackingQuery().getTotalPackQtyByCaseNo(sCaseNo).toString()+" K/B "
                Handler(Looper.getMainLooper()).post {
                    textviewTotal.text = qty
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@CheckListCase)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    loadRecyclerView()
                    selectedPartNo = ""
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun asyncSave(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            save()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@CheckListCase)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    if (loadRecyclerCase){
                        selectedPartNo = ""
                        loadRecyclerView()
                    }
                    progressDialogBuilder.cancel()
                    if(noSKBUpdate){
                        alertDialogNoSKBUpdate()
                    }
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun save(){
        try{
            noSKBUpdate = false
            val sCaseNo = editTextCaseNo.text.toString()
            val sPartNo = selectedPartNo
            val sOrderNo = spinnerOrderNo.selectedItem.toString()
            val qty = Integer.parseInt(editTextQty.text.toString().trim())
            var recQty = 0

            //case Serial K/B
            if(sPartNo.length == 15){
                recQty = PackingQuery().getQtyInOrder(sOrderNo, sPartNo)
                when {
                    qty > recQty -> {
                        loadRecyclerCase = false
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "จำนวนต้องไม่เกิน Receive Qty", layoutInflater)
                        Handler(Looper.getMainLooper()).post(){
                            editTextQty.setText("1")
                            editTextQty.requestFocus()
                        }
                    }

                    qty > 1 -> {
                        loadRecyclerCase = false
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "กรณี Serial No. จำนวนต้องไม่เกิน 1", layoutInflater)
                        Handler(Looper.getMainLooper()).post(){
                            editTextQty.setText("1")
                            editTextQty.requestFocus()
                        }
                    }

                    qty == 0 -> {
                        loadRecyclerCase = true
                        if(PackingQuery().updateOrderProcessSKB(sOrderNo, sPartNo)){
                            PackingQuery().deletePackingInformationSKB(sCaseNo, sOrderNo, sPartNo)
                            Gvariable().messageOkDialog(this, "บบันทึกข้อมูลเรียบร้อย", layoutInflater)
                            Handler(Looper.getMainLooper()).post(){
                                orderNoList.clear()
                                setOrderNoSpinner(this)
                                textViewPartNo.text = ""
                                editTextQty.setText("0")
                            }
                        }
                    }

                    else -> {
                        loadRecyclerCase = true
                        Gvariable().messageOkDialog(this, "บันทึกข้อมูลเรียบร้อย", layoutInflater)
                        Handler(Looper.getMainLooper()).post(){
                            orderNoList.clear()
                            setOrderNoSpinner(this)
                            textViewPartNo.text = ""
                            editTextQty.setText("0")
                        }
                    }
                }
            }
            //Case Non Serial K/B
            else{
                recQty = PackingQuery().getQtyInOrder(sOrderNo, sPartNo)
                val packQty:Int = PackingQuery().getTotalPackQtyNotInCaseNo(sOrderNo, sPartNo, sCaseNo)
                if( (qty + packQty) > recQty){
                    loadRecyclerCase = false
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "จำนวนต้องไม่เกิน Receive Qty", layoutInflater)
                    Handler(Looper.getMainLooper()).post(){
                        editTextQty.setText("0")
                        editTextQty.requestFocus()
                    }
                }
                else{
                    if(qty >= 0){
                        noSKBUpdate = true
                    }else{
                        loadRecyclerCase = false
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this, "จำนวนต้องมากกว่าหรือเท่ากับ 0", layoutInflater)
                        Handler(Looper.getMainLooper()).post(){
                            editTextQty.setText("0")
                            editTextQty.requestFocus()
                        }
                    }
                }
            }
            packingInfoList.clear()
            packingInfoList = PackingQuery().getListPackingInfoByCaseNo(sCaseNo)
            if(packingInfoList.isNotEmpty()){
                val qty = PackingQuery().getTotalPackQtyByCaseNo(sCaseNo).toString()+" K/B "
                Handler(Looper.getMainLooper()).post {
                    textviewTotal.text = qty
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun alertDialog(){
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

        textView.text = "ต้องการบันทึกข้อมูล?"

        buttonYes.setOnClickListener {
            dialog.dismiss()
            asyncSave()
        }
        buttonNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun alertDialogNoSKBUpdate(){
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

        textView.text = "ตต้องการแก้ไขข้อมูล?"

        buttonYes.setOnClickListener {
            dialog.dismiss()
            asyncUpdateNoSKB()
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