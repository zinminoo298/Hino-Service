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
            val sql = "SELECT CONVERT(char(20), Format(getdate(),'dd-MM-yyyy'), 120) AS date"
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                currentDate = resultSet.getString("date").trim()
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