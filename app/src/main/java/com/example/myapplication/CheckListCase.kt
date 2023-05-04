package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.*
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
        var orderNoList = ArrayList<String>()
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
        packingInfoList = ArrayList<GetPackingInfoListModel>()

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
            editTextQty.setText("${ (Integer.parseInt(editTextQty.text.toString()) + 1) }")
        }

        buttonDown.setOnClickListener {
            editTextQty.setText("${ (Integer.parseInt(editTextQty.text.toString()) - 1) }")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun asyncScanCaseNo(caseNo: String){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val sCaseNoStatus = PackingQuery().getCaseNoStatus(caseNo)
            if(sCaseNoStatus != 1){
                //clear spinners
                //set qty = 0
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
        var qty = 0
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val sCaseNo = editTextCaseNo.text.toString().trim()
            loadListOrderNo(sCaseNo, sPartNo)
            qty = if(sPartNo.length == 15){
                1
            }else{
                PackingQuery().getQtyByCaseNo(sCaseNo, spinnerOrderNo.selectedItem.toString(),sPartNo)
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

}