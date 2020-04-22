package cn.nubia.systemui.common

class BiometricDiplayConstant{
    companion object {

        //启动指纹识别
        val STATE_AUTHENTICATED = 1 shl 0
        //非锁屏状态栏下拉
        val STATE_EXPANDED = 1 shl 1
        //锁屏状态栏下拉
        val STATE_QS_EXPANDED = 1 shl 2
        //通话状态？通话界面(occulded)？ 是否需要这个判断
        val STATE_INCALL =  1 shl 3
        //正在截屏？
        val STATE_SUPERSNAP_VIEW = 1 shl 4
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
        val STATE_UK_OCCLUDED = 1 shl 12
        val STATE_MAX = 1 shl 13

        fun isValidState(flags:Int):Boolean = (flags>=0) && (flags<STATE_MAX)
        fun hasAuth(flags: Int) = (flags and STATE_AUTHENTICATED)!=0
        fun hasExpanded(flags: Int) = (flags and STATE_EXPANDED)!=0
        fun hasQsExpanded(flags: Int) = (flags and STATE_QS_EXPANDED)!=0
        fun hasIncall(flags: Int) = (flags and STATE_INCALL)!=0
        fun hasSupersnap(flags: Int) = (flags and STATE_SUPERSNAP_VIEW)!=0
        fun hasAod(flags: Int) = (flags and STATE_AOD_UI)!=0
        fun hasSlideCamera(flags: Int) = (flags and STATE_SLIDE_CAMERA)!=0
        fun hasSlideNavi(flags: Int) = (flags and STATE_SLIDE_NAVI)!=0
        fun hasShutdownView(flags: Int) = (flags and STATE_SHUTDOWN_SHOW)!=0
        fun hasPocketView(flags: Int) = (flags and STATE_POCKET_SHOW)!=0
        fun hasStrongAuth(flags: Int) = (flags and STATE_STRONG_AUTH)!=0
        fun hasInputMethod(flags: Int) = (flags and STATE_INPUT_METHOD)!=0
        fun hasUkOccluded(flags: Int) = (flags and STATE_UK_OCCLUDED)!=0

        val TYPE_NORMAL = 1 shl 0
        val TYPE_NORMAL_MASK = 0
                .or(STATE_AUTHENTICATED)
                .or(STATE_SUPERSNAP_VIEW)
                .or(STATE_SLIDE_NAVI)
                .or(STATE_STRONG_AUTH)
                .or(STATE_EXPANDED)
                .or(STATE_SHUTDOWN_SHOW)
                .or(STATE_STRONG_AUTH)
                .or(STATE_INPUT_METHOD)

        val TYPE_AOD = 1 shl 1
        val TYPE_AOD_MASK = 0
                .or(STATE_AUTHENTICATED)
                .or(STATE_AOD_UI)
                .or(STATE_SUPERSNAP_VIEW)

        val TYPE_KEYGUARD = 1 shl 2
        val TYPE_KEYGUARD_MASK = 0
                .or(STATE_AUTHENTICATED)
                .or(STATE_QS_EXPANDED)
                .or(STATE_SUPERSNAP_VIEW)
                .or(STATE_SLIDE_CAMERA)
                .or(STATE_SLIDE_NAVI)
                .or(STATE_SHUTDOWN_SHOW)
                .or(STATE_STRONG_AUTH)
                .or(STATE_INPUT_METHOD)
                .or(STATE_UK_OCCLUDED)

        fun canShowFingerprint(type:Int, state:Int):Boolean{
            val auth = when(type){
                TYPE_NORMAL ->  state and TYPE_NORMAL_MASK
                TYPE_AOD -> state and TYPE_AOD_MASK
                TYPE_KEYGUARD -> state and TYPE_KEYGUARD_MASK
                else -> throw IllegalAccessError("type=${type} is error")
            }
            return auth == STATE_AUTHENTICATED
        }

        fun toString(flags:Int):String{
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
                return build.toString().trim()
            }
        }

    }
}
class SystemUIStateConstant{
    companion object {
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
    }
}