package com.example.appchatkl.data

class Message(
    var message: String = "",
    var id: String = "",
    var time: String = "",
    var avata: String ="",
    var image:String="",
    var isImage:Boolean=false,
    var isShowTime:Boolean=false,
    var isShowAvata:Boolean=false
) {
}