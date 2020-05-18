
import cn.nubia.systemui.annotation.LongDef
import cn.nubia.systemui.common.BiometricShowFlagsConstant
import cn.nubia.systemui.fingerprint.process.ActionList
import kotlin.reflect.KFunction1

fun test(@LongDef aa:Int){
    when (aa){
        in ActionList.ActionKey ->{
            print(aa)
        }
        else -> {
            throw IllegalAccessError("fail key")
        }
    }
}

fun main(args: Array<String>) {
    println("NORMAL = {${BiometricShowFlagsConstant.flagsToString(BiometricShowFlagsConstant.TYPE_NORMAL_MASK)}}")
    println("AOD = {${BiometricShowFlagsConstant.flagsToString(BiometricShowFlagsConstant.TYPE_AOD_MASK)}}")
    println("KEYGUARD = {${BiometricShowFlagsConstant.flagsToString(BiometricShowFlagsConstant.TYPE_KEYGUARD_MASK)}}")

    onStop(1234)

}

private fun handleStop(ss: Int){
    print("hello ${ss}")
}

fun handlerInvoke(service: KFunction1<Int, Unit>, s:Int) {
    service.invoke(s)
}

fun onStop(service: Int) {
    handlerInvoke(::handleStop, service)
}
