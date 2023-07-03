package com.example.myapplication.DataQuery

import com.example.myapplication.DataModel.GetOrderDetailModel
import com.example.myapplication.Gvariable
import java.sql.ResultSet
import java.util.stream.Stream

class DeliveryQuery {

    fun showOrder(orderDate:String, partNo:String, process:String) : ArrayList<String> {
        var showOrderList = ArrayList<String>()
        showOrderList.clear()
        var resultSet: ResultSet? = null
        var sql = ""

        if(partNo == ""){
            sql = "Select OrderNo As F_Order_No, SKBStatus From V_PDA_SummaryRPD_by_Order" +
                    " Where OrderDate like '$orderDate%' Group by OrderNo, SKBStatus Order by OrderNo"
        }else{
            sql = "Select OrderNo AS F_Order_No, SKBStatus From V_PDA_SummaryRPD_by_Order Where 1=1 And PartNo = '$partNo' "
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
                var skbStatus = if(resultSet.getString("SKBStatus") == null) "" else resultSet.getString("SKBStatus")
                showOrderList.add("$orderNo|$skbStatus")
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

    fun showOrderByPds(pds:String, partNo: String, orderNo:String) : ArrayList<String> {
        var showOrderByPdsList = ArrayList<String>()
        var resultSet: ResultSet? = null
        var sql = "SELECT F_PDS_No, F_Order_No" +
                " FROM PDA_Receive_RemainOrderNo ('$pds','$partNo') " +
                " Order by F_Order_No "
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            showOrderByPdsList.add("Select")
            while (resultSet.next()){
                var orderNo = if(resultSet.getString("F_Order_No") == null) "" else resultSet.getString("F_Order_No")
                showOrderByPdsList.add(orderNo)
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            showOrderByPdsList.clear()
            Gvariable.conn!!.close()
        }
        return showOrderByPdsList
    }

    fun checkOrderByPds(pds:String, partNo:String): Boolean {
        var resultSet: ResultSet? = null
        var sql = "SELECT F_PDS_No, F_Order_No " +
                " FROM PDA_Receive_RemainOrderNo ('$pds','$partNo') " +
                " Order by F_Order_No "
        var i = 0
        try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            while (resultSet.next()) {
                i++
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Gvariable.conn!!.close()
            i = 0
        }

        return i > 0
    }

    fun showOrderByOrder(partNo: String) : ArrayList<String> {
        var showOrderByOrderList = ArrayList<String>()
        var resultSet: ResultSet? = null
        var sql = "Select OrderNo As F_Order_No" +
                " FROM V_PDA_SummaryRPD_by_Order" +
                " Where PartNo  = '$partNo' and OrderQty > ReceiveQty " +
                " Order by OrderNo"
        try{
                         Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            showOrderByOrderList.add("Select")
            while (resultSet.next()){
                var orderNo = if(resultSet.getString("F_Order_No") == null) "" else resultSet.getString("F_Order_No")
                showOrderByOrderList.add(orderNo)
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            showOrderByOrderList.clear()
            Gvariable.conn!!.close()
        }
        return showOrderByOrderList
    }

    fun getCaseNoStatus(caseNo: String) : String {
        var resultSet: ResultSet? = null
        var status = ""
        var sql = "Select dbo.PDA_Delivery_getCaseNoStatus('$caseNo') AS CaseStatus "
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                status = resultSet.getString("CaseStatus")
            }else{
                status = ""
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            status = ""
            Gvariable.conn!!.close()
        }
        return status
    }

    fun getOrderDetail(caseNo: String) : ArrayList<GetOrderDetailModel> {
        var orderDetailList = ArrayList<GetOrderDetailModel>()
        orderDetailList.clear()
        var resultSet: ResultSet? = null
        var status = ""
        var sql = "Select OrderNo,Partno,PackQty from PDA_Delivery_getListCaseNobyCase('$caseNo') Order by OrderNo,Partno"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            while(resultSet.next()){
                val orderNo = resultSet.getString("OrderNo")
                val partNo = resultSet.getString("Partno")
                val packQty = resultSet.getInt("PackQty")
                orderDetailList.add(GetOrderDetailModel(partNo, orderNo, packQty))
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            orderDetailList.clear()
            Gvariable.conn!!.close()
        }
        return orderDetailList
    }

    fun closeCase(sDeliveryDate:String, sRound:Int, sCaseNo:String, deliveryBy:String) : Boolean{
        var sql = "EXEC spPDA_Delivery_closeCaseNo '$sDeliveryDate','$sRound', '$sCaseNo', '$deliveryBy','' "
        return try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            statement.executeUpdate(sql)
            true
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }

    fun transferCaseNo(sOCaseNo:String, sNCaseNo:String) : Boolean{
        var sql = "EXEC spPDA_Delivery_TransferCaseNo '$sOCaseNo','$sNCaseNo', '' "
        return try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            statement.executeUpdate(sql)
            true
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }
}