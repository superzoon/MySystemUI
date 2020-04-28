import cn.nubia.systemui.common.BiometricShowFlagesConstant


fun main(args: Array<String>) {
    println("NORMAL = {${BiometricShowFlagesConstant.flagsToString(BiometricShowFlagesConstant.TYPE_NORMAL_MASK)}}")
    println("AOD = {${BiometricShowFlagesConstant.flagsToString(BiometricShowFlagesConstant.TYPE_AOD_MASK)}}")
    println("KEYGUARD = {${BiometricShowFlagesConstant.flagsToString(BiometricShowFlagesConstant.TYPE_KEYGUARD_MASK)}}")
}