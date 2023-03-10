package com.example.myapplication.DataQuery

import com.example.myapplication.Gvariable
import java.sql.ResultSet

class DeliveryQuery {
    var showOrderList = ArrayList<String>()

    fun showOrder(orderDate:String, partNo:String, process:String) : ArrayList<String> {
        showOrderList.clear()
        var resultSet: ResultSet? = null
        var sql = ""

        if(partNo == ""){
            sql = "Select OrderNo As F_Order_No, SKBStatus From V_PDA_SummaryRPD_by_Order" +
                    " Where OrderDate like '$orderDate%' Group by OrderNo, SKBStatus Order by OrderNo"
        }else{
            sql = "Select OrderNo AS F_Order_No, SKBStatus From V_PDA_SummaryRPD_by_Order Where 1=1 And and PartNo = '$partNo' "
            if(orderDate != ""){
                sql += " And OrderDate = '$orderDate'"
            }
            when (process) {
                "RECEIVE" -> {
                    sql += " and OrderQty > ReceiveQty "
                }
                "PACKING" -> {
                    sql += " and ReceiveQty <> 0 and OrderQty > PackQty "
                }
                "DELIVERY" -> {
                    sql += " and ReceiveQty <> 0 and OrderQty > DeliveryQty "
                }
            }
            sql += " Group by OrderNo, SKBStatus"
        }
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            while (resultSet.next()){
                var orderNo = if(resultSet.getString("F_Order_No") == null) "" else resultSet.getString("F_Order_No")
                showOrderList.add(orderNo)
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            showOrderList.clear()
            Gvariable.conn!!.close()
        }
        return showOrderList
    }
}