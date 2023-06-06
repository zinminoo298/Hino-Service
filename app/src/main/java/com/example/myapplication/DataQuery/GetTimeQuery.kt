package com.example.myapplication.DataQuery

import com.example.myapplication.Gvariable
import java.sql.ResultSet

class GetTimeQuery {
    fun timeServer() : String{
        var currentDate = ""
        var resultSet: ResultSet? = null
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            val sql = "SELECT CONVERT(char(20), Format(getdate(),'dd-MM-yyyy'), 120) AS date, CONVERT(char(20), Format(getdate(),'yyyyMMdd'), 120) AS date1"
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                val date = resultSet.getString("date").trim()
                val date1 = resultSet.getString("date1").trim()
                currentDate = "$date|$date1"
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            currentDate = ""
        }
        return currentDate
    }

}