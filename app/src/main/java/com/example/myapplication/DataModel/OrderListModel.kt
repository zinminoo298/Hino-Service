package com.example.myapplication.DataModel

class OrderListModel {
    var no:String? = null
    var partNo:String? = null
    var qty:String? = null
    var r:String? = null
    var p:String? = null
    var d:String? = null

    constructor(no: String?, partNo: String?, qty: String?, r: String?, p: String?, d: String?) {
        this.no = no
        this.partNo = partNo
        this.qty = qty
        this.r = r
        this.p = p
        this.d = d
    }
}