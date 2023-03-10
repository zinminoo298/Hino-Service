package com.example.myapplication.DataQuery

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.widget.Toast
import com.example.myapplication.Gvariable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

class LoginQuery(private val context: Context, private val username: String, private val password: String) {
    fun login(): Boolean {
        val driverClass = "net.sourceforge.jtds.jdbc.Driver"
        var conn: Connection? = null
        var result: ResultSet? = null
        val policy =
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            Class.forName(driverClass)
            Gvariable.connURL = ("jdbc:jtds:sqlserver://${Gvariable.databaseServer}:1433/${Gvariable.databaseName};encrypt=false;user=${Gvariable.databaseUser};password=${Gvariable.databasePassword};")
            val connURL = Gvariable.connURL
            DriverManager.setLoginTimeout(3)
            conn = DriverManager.getConnection(connURL)


            val statement = conn.createStatement()
            val sql = "select * from [User]  inner join MenuAuth  on [User].UserName collate " +
                    " SQL_Latin1_General_CP1_CI_AS = MenuAuth.UserName where [User].UserName = '$username' " +
                    " and [User].Password = '$password' and [User].DeleteYN = '0' and [MenuAuth].MenuID like 'PDA%' "
            result = statement.executeQuery(sql)

            if(result.next()){

                if (result.getString("UserName") == null) {
                    Gvariable.userRealName = ""
                } else {
                    Gvariable.userRealName = result.getString("UserName")
                }

                if (result.getString("FullName") == null) {
                    Gvariable.userName = ""
                } else {
                    Gvariable.userName = result.getString("FullName")
                }
            }
            else{
                Gvariable.userName = ""
                Gvariable.userRealName = ""
            }

            return if (Gvariable.userName == "" || Gvariable.userName == null) {
                conn?.close()
                uiThreadToast("Username or Password is wrong!")
                false
            } else{
                conn?.close()
                true
            }

        } catch (e: Exception) {
            conn?.close()
            e.printStackTrace()
            uiThreadToast("Cannot connect to database")
            return false
        }
    }

    private fun uiThreadToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}