import cn.nubia.systemui.common.BiometricDiplayConstant


fun main(args: Array<String>) {
    println("NORMAL = {${BiometricDiplayConstant.flagsToString(BiometricDiplayConstant.TYPE_NORMAL_MASK)}}")
    println("AOD = {${BiometricDiplayConstant.flagsToString(BiometricDiplayConstant.TYPE_AOD_MASK)}}")
    println("KEYGUARD = {${BiometricDiplayConstant.flagsToString(BiometricDiplayConstant.TYPE_KEYGUARD_MASK)}}")
}