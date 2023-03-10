package com.example.myapplication.DataQuery

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
                ""
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
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
}