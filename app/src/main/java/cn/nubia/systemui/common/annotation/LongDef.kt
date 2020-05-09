package cn.nubia.systemui.common.annotation

import java.lang.annotation.ElementType
import java.lang.annotation.Target


@Retention(AnnotationRetention.SOURCE)
@Target(ElementType.ANNOTATION_TYPE)
annotation class LongDef(vararg val value: Long = longArrayOf(), val flag: Boolean = false)