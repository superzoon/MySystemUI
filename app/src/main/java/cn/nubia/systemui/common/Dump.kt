package cn.nubia.systemui.common

import java.io.FileDescriptor
import java.io.PrintWriter

interface Dump {
    fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?){}
}