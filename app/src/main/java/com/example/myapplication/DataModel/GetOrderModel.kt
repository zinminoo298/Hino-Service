package com.example.myapplication.DataModel

class GetOrderModel {
    var receiveQty:Int? = null
    var receiveDate:String? = null
    var pId:String? = null
    var partNo:String? = null
    var orderDetailId:String? = null

    constructor(
        receiveQty: Int?,
        receiveDate: String?,
        pId: String?,
        partNo: String?,
        orderDetailId: String?
    ) {
        this.receiveQty = receiveQty
        this.receiveDate = receiveDate
        this.pId = pId
        this.partNo = partNo
        this.orderDetailId = orderDetailId
    }
}