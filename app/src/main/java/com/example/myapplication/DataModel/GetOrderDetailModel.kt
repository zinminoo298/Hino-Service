package com.example.myapplication.DataModel

class GetOrderDetailModel {
    var partNo:String? = null
    var orderNo:String? = null
    var packQty:Int? = null

    constructor(partNo: String?, orderNo: String?, packQty: Int?) {
        this.partNo = partNo
        this.orderNo = orderNo
        this.packQty = packQty
    }
}