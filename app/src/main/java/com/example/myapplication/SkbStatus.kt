package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.SkbStatusListAdapter
import com.example.myapplication.DataModel.GetDetailSKBModel
import com.example.myapplication.DataQuery.GetTimeQuery
import com.example.myapplication.DataQuery.OrderDetailQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SkbStatus : AppCompatActivity() {
    companion object{
        lateinit var editTextSKB: EditText
        lateinit var textViewOrderNo: TextView
        lateinit var detailSkbList:ArrayList<GetDetailSKBModel>
        lateinit var recyclerView: RecyclerView
        lateinit var viewAdapter: RecyclerView.Adapter<*>
        lateinit var viewManager: RecyclerView.LayoutManager
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skb_status)

        editTextSKB = findViewById(R.id.editText_skb)
        textViewOrderNo = findViewById(R.id.textview_orderNo)
        recyclerView = findViewById(R.id.recyclerview_skb_list)
        detailSkbList = ArrayList<GetDetailSKBModel>()


        editTextSKB.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (editTextSKB.text.toString().trim().isNotEmpty()) {
                    if (editTextSKB.text.toString().length == 15) {
                        asyncSearchSKB()
                    } else {
                        //Not SKB document
                        Gvariable().messageAlertDialog(
                            this,
                            "ไม่ใช่เอกสาร SKB",
                            layoutInflater
                        )
                        Gvariable().alarm(this)
                        editTextSKB.selectAll()
                        editTextSKB.requestFocus()
                    }
                } else {
                    Gvariable().messageAlertDialog(
                        this,
                        "กรุณาแสกน SKB",
                        layoutInflater
                    )
                    Gvariable().alarm(this)
                    editTextSKB.selectAll()
                    editTextSKB.requestFocus()
                }
            }
            false
        })
    }

    private fun asyncSearchSKB(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            searchSKB()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@SkbStatus)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    //reload Recycler vie
                    loadRecyclerView()
                    editTextSKB.selectAll()
                    editTextSKB.requestFocus()
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun searchSKB(){
        val eSKB = editTextSKB.text.toString().trim()
        val orderNo = OrderDetailQuery().getOrderFromSKB(eSKB)
        Handler(Looper.getMainLooper()).post {
            textViewOrderNo.text = orderNo
        }
        detailSkbList.clear()
        //set textviewSKB
        if(orderNo.trim() == ""){
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "ไม่พบข้อมูล Serial K/B", layoutInflater)
        }
        else{
            detailSkbList = OrderDetailQuery().getDetailSKB(eSKB)
        }
    }

    private fun loadRecyclerView(){
        viewManager = LinearLayoutManager(this)
        viewAdapter = SkbStatusListAdapter(detailSkbList!!, this)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}