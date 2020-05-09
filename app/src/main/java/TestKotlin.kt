
import cn.nubia.systemui.annotation.LongDef
import cn.nubia.systemui.common.BiometricShowFlagesConstant
import cn.nubia.systemui.fingerprint.process.ActionList

@LongDef(value = longArrayOf(ActionList.ActionKey.KEY_SCREEN_OFF.toLong(),
        ActionList.ActionKey.KEY_SCREEN_ON.toLong(),
        ActionList.ActionKey.KEY_SCREEN_DOZE.toLong(),
        ActionList.ActionKey.KEY_SCREEN_HBM.toLong()))
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class KeyInt{

}


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
    println("NORMAL = {${BiometricShowFlagesConstant.flagsToString(BiometricShowFlagesConstant.TYPE_NORMAL_MASK)}}")
    println("AOD = {${BiometricShowFlagesConstant.flagsToString(BiometricShowFlagesConstant.TYPE_AOD_MASK)}}")
    println("KEYGUARD = {${BiometricShowFlagesConstant.flagsToString(BiometricShowFlagesConstant.TYPE_KEYGUARD_MASK)}}")

    test(1234)

}