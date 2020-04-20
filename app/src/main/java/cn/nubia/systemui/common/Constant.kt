package cn.nubia.systemui.common

class SystemUIStateConstant{
    companion object {
        val STATE_AUTHENTICATED = 1 shl 0
        val STATE_EXPANDED = 1 shl 1
        val STATE_INCALL =  1 shl 2
        val STATE_SUPERSNAP_VIEW = 1 shl 3
        val STATE_AOD_UI = 1 shl 4
        val STATE_SLIDE_CAMERA = 1 shl 5
        val STATE_SLIDE_NAVI = 1 shl 6
        val STATE_SHUTDOWN_SHOW = 1 shl 7
        val STATE_POCKET_SHOW = 1 shl 8
        val STATE_STRONG_AUTH = 1 shl 9
        val STATE_MAX = 1 shl 10

        fun isValidState(flags:Int):Boolean = (flags>=0) && (flags<STATE_MAX)
        fun hasAuth(flags: Int) = (flags and STATE_AUTHENTICATED)!=0
        fun hasExpanded(flags: Int) = (flags and STATE_EXPANDED)!=0
        fun hasIncall(flags: Int) = (flags and STATE_INCALL)!=0
        fun hasSupersnap(flags: Int) = (flags and STATE_SUPERSNAP_VIEW)!=0
        fun hasAod(flags: Int) = (flags and STATE_AOD_UI)!=0
        fun hasSlideCamera(flags: Int) = (flags and STATE_SLIDE_CAMERA)!=0
        fun hasSlideNavi(flags: Int) = (flags and STATE_SLIDE_NAVI)!=0
        fun hasShutdownView(flags: Int) = (flags and STATE_SHUTDOWN_SHOW)!=0
        fun hasPocketView(flags: Int) = (flags and STATE_POCKET_SHOW)!=0
        fun hasStrongAuth(flags: Int) = (flags and STATE_STRONG_AUTH)!=0

        fun getStateString(flags:Int):String{
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
                    build.append("StrongAuth")
                }
                return build.toString()
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
    }
}