package com.example.myapplication.DataQuery

import com.example.myapplication.DataModel.GetDetailSKBModel
import com.example.myapplication.DataModel.GetOrderDetailModel
import com.example.myapplication.DataModel.OrderListModel
import com.example.myapplication.Gvariable
import java.sql.Date
import java.sql.ResultSet
import java.util.Calendar

class OrderDetailQuery {
    fun getOrderIdBySerial(station:String, serialNo:String): String{
        var mReturn = ""
        var sql = ""
        var result: ResultSet? = null

        when (station) {
            "RECEIVE" -> {
                sql = "Select OrderDetailId,OrderNO,OrderDate From dbo.PDA_Order_ListData_RECEIVE('$serialNo') Order by OrderNo ASC"
            }
            "PACKING" -> {
                sql = "Select OrderDetailId,OrderNO,OrderDate From dbo.PDA_Order_ListData_PACKING('$serialNo') Order by OrderNo ASC"
            }
            "DELIVERY" -> {
                sql = "Select OrderDetailId,OrderNO,OrderDate From dbo.PDA_Order_ListData_DELIVERY('$serialNo') Order by OrderNo ASC"
            }
        }

        try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            mReturn = if(result.next()) {
                "${result.getString("OrderDetailId")}|${result.getString("OrderNO")}|${result.getString("OrderDate")}"
            } else{
                "|||"
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            mReturn = "|||"
        }

        return mReturn
    }

    fun getOrderQty(orderDetailId:String): Int{
        var mReturn = 0
        var sql = ""
        var result: ResultSet? = null

        sql = "Select OrderQty From  V_PDA_SummaryRPD_by_Order Where OrderDetailId = '$orderDetailId'"

        try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            mReturn = if(result.next()) {
                result.getInt("OrderQty")
            } else{
                0
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
        }
        return mReturn
    }

    fun writeLog(job:String, table:String, detail:String, key:String, userName:String){
        try{
            Gvariable().startConn()
            val sql = "Insert into LogFile (F_Issued_Date,F_Issued_Time,F_Job,F_Table,F_Detail,F_Program_ID," +
                    " F_Key_ID,F_Hostname,F_Filler,F_User_ID, F_User_Name, F_System, F_Module) " +
                    " Values (CONVERT(char(10), Format(getdate(),'MM/dd/yyyy'), 120), CONVERT(char(20), Format(getdate(),'HH:mm'), 120), '$job','$table'," +
                    " '$detail','$job','$key', HOST_NAME(),'','$userName','','Service','$job')  "
            val statement = Gvariable.conn!!.createStatement()
            statement.executeUpdate(sql)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun getOrderFromSKB(skb:String): String{
        var result: ResultSet? = null
        var orderNo = ""
        var sql = "SELECT Top 1  Orderno From OrderProcess Where SerialNo = '$skb' Order by OrderNo Desc"

        try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            orderNo = if(result.next()) {
                result.getString("Orderno")
            } else{
                ""
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            orderNo = ""
        }
        return orderNo
    }

    fun getDetailSKB(skb:String) : ArrayList<GetDetailSKBModel>{
        var detailSkbList = ArrayList<GetDetailSKBModel>()
        var result: ResultSet? = null
        var sql = "SELECT ColumnNM As Detail,Detail As [Date]  FROM  PDA_Detail_getSKBDetail('$skb')  Order by Row_ID"
        try{
            detailSkbList.clear()
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            while(result.next()){
                //add to data mode
                val detail = result.getString("Detail")
                val date = result.getString("Date")
                detailSkbList.add(GetDetailSKBModel(detail, date))
            }
        }catch (e:Exception){
            e.printStackTrace()
            detailSkbList.clear()
            Gvariable.conn!!.close()
        }
        return detailSkbList
    }

    fun getOrderDetailByPartNo(orderNo:String, partNo:String): String{
        var result: ResultSet? = null
        var returnString = ""
        var sql = "SELECT OrderDetailId, OrderNo, OrderDate, OrderQty  " +
                "  FROM V_PDA_SummaryRPD_by_Order " +
                "  WHERE (OrderNo = '$orderNo') AND (PartNo = '$partNo')"

        try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            if(result.next()) {
                returnString = "${result.getString("OrderDetailId")}|" +
                        "${result.getString("OrderNo")}|" +
                        "${result.getString("OrderDate")}|" +
                        "${result.getString("OrderQty")}"
            } else{
                returnString = ""
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            returnString = ""
        }
        return returnString
    }

    fun loadDataOrderDetail(orderNo:String) :ArrayList<OrderListModel>{
        var orderList = ArrayList<OrderListModel>()
        var result: ResultSet? = null
        var sql = "Select OrderDetailId,OrderNo,OrderDate, KPBNo,  PartNo, PartName, OrderQty, ReceiveQty, PackQty, DeliveryQty " +
                " FROM  V_PDA_SummaryRPD_by_Order " +
                " Where 1=1 " +
                " and OrderNo = '$orderNo' " +
                " ORDER BY OrderNo,PartNo "
        try{
            orderList.clear()
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            var i = 0
            while(result.next()){
                //add to data mode
                i++
                var no = i.toString()
                val partNo = result.getString("PartNo")

                val qty = if(result.getString("OrderQty") != null){
                    result.getString("OrderQty")
                }else{
                    ""
                }

                val receive = if(result.getString("ReceiveQty") != null){
                    result.getString("ReceiveQty")
                }else{
                    ""
                }
                val packing = if(result.getString("PackQty") != null){
                    result.getString("PackQty")
                }else{
                    ""
                }
                val delivery = if(result.getString("DeliveryQty") != null){
                    result.getString("DeliveryQty")
                }else{
                    ""
                }
                orderList.add(OrderListModel(no, partNo, qty, receive, packing, delivery))
            }
        }catch (e:Exception){
            e.printStackTrace()
            orderList.clear()
            Gvariable.conn!!.close()
        }
        return orderList
    }

    fun getOrderDetailBySerial(station: String, serialNo: String) : String{
        var orderDetail = ""
        var result:ResultSet? = null
        var sql = ""

        when(station){
            "RECEIVE" -> {
                sql = "Select OrderDetailId,OrderNO,OrderDate From dbo.PDA_Order_ListData_RECEIVE('$serialNo') Order by OrderNo ASC"
            }

            "PACKING" -> {
                sql = "Select OrderDetailId,OrderNO,OrderDate From dbo.PDA_Order_ListData_PACKING('$serialNo') Order by OrderNo ASC"
            }

            "DELIVERY" -> {
                sql = "Select OrderDetailId,OrderNO,OrderDate From dbo.PDA_Order_ListData_DELIVERY('$serialNo') Order by OrderNo ASC"
            }
        }
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            orderDetail = if(result.next()){
                if(result.getString("OrderDetailId") == null){
                    ""
                }else{
                    result.getString("OrderDetailId") + "|" + result.getString("OrderNo") + "|" + result.getString("OrderDate")
                }
            } else{
                ""
            }
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            orderDetail = ""
        }
        return orderDetail
    }
}