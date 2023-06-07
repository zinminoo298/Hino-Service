package com.example.myapplication.DataQuery

import com.example.myapplication.Gvariable
import java.sql.ResultSet

class SerialQuery {
    fun updateSerialStatus(serial:String, serialStatus:String){
            val sql = "update [Serial] set" +
                    "  SerialStatus = $serialStatus, LastEditBy = ${Gvariable.userName}, LastEditDate = CAST(CAST(GETDATE() AS date) AS datetime) " +
                    "  where SerialNo = $serial "
            try{
                Gvariable().startConn()
                val statement = Gvariable.conn!!.createStatement()
                statement.executeUpdate(sql)
                statement.close()
                Gvariable.conn!!.close()
            }catch (e:Exception){
                e.printStackTrace()
                Gvariable.conn!!.close()
            }
    }
}