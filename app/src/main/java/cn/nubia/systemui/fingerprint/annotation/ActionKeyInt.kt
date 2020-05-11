package cn.nubia.systemui.fingerprint.annotation

import cn.nubia.systemui.annotation.IntDef
import cn.nubia.systemui.fingerprint.process.ActionList

@IntDef(value = intArrayOf(ActionList.ActionKey.KEY_SCREEN_OFF,
        ActionList.ActionKey.KEY_SCREEN_ON,
        ActionList.ActionKey.KEY_SCREEN_DOZE,
        ActionList.ActionKey.KEY_SCREEN_HBM,
        ActionList.ActionKey.KEY_SCREEN_FRAME,
        ActionList.ActionKey.KEY_SCREEN_2FRAME))
@Retention(AnnotationRetention.SOURCE)
annotation class ActionKeyInt