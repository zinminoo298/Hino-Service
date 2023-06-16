package com.example.myapplication.DataQuery

import com.example.myapplication.DataModel.GetPackingInfoListModel
import com.example.myapplication.DataModel.GetPackingListModel
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
            else{
                qty = 0
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

    fun getCountCaseByUser(user:String) : Int{
        var resultSet: ResultSet? = null
        var qty = 0
        var sql = "SELECT cntCaseNO  FROM  PDA_Packing_getCntCasebyUser ('$user')"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                qty = resultSet.getInt("cntCaseNO")
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

    fun getCaseNoByUser(user:String) : ArrayList<GetPackingListModel>{
        val caseList = ArrayList<GetPackingListModel>()
        var resultSet: ResultSet? = null

        val sql = "SELECT * FROM PDA_Packing_getListCaseNobyUser ('$user')"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            caseList.clear()
            while(resultSet.next()){
                val no = resultSet.getString("NO")
                val caseNo = resultSet.getString("CASENO")
                caseList.add(GetPackingListModel(no, caseNo))
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            caseList.clear()
        }

        return caseList
    }

    fun deleteCaseNo(caseNo:String, user:String) : Boolean{
        val sql = "EXEC  spPDA_Packing_DeleteCaseNo '$caseNo', '$user',''"
        return try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            statement.executeUpdate(sql)
            true
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            false
        }
    }

    fun getCountCaseNo(year:String, caseNo:String) : Int{
        var resultSet: ResultSet? = null
        var qty = 0
        var sql = "Select cntCaseNo From PDA_Packing_getCountCaseNo ('$year','$caseNo')"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                qty = resultSet.getInt("cntCaseNO")
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

    fun checkCaseWithMaster(caseNo:String) : Int {
        var resultSet: ResultSet? = null
        var qty = 0
        var sql = "Select CntCaseNO From PDA_Packing_checkCasewithMaster ('$caseNo')"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                qty = resultSet.getInt("cntCaseNO")
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

    fun getCaseMatchStatus(year:String, caseNo:String) : Int{
        var resultSet: ResultSet? = null
        var qty = 0
        var sql = "Select  dbo.PDA_Packing_getCaseMatchStatus('$caseNo') As CaseStatus"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                qty = resultSet.getInt("CaseStatus")
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

    fun updateReuseCaseFlag(caseNo:String, user:String) {
        var sql = "EXEC spPDA_Packing_UpdateReFlagCaseNo '$caseNo', '$user',''"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            statement.executeQuery(sql)
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
        }
    }

    fun insertCaseNoMatch(year:String, caseNo:String, user: String, saveFlag:Int){
        var sql = "EXEC spPDA_Packing_InsertCaseNo '$year', '$caseNo','${user.trim()}', '$saveFlag', ''"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            statement.executeQuery(sql)
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
        }
    }

    fun getListCaseNoByUser(user:String) : ArrayList<String>{
        val caseList = ArrayList<String>()
        var resultSet: ResultSet? = null

        val sql = "SELECT CaseNO  FROM  PDA_Packing_getListCasebyUser ('$user') Order by  CaseNO"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            caseList.clear()
            while(resultSet.next()){
                val caseNo = resultSet.getString("CaseNO")
                caseList.add(caseNo)
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            caseList.clear()
        }

        return caseList
    }

    fun getCaseNoList(user:String) : Int{
        var resultSet: ResultSet? = null
        var qty = 0
        var sql = "SELECT Count(CaseNO) As cntCaseNo  FROM  PDA_Packing_getListCasebyUser ('$user')"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            if(resultSet.next()){
                qty = resultSet.getInt("cntCaseNo")
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

    fun getQtyInOrder(orderNo:String, partNo: String) : Int{
        var resultSet: ResultSet? = null
        var qty = 0
        var sql = "Select  dbo.PDA_Delivery_getReceiveQtybyOrderNoPartNo('$orderNo','$partNo') AS QtyOrder"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            qty = if(resultSet.next()){
                if(resultSet.getInt("QtyOrder") != null){
                    resultSet.getInt("QtyOrder")
                }else{
                    0
                }
            } else{
                0
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

    fun updateOrderProcessSKB(orderNo: String, partNo: String) :Boolean {
        val sql = "EXEC  spPDA_Packing_UpdateOrderProcessSKB '$orderNo','$partNo' ,''"
        return try{
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
    }

    fun deletePackingInformationSKB(caseNo:String, orderNo: String, partNo: String) : Boolean{
        val sql = "EXEC  spPDA_Packing_UpdatePackingInfoSKB '$caseNo','$orderNo','$partNo' ,''"
        return try{
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
    }

    fun getTotalPackQtyNotInCaseNo(orderNo:String, partNo: String, caseNo: String) : Int {
        var resultSet: ResultSet? = null
        var qty = 0
        var sql = "Select  dbo.PDA_Packing_getTotalPackqtynotinCaseNo('$orderNo','$partNo','$caseNo') As intPackQty "
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            qty = if(resultSet.next()){
                if(resultSet.getInt("intPackQty") != null){
                    resultSet.getInt("intPackQty")
                }else{
                    0
                }
            } else{
                0
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

    fun isSKB(partNo:String) : Boolean{
        var resultSet: ResultSet? = null
        var result = false
        var sql = "SELECT [Part].SKB FROM [Part] WHERE [Part].PartNo = '$partNo'"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            result = if(resultSet.next()){
                resultSet.getString("SKB") == "S"
            } else{
                false
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            result  = false
        }
        return result
    }

    fun isEDP(partNo: String) : Boolean {
        var resultSet: ResultSet? = null
        var result = false
        var sql = "SELECT [Part].EDP FROM [Part] with (nolock) WHERE [Part].PartNo = '$partNo'"
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            result = if(resultSet.next()){
                resultSet.getString("EDP") == "E"
            } else{
                false
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            result  = false
        }
        return result
    }

    fun checkPartAvailable(partNo: String, orderNo: String) : Boolean{
        var resultSet: ResultSet? = null
        var result = false
        var sql = "Select OrderNo From OrderProcess " +
                " Where Isnull(SerialNo,'') <> '' and Isnull(ReceiveQty,0) > Isnull(PackQTy,0) "
        if(partNo != ""){
            sql += " and SerialNO = '$partNo' "
        }
        if(orderNo != ""){
            sql += " and OrderNo = '$orderNo' "
        }
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            result = resultSet.next()
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            result  = false
        }
        return result
    }

    fun updatePackingInformationNoSKB(caseNo:String, orderNo:String, partNo:String, qty:Int, user:String) : Boolean{
        val sql = "EXEC  spPDA_Packing_UpdatePackingInfoNoSKB '$caseNo','$orderNo','$partNo' ,'$qty','$user',''"
        return try{
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
    }

    fun updateOrderProcessNoSKB(orderNo:String, partNo: String, qty:Int, user:String, caseNo: String) : Boolean {
        val sql = "EXEC  spPDA_Packing_UpdateOrderProcessNoSKB '$orderNo','$partNo','$qty' ,'$user','$caseNo',''"
        return try{
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
    }

    fun saveCaseNo(pId:String, caseNo:String, packQty:String, packBy:String) : Boolean{
        val sql = "EXEC  spPDA_Packing_savePackingInfo '$pId','$caseNo','${Integer.parseInt(packQty)}' ,'$packBy',''"
        return try{
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
    }

    fun getMaxYearByCaseNo(caseNo :String) : Int{
        var resultSet: ResultSet? = null
        var date = 0
        var sql = "SELECT dbo.PDA_Packing_getMaxYearbyCaseNO ('$caseNo')  as CaseYear "
        try{
            Gvariable().startConn()
            val statement = Gvariable.conn!!.createStatement()
            resultSet = statement.executeQuery(sql)
            date = if(resultSet.next()){
                resultSet.getInt("CaseYear")
            } else{
                0
            }
            statement.close()
            Gvariable.conn!!.close()
        }catch (e:Exception){
            e.printStackTrace()
            Gvariable.conn!!.close()
            date  = 0
        }
        return date
    }

    fun updateCaseStatus(caseNo:String, caseYear:Int, status:String){
        var sql = "update [CaseNo] set " +
                " Printed= '$status' " +
                " where CaseNo= '$caseNo' and CaseYear= '$caseYear' "
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

    fun updateCaseFlag(caseNo:String, user:String) : Boolean{
        val sql = "EXEC  spPDA_Packing_UpdateFlagCaseNo '$caseNo', '$user','' "
        return try{
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
    }
}