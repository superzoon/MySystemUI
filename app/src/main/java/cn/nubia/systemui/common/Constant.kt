package cn.nubia.systemui.common

class BiometricShowFlagesConstant{
    companion object {
        //无状态
        val STATE_NORMAL = 0
        //启动指纹识别
        val STATE_AUTHENTICATED = 1 shl 0
        //非锁屏状态栏下拉
        val STATE_EXPANDED = 1 shl 1
        //锁屏状态栏下拉
        val STATE_QS_EXPANDED = 1 shl 2
        //通话状态？通话界面(occulded)？ 是否需要这个判断
        val STATE_INCALL =  1 shl 3
        //正在截屏？
        val STATE_SUPERSNAP = 1 shl 4
        //AOD显示？
        val STATE_AOD_UI = 1 shl 5
        //锁屏滑动相机图标
        val STATE_SLIDE_CAMERA = 1 shl 6
        //手势进多任务ing
        val STATE_SLIDE_NAVI = 1 shl 7
        //显示关机界面
        val STATE_SHUTDOWN_SHOW = 1 shl 8
        //口袋模式显示
        val STATE_POCKET_SHOW = 1 shl 9
        //需要强制密码认证
        val STATE_STRONG_AUTH = 1 shl 10
        //输入法显示
        val STATE_INPUT_METHOD = 1 shl 11
        //非锁屏类ACTIVITY *UnKeyguardOccluded* 锁屏音乐等白名单不属于
        val STATE_KEYGUARD = 1 shl 12
        val STATE_UK_OCCLUDED = 1 shl 13
        val STATE_GALLERY_SHOW = 1 shl 14
        val STATE_MASK = (1 shl 15) - 1

        fun isValidState(flags:Int):Boolean = STATE_MASK.xor(flags) > STATE_NORMAL
        fun hasAuth(flags: Int) = (flags and STATE_AUTHENTICATED) == STATE_AUTHENTICATED
        fun hasExpanded(flags: Int) = (flags and STATE_EXPANDED) == STATE_AUTHENTICATED
        fun hasQsExpanded(flags: Int) = (flags and STATE_QS_EXPANDED) == STATE_AUTHENTICATED
        fun hasIncall(flags: Int) = (flags and STATE_INCALL) == STATE_AUTHENTICATED
        fun hasSupersnap(flags: Int) = (flags and STATE_SUPERSNAP) == STATE_AUTHENTICATED
        fun hasAod(flags: Int) = (flags and STATE_AOD_UI) == STATE_AUTHENTICATED
        fun hasSlideCamera(flags: Int) = (flags and STATE_SLIDE_CAMERA) == STATE_AUTHENTICATED
        fun hasSlideNavi(flags: Int) = (flags and STATE_SLIDE_NAVI) == STATE_AUTHENTICATED
        fun hasShutdownView(flags: Int) = (flags and STATE_SHUTDOWN_SHOW) == STATE_AUTHENTICATED
        fun hasPocketView(flags: Int) = (flags and STATE_POCKET_SHOW) == STATE_AUTHENTICATED
        fun hasStrongAuth(flags: Int) = (flags and STATE_STRONG_AUTH) == STATE_AUTHENTICATED
        fun hasInputMethod(flags: Int) = (flags and STATE_INPUT_METHOD) == STATE_AUTHENTICATED
        fun hasKeyguard(flags: Int) = (flags and STATE_KEYGUARD) == STATE_AUTHENTICATED
        fun hasUkOccluded(flags: Int) = (flags and STATE_UK_OCCLUDED) == STATE_AUTHENTICATED
        fun hasGalleryShow(flags: Int) = (flags and STATE_GALLERY_SHOW) == STATE_AUTHENTICATED

        val TYPE_NORMAL = 1 shl 0
        val TYPE_NORMAL_MASK = STATE_NORMAL
                .or(STATE_AUTHENTICATED)
                .or(STATE_EXPANDED)
                .or(STATE_SUPERSNAP)
                .or(STATE_SLIDE_NAVI)
                .or(STATE_STRONG_AUTH)
                .or(STATE_SHUTDOWN_SHOW)
                .or(STATE_INPUT_METHOD)

        val TYPE_AOD = 1 shl 1
        val TYPE_AOD_MASK = STATE_NORMAL
                .or(STATE_AUTHENTICATED)
                .or(STATE_AOD_UI)
                .or(STATE_SUPERSNAP)

        val TYPE_KEYGUARD = 1 shl 2
        val TYPE_KEYGUARD_MASK = STATE_NORMAL
                .or(STATE_AUTHENTICATED)
                .or(STATE_QS_EXPANDED)
                .or(STATE_SUPERSNAP)
                .or(STATE_SLIDE_CAMERA)
                .or(STATE_SLIDE_NAVI)
                .or(STATE_SHUTDOWN_SHOW)
                .or(STATE_STRONG_AUTH)
                .or(STATE_INPUT_METHOD)
                .or(STATE_UK_OCCLUDED)
                .or(STATE_GALLERY_SHOW)

        fun canShowFingerprint(state:Int):Boolean = STATE_AUTHENTICATED == when{
                !isValidState(state) -> throw IllegalAccessError("type=${state} is error")
                hasAod(state) ->  state and TYPE_NORMAL_MASK
                hasKeyguard(state) -> state and TYPE_AOD_MASK
                else -> state and TYPE_KEYGUARD_MASK
            }

        fun flagsToString(flags:Int):String{
            if(flags==0){
                return "UnAuth"
            }else{
                val build = StringBuilder()
                if(hasAuth(flags)){
                    build.append("Auth ")
                }
                if(hasExpanded(flags)){
                    build.append("Expanded ")
                }
                if(hasQsExpanded(flags)){
                    build.append("QsExpanded ")
                }
                if(hasIncall(flags)){
                    build.append("Incall ")
                }
                if(hasSupersnap(flags)){
                    build.append("Supersnap ")
                }
                if(hasAod(flags)){
                    build.append("Aod ")
                }
                if(hasSlideCamera(flags)){
                    build.append("SlideCamera ")
                }
                if(hasSlideNavi(flags)){
                    build.append("SlideNavi ")
                }
                if(hasShutdownView(flags)){
                    build.append("Shutdown ")
                }
                if(hasPocketView(flags)){
                    build.append("Pocket ")
                }
                if(hasStrongAuth(flags)){
                    build.append("StrongAuth ")
                }
                if(hasInputMethod(flags)){
                    build.append("InputMethod ")
                }
                if(hasUkOccluded(flags)){
                    build.append("UkOccluded ")
                }
                if(hasGalleryShow(flags)){
                    build.append("Gallery ")
                }
                return build.toString().trim()
            }
        }

    }
}
class BiometricConstant{
    companion object {
        val TYPE_SHOW = 1
        val TYPE_HIDE = 2
        val TYPE_AUTHENTICATED = 3
        val TYPE_HELP = 4
        val TYPE_ERROR = 5
        val TYPE_ATTR_FLAGES = 6
        val TYPE_INFO = 7
    }
}
class FingerprintInfo{
    companion object {
        //指纹记录过短，请延迟触摸时间
        val MSG_RECORD_MOVING_TOO_FALST = 5
        //调整手指位置记录录入
        val MSG_RECORD_MOVE_FINGER = 1106
        //手指稍许用例，增加解除面积，提高准确性
        val MSG_RECORD_ACQUIRED_PARTIAL = 1
        //请在尝试一次
        val MSG_RECORD_ACQUIRED_INSUFFICIENT = 2
        //请保持屏幕干燥清洁
        val MSG_RECORD_ACQUIRED_IMAGE_DIRTY = 3

        //手指移动太慢
        val MSG_VERIFY_ACQUIRED_TOO_SLOW = 4
        //手指移动太快
        val MSG_VERIFY_ACQUIRED_TOO_FAST = 5
        //预处理失败，请在尝试一次
        val MSG_ACQUIRED_VENDOR_TRY_AGAIN = 1120

        //指纹硬件无法使用
        val MSG_HINT_ERROR_HW = 1
        //请重试
        val MSG_HINT_ERROR_UNABLE_TO_PROCESS = 2
        //指纹录入超时
        val MSG_HINT_ERROR_TIMEOUT = 3
        //无法存储指纹，请移除一个现有指纹
        val MSG_HINT_ERROR_NO_SPACE = 4
        //指纹操作取消
        val MSG_HINT_ERROR_CANCELED = 5
        //尝试次数过多，请稍后重试
        val MSG_HINT_ERROR_LOCKOUT = 7
        //尝试次数过多，传感器已停止工作
        val MSG_HINT_ERROR_LOCKOUT_PERMANENT = 8
        //用户取消操作
        val MSG_HINT_ERROR_USER_CANCELED = 10
        //未注册任何指纹
        val MSG_HINT_ERROR_NO_FINGERPRINTS = 11
        //此设备没有指纹传感器
        val MSG_HINT_ERROR_HW_NOT_PRESENT = 12
        //手指按下
        val MSG_HINT_FINGER_DOWN = 1102
        //手指抬起
        val MSG_HINT_FINGER_UP = 1103

        //录入结果
        val MSG_ENROLL_RESULT = 100
        //捕获
        val MSG_ACQUIRED = 101
        //验证成功
        val MSG_AUTHENTICATION_SUCCEEDED = 102
        //验证失败
        val MSG_AUTHENTICATION_FAILED = 103
        //出错
        val MSG_ERROR = 104
        //移除
        val MSG_REMOVED = 105
        //枚举
        val MSG_ENUMERATED = 106
        val MSG_EENROLL_BASE_CODE = 2200
        infix fun isVibrateError(info:Int):Boolean{
            return info == MSG_ACQUIRED_VENDOR_TRY_AGAIN
        }

        operator fun contains(info:Int):Boolean{
            return false
        }
    }
}

class BiometricCmd{
    companion object {
        val CMD_DOWN = 13
        val CMD_UI_READY = 14
        val CMD_UP = 15
        operator fun contains(value:Int):Boolean =(value>=CMD_DOWN) and (value<=CMD_UP)
    }
}
class SystemUIStateConstant{
    companion object {
        val TYPE_TEST = 1
    }
}