package cn.nubia.systemui.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
annotation class LongDef(
        /** Defines the allowed constants for this element  */
        vararg val value: Long = [],
        /** Defines whether the constants can be used as a flag, or just as an enum (the default)  */
        val flag: Boolean = false)