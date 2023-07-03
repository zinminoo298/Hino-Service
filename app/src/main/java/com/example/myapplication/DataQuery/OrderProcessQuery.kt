package com.example.myapplication.DataQuery

import com.example.myapplication.DataModel.GetOrderModel
import com.example.myapplication.Gvariable
import java.sql.ResultSet

class OrderProcessQuery {
    var getOrderList = ArrayList<GetOrderModel>()
    fun getOrder(userSerialNo:String, uOrder:String, uDate:String):ArrayList<GetOrderModel>{
        var sql = ""
        var result: ResultSet? = null
        getOrderList.clear()

        sql = "SELECT OrderProcess.PId, OrderProcess.OrderDetailId, OrderProcess.OrderNo, OrderProcess.SerialNo, OrderProcess.PackingDate, " +
                "OrderProcess.ReceiveQty, OrderProcess.PackQty, OrderProcess.ReceiveDate, " +
                " OrderProcess.DeliveryDate, OrderProcess.EDPQualityCheckDate, OrderDetail.PartNo, OrderDetail.Qty " +
                " FROM OrderProcess with (nolock) INNER JOIN OrderDetail  with (nolock)  ON OrderProcess.OrderDetailId = OrderDetail.OrderDetailId " +
                "INNER JOIN [Order]  with (nolock) on [Order].OrderNo = OrderDetail.OrderNo and [Order].OrderDate = OrderDetail.OrderDate " +
                " where OrderProcess.SerialNo ='$userSerialNo' and [Order].OrderNo ='$uOrder' "
        if(uDate != ""){
            sql += " and [Order].OrderDate = '$uDate'"
        }

        try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            while (result.next()) {
                val qty = if(result.getInt("ReceiveQty") != null){
                    result.getInt("ReceiveQty")
                }else{
                    0
                }

                val receiveDate = if(result.getString("ReceiveDate") != null){
                    result.getString("ReceiveDate")
                }else{
                    ""
                }

                val pId = if(result.getString("PId") != null){
                    result.getString("PId")
                }else{
                    ""
                }

                val partNo = if(result.getString("PartNo") != null){
                    result.getString("PartNo")
                }else{
                    ""
                }

                val orderDetailId = if(result.getString("OrderDetailId") != null){
                    result.getString("OrderDetailId")
                }else{
                    ""
                }

                val edpQualityCheckDate = if(result.getString("EDPQualityCheckDate") != null){
                    result.getString("EDPQualityCheckDate")
                }else{
                    ""
                }

                val packingDate = if(result.getString("PackingDate") != null){
                    result.getString("PackingDate")
                }else{
                    ""
                }

                getOrderList.add(GetOrderModel(
                    qty, receiveDate, pId, partNo, orderDetailId, edpQualityCheckDate, packingDate
                ))
            }
            statement.close()
            Gvariable.conn!!.close()
            return getOrderList
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            getOrderList.clear()
            return getOrderList
        }
    }

    fun updateOrderProcessReceive(orderProcessId:String, qty:Int) : Boolean{
        var mReturn:Boolean? = null
        var sql = ""
        var result: ResultSet? = null

        sql = "update [OrderProcess] set " +
                " ReceiveQty= '$qty', ReceiveDate = getdate(), ReceiveBy = '${Gvariable.userName}' ,LastEditBy = ' ${Gvariable.userName}', LastEditDate = getdate()" +
                " where PId = '$orderProcessId' "

        mReturn = try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            statement.executeUpdate(sql)
            statement.close()
            Gvariable.conn!!.close()
            true
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            false
        }
        return mReturn!!
    }

    fun checkUpdateStatus(pId:String) {
        var result: ResultSet? = null
        try {
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery("Select * from OrderProcess where PId = '$pId'")
            if (result.next()) {
                if (!result.getString("PackingDate")
                        .isNullOrEmpty() && !result.getString("ReceiveDate")
                        .isNullOrEmpty() && !result.getString("DeliveryDate").isNullOrEmpty()
                ) {
                    SerialQuery().updateSerialStatus(result.getString("SerialNo"), "F")
                }
                statement.close()
                Gvariable.conn!!.close()
            }
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
        }
    }

    fun getSumQty(detailId:String, fieldName:String) : Int{
        var qty = 0
        var sql = "select sum($fieldName) as Qty from orderprocess where orderdetailid='$detailId'"
        var result: ResultSet? = null
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            if(result.next()){
                qty = result.getInt("Qty")
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            qty = 0
            e.printStackTrace()
            Gvariable.conn!!.close()
        }
        return qty
    }

    fun getSumQtyByPid(pId:String, fieldName:String) : Int{
        var qty = 0
        var sql = "Select Isnull(sum($fieldName),'') as Qty from orderprocess where PId='$pId'"
        var result: ResultSet? = null
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            if(result.next()){
                qty = result.getInt("Qty")
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            qty = 0
            e.printStackTrace()
            Gvariable.conn!!.close()
        }
        return qty
    }

    fun save(id:String, orderDetailId:String, orderNo:String, serialNo:String) : Boolean{
        var mReturn:Boolean? = null
        var sql = ""
        var result: ResultSet? = null

        sql = "select PId from OrderProcess where PId = '$id' " +
                " if @@Rowcount = 0  " +
                " INSERT INTO OrderProcess(PId, OrderDetailId, OrderNo, SerialNo) " +
                " VALUES ('$id', '$orderDetailId', '$orderNo', '$serialNo') " +
                " else " +
                " update OrderProcess set " +
                " PId='$id', OrderDetailId='$orderDetailId', OrderNo='$orderNo', SerialNo='$serialNo' " +
                " where PId = '$id' "

        mReturn = try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            statement.executeQuery(sql)
            statement.close()
            Gvariable.conn!!.close()
            true
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            false
        }

        return mReturn!!
    }

    fun updateOrderProcessPacking(caseNo: String, orderProcessId:String, qty:String, user:String){
        var sql = "update [OrderProcess] set" +
                " CaseNo = '$caseNo', PackQty='${Integer.parseInt(qty)}', PackingDate =  getdate(), PackingBy = '$user' ,LastEditBy = '$user', LastEditDate = getdate()" +
                " Where PId = '$orderProcessId'"
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

    fun getQtyOrder(orderNo:String, partNo:String) : Int {
        var qty = 0
        var sql = "SELECT CAST(OrderQty AS INTEGER) AS F_Qty" +
                " FROM V_PDA_SummaryRPD_by_Order" +
                " WHERE OrderNo = '$orderNo'" +
                " AND PartNO = '$partNo'"
        var result: ResultSet? = null
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            result = statement.executeQuery(sql)
            if(result.next()){
                qty = result.getInt("F_Qty")
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            qty = 0
            e.printStackTrace()
            Gvariable.conn!!.close()
        }
        return qty
    }
}