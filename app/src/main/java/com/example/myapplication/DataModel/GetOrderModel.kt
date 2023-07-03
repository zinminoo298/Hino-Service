package com.example.myapplication.DataModel

class GetOrderModel {
    var receiveQty = 0
    var receiveDate = ""
    var pId:String? = ""
    var partNo:String = ""
    var orderDetailId = ""
    var edpQualityCheckDate = ""
    var packingDate = ""

    constructor(
        receiveQty: Int,
        receiveDate: String,
        pId: String,
        partNo: String,
        orderDetailId: String,
        edpQualityCheckDate: String,
        packingDate:String
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