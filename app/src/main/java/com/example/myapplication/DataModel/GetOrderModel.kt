package com.example.myapplication.DataModel

class GetOrderModel {
    var receiveQty:Int? = null
    var receiveDate:String? = null
    var pId:String? = null
    var partNo:String? = null
    var orderDetailId:String? = null
    var edpQualityCheckDate:String? = null
    var packingDate:String? = null

    constructor(
        receiveQty: Int?,
        receiveDate: String?,
        pId: String?,
        partNo: String?,
        orderDetailId: String?,
        edpQualityCheckDate: String?,
        packingDate:String?
    ) {
        this.receiveQty = receiveQty
        this.receiveDate = receiveDate
        this.pId = pId
        this.partNo = partNo
        this.orderDetailId = orderDetailId
        this.edpQualityCheckDate = edpQualityCheckDate
        this.packingDate = packingDate
    }
}