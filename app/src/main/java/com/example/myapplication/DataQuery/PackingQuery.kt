package com.example.myapplication.DataQuery

import com.example.myapplication.DataModel.GetPackingInfoListModel
import com.example.myapplication.Gvariable
import java.sql.ResultSet

class PackingQuery {
    fun getCaseNoStatus(caseNo: String) : Int {
        var resultSet: ResultSet? = null
        var status = 0
        var sql = "Select  dbo.PDA_Packing_getCaseNoStatus('$caseNo') AS CaseNoStatus "
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                status = resultSet.getInt("CaseNoStatus")
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            status = 0
        }
        return status
    }

    fun getListPackingInfoByCaseNo(caseNo: String) : ArrayList<GetPackingInfoListModel>{
        var packingInfoList = ArrayList<GetPackingInfoListModel>()
        var resultSet: ResultSet? = null
        var sql = "Select [No],PartNo,PackingQty As Qty From [PDA_Packing_getListPackingInforByCaseNo]('$caseNo') Order by [No]"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            while(resultSet.next()){
                val no = resultSet.getString("No")
                val partNo = resultSet.getString("PartNo")
                val qty = resultSet.getInt("Qty")
                packingInfoList.add(GetPackingInfoListModel(no, partNo, qty))
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            packingInfoList.clear()
            Gvariable.conn!!.close()
        }
        return packingInfoList
    }

    fun getTotalPackQtyByCaseNo(caseNo: String) : Int {
        var resultSet: ResultSet? = null
        var qty = 0
        val sql = "SELECT dbo.[PDA_Packing_getTotalPackQtybyCaseNO] ('$caseNo')  as SumPackQty "
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                qty = resultSet.getInt("SumPackQty")
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            qty = 0
        }
        return qty
    }

    fun getOrderNoByCaseNo(caseNo: String, partNo: String) : ArrayList<String> {
        var resultSet: ResultSet? = null
        var list = ArrayList<String>()
        var sql = "Select OrderNo From [PDA_Packing_getOrderNoByCaseNo]('$caseNo','$partNo') Order by OrderNo "
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            while(resultSet.next()){
                list.add(resultSet.getString("OrderNo"))
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            list.clear()
        }
        return list
    }

    fun getQtyByCaseNo(caseNo: String, orderNo: String, partNo: String) : Int{
        var resultSet: ResultSet? = null
        var qty = 0
        var sql = "Select  dbo.PDA_Delivery_getQtybyCaseNo('$caseNo','$orderNo','$partNo') AS QtyDelivery "
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                qty = resultSet.getInt("QtyDelivery")
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            qty  = 0
        }
        return qty
    }
}