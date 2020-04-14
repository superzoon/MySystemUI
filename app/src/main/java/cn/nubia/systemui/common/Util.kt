package cn.nubia.systemui.fingerprint

import android.graphics.PointF
import android.util.Log
import cn.nubia.systemui.NubiaSystemUIApplication
import java.io.FileDescriptor
import java.io.PrintWriter
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private val TAG = "${NubiaSystemUIApplication.TAG}.Util"
private var mAodMode:AtomicInteger = AtomicInteger(0)
private var mIsHbm:AtomicBoolean = AtomicBoolean(false)

@Synchronized fun setAodMode(mode:Int){
    if(mAodMode.get() != mode){
        mAodMode.getAndSet(mode)
    }
}

@Synchronized fun setHBM(enable:Boolean){
    if(mIsHbm.get() != enable){
        mIsHbm.getAndSet(enable)
    }
}


@Synchronized fun writeNode(path:String, value:String){
    Log.i(TAG,"writeNode ${path}:${value}")
}

fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
    Log.i(TAG,"dump ${fd}.${writer}.${args}")
}

class PointUtil{
    companion object {

        //返回两圆外切点坐标
        fun getCircleTangentPointOut(c1: PointF, r1: Float, c2: PointF, r2: Float): Array<PointF?>? {
            val centerLine = getPointDistance(c1, c2)
            if (centerLine > Math.abs(r1 - r2)) {
                //计算外切
                val points = arrayOfNulls<PointF>(8)
                //圆心连线与圆1的交点
                val r1Point = ratioPoint(c1, c2, r1 / centerLine)
                points[6] = r1Point
                //圆心连线与圆2的交点
                val r2Point = ratioPoint(c1, c2, (centerLine - r2) / centerLine)
                points[7] = r2Point
                //两元交点连线和两圆焦点在左边圆的角度
                val angleR1 = getAngle(r1, centerLine, r2)
                //两元交点连线和两圆焦点在右边圆的角度
                val angleR2 = getAngle(r2, centerLine, r1)
                //外切线与圆心连线的角度(0~90度之间的角度)
                val angle = Math.acos((Math.abs(r1 - r2) / centerLine).toDouble()).toFloat()
                //两圆的交点
                points[4] = rotatePoint(r1Point, c1, angleR1)
                points[5] = rotatePoint(r2Point, c2, angleR2)
                if (r1 >= r2) {
                    //切线与第一个圆的交点
                    points[0] = rotatePoint(r1Point, c1, angle)
                    points[1] = rotatePoint(r1Point, c1, -angle)
                    //切线与第二个圆的交点
                    points[2] = rotatePoint(r2Point, c2, -(Math.PI - angle).toFloat())
                    points[3] = rotatePoint(r2Point, c2, (Math.PI - angle).toFloat())
                } else {
                    //切线与第一个圆的交点
                    points[0] = rotatePoint(r1Point, c1, (Math.PI - angle).toFloat())
                    points[1] = rotatePoint(r1Point, c1, -(Math.PI - angle).toFloat())
                    //切线与第二个圆的交点
                    points[2] = rotatePoint(r2Point, c2, -angle)
                    points[3] = rotatePoint(r2Point, c2, angle)
                }
                return points
            }else{
                return null
            }
        }

        //返回两圆内切点坐标
         fun getCircleTangentPointIn(c1: PointF, r1: Float, c2: PointF, r2: Float): Array<PointF?>? {
            val centerLine = getPointDistance(c1, c2)
            if (centerLine > r1 + r2) {
                //计算内切
                val points = arrayOfNulls<PointF>(7)
                //内切线焦点
                points[4] = PointF((c1.x * r2 + c2.x * r1) / (r1 + r2), (c1.y * r2 + c2.y * r1) / (r1 + r2))
                val l1 = centerLine * r1 / (r1 + r2)
                val l2 = centerLine * r2 / (r1 + r2)
                //圆心连线与圆1的交点
                points[5] = ratioPoint(c1, points[4]!!, r1 / l1)
                val angle = Math.acos((r1 / l1).toDouble()).toFloat()
                //第1个圆的切点
                points[0] = rotatePoint(points[5]!!, c1, angle)
                points[1] = rotatePoint(points[5]!!, c1, -angle)
                //圆心连线与圆2的交点
                points[6] = ratioPoint(points[4], c2, (l2 - r2) / l2)
                //第2个圆的切点
                points[2] = rotatePoint(points[6]!!, c2, -angle)
                points[3] = rotatePoint(points[6]!!, c2, angle)
                return points
            }
            return null
        }

        //根据点 a, b, c位置距离为ab, bc, ac获取b点在ac上的垂点d，返回垂点d
        fun getVerticalPoint(a: PointF, b: PointF, c: PointF): PointF {
            val ab = getPointDistance(a, b)
            val ac = getPointDistance(a, c)
            val bc = getPointDistance(b, c)
            return getVerticalPoint(ab, ac, bc, a, c)
        }

        fun getVerticalPoint(ab: Float, ac: Float, bc: Float, a: PointF, c: PointF): PointF {
            val angle = getAngle(ab, ac, bc)
            val ratio = (Math.cos(angle.toDouble()) * ab).toFloat() / ac
            return ratioPoint(a, c, ratio)
        }

        //返回两点之间的距离
        fun getPointDistance(a: PointF, b: PointF): Float {
            return Math.sqrt(Math.pow((a.x - b.x).toDouble(), 2.0) + Math.pow((a.y - b.y).toDouble(), 2.0)).toFloat()
        }

        //根据点 a, b, c位置距离为ab, bc, ac获取a点角度
        fun getAngle(ab: Float, ac: Float, bc: Float): Float {
            return Math.acos(((ab * ab + ac * ac - bc * bc) / (2f * ab * ac)).toDouble()).toFloat()
        }

        //获取一个点，起始点到该点长度除以起始点到结束点长度的比例为ratio
        fun ratioPoint(startPoint: PointF?, endPoint: PointF, ratio: Float): PointF {
            var startPoint =  if (startPoint == null) {
                PointF(0f, 0f)
            } else{
                startPoint
            }
            val ret = PointF()
            val x = endPoint.x - startPoint.x
            val y = endPoint.y - startPoint.y
            ret.x = x * ratio + startPoint.x
            ret.y = y * ratio + startPoint.y
            return ret
        }

        //空间一个点围绕center点旋转angle角度后的位置
        fun rotatePoint(point: PointF, center: PointF?, angle: Float): PointF {
            var center = if (center == null) {
                PointF(0f, 0f)
            }else{
                center
            }
            val ret = PointF()
            //获取相对位置
            val x = point.x - center.x
            val y = point.y - center.y
            //根据选择矩阵旋转后加上中心点位置
            ret.x = (x * Math.cos(angle.toDouble()) - y * Math.sin(angle.toDouble()) + center.x).toFloat()
            ret.y = (x * Math.sin(angle.toDouble()) + y * Math.cos(angle.toDouble()) + center.y).toFloat()
            return ret
        }
    }
}