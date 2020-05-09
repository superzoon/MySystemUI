package android.os

import android.hardware.fingerprint.FingerprintManager
import cn.nubia.systemui.common.BiometricCmd
import cn.nubia.systemui.common.writeLine
import java.io.File


fun FingerprintManager.writeNode(fileName:String, value:Int){
    File(fileName).writeLine("${value}")
}

fun FingerprintManager.processCmd(cmd:Int, arr1:Int, arr2:Int, sendBuff:ByteArray, len:Int): ByteArray?{
    if(cmd in BiometricCmd){
        return null
    }else{
        throw IllegalAccessError("processCmd cmd=${cmd}")
    }
}
