package cn.nubia.systemui.common.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Target


@Retention(AnnotationRetention.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
annotation class IntDef(vararg val value: Int = intArrayOf(), val flag: Boolean = false)