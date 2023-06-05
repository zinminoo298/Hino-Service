package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataQuery.DeliveryQuery
import com.example.myapplication.DataQuery.PackingQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Packing : AppCompatActivity() {
    companion object{
        lateinit var cardViewBack: CardView
        var countCaseNo = 0
        var date = ""
        var orderNoList = ArrayList<String>()
        var caseNoList = ArrayList<String>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_packing)

        cardViewBack = findViewById(R.id.cardView_back)
    }

    private fun asyncDelete(){

        val deferred = lifecycleScope.async(Dispatchers.IO) {

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

    fun onLoad(){
        if(countCaseNo > 1){
            // set spinner case no position to -1
        }
        else{
            // set spinner case no position to 0
            // scan focus
        }
    }

    fun listOrderNo(){
        orderNoList.clear()
        val showOrderList = DeliveryQuery().showOrder( date, "", "")
        for(i in 0 until showOrderList.size){
            orderNoList.add(showOrderList[i].substringBefore("|"))
        }
    }

    fun listCaseNo(){
        caseNoList.clear()
        caseNoList = PackingQuery().getListCaseNoByUser(Gvariable.userName.toString())
    }


}