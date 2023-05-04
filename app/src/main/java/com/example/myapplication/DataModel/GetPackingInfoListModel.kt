package com.example.myapplication.DataModel

class GetPackingInfoListModel {
    var no:String? = null
    var partNo:String? = null
    var packingQty:Int? = null

    constructor(no: String?, partNo: String?, packingQty: Int?) {
        this.no = no
        this.partNo = partNo
        this.packingQty = packingQty
    }
}