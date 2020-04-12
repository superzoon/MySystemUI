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

        //������Բ���е�����
        fun getCircleTangentPointOut(c1: PointF, r1: Float, c2: PointF, r2: Float): Array<PointF?>? {
            val centerLine = getPointDistance(c1, c2)
            if (centerLine > Math.abs(r1 - r2)) {
                //��������
                val points = arrayOfNulls<PointF>(8)
                //Բ��������Բ1�Ľ���
                val r1Point = ratioPoint(c1, c2, r1 / centerLine)
                points[6] = r1Point
                //Բ��������Բ2�Ľ���
                val r2Point = ratioPoint(c1, c2, (centerLine - r2) / centerLine)
                points[7] = r2Point
                //��Ԫ�������ߺ���Բ���������Բ�ĽǶ�
                val angleR1 = getAngle(r1, centerLine, r2)
                //��Ԫ�������ߺ���Բ�������ұ�Բ�ĽǶ�
                val angleR2 = getAngle(r2, centerLine, r1)
                //��������Բ�����ߵĽǶ�(0~90��֮��ĽǶ�)
                val angle = Math.acos((Math.abs(r1 - r2) / centerLine).toDouble()).toFloat()
                //��Բ�Ľ���
                points[4] = rotatePoint(r1Point, c1, angleR1)
                points[5] = rotatePoint(r2Point, c2, angleR2)
                if (r1 >= r2) {
                    //�������һ��Բ�Ľ���
                    points[0] = rotatePoint(r1Point, c1, angle)
                    points[1] = rotatePoint(r1Point, c1, -angle)
                    //������ڶ���Բ�Ľ���
                    points[2] = rotatePoint(r2Point, c2, -(Math.PI - angle).toFloat())
                    points[3] = rotatePoint(r2Point, c2, (Math.PI - angle).toFloat())
                } else {
                    //�������һ��Բ�Ľ���
                    points[0] = rotatePoint(r1Point, c1, (Math.PI - angle).toFloat())
                    points[1] = rotatePoint(r1Point, c1, -(Math.PI - angle).toFloat())
                    //������ڶ���Բ�Ľ���
                    points[2] = rotatePoint(r2Point, c2, -angle)
                    points[3] = rotatePoint(r2Point, c2, angle)
                }
                return points
            }else{
                return null
            }
        }

        //������Բ���е�����
         fun getCircleTangentPointIn(c1: PointF, r1: Float, c2: PointF, r2: Float): Array<PointF?>? {
            val centerLine = getPointDistance(c1, c2)
            if (centerLine > r1 + r2) {
                //��������
                val points = arrayOfNulls<PointF>(7)
                //�����߽���
                points[4] = PointF((c1.x * r2 + c2.x * r1) / (r1 + r2), (c1.y * r2 + c2.y * r1) / (r1 + r2))
                val l1 = centerLine * r1 / (r1 + r2)
                val l2 = centerLine * r2 / (r1 + r2)
                //Բ��������Բ1�Ľ���
                points[5] = ratioPoint(c1, points[4]!!, r1 / l1)
                val angle = Math.acos((r1 / l1).toDouble()).toFloat()
                //��1��Բ���е�
                points[0] = rotatePoint(points[5]!!, c1, angle)
                points[1] = rotatePoint(points[5]!!, c1, -angle)
                //Բ��������Բ2�Ľ���
                points[6] = ratioPoint(points[4], c2, (l2 - r2) / l2)
                //��2��Բ���е�
                points[2] = rotatePoint(points[6]!!, c2, -angle)
                points[3] = rotatePoint(points[6]!!, c2, angle)
                return points
            }
            return null
        }

        //���ݵ� a, b, cλ�þ���Ϊab, bc, ac��ȡb����ac�ϵĴ���d�����ش���d
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

        //��������֮��ľ���
        fun getPointDistance(a: PointF, b: PointF): Float {
            return Math.sqrt(Math.pow((a.x - b.x).toDouble(), 2.0) + Math.pow((a.y - b.y).toDouble(), 2.0)).toFloat()
        }

        //���ݵ� a, b, cλ�þ���Ϊab, bc, ac��ȡa��Ƕ�
        fun getAngle(ab: Float, ac: Float, bc: Float): Float {
            return Math.acos(((ab * ab + ac * ac - bc * bc) / (2f * ab * ac)).toDouble()).toFloat()
        }

        //��ȡһ���㣬��ʼ�㵽�õ㳤�ȳ�����ʼ�㵽�����㳤�ȵı���Ϊratio
        fun ratioPoint(startPoint: PointF?, endPoint: PointF, ratio: Float): PointF {
            var startPoint = startPoint
            if (startPoint == null) {
                startPoint = PointF(0f, 0f)
            }
            val ret = PointF()
            val x = endPoint.x - startPoint.x
            val y = endPoint.y - startPoint.y
            ret.x = x * ratio + startPoint.x
            ret.y = y * ratio + startPoint.y
            return ret
        }

        //�ռ�һ����Χ��center����תangle�ǶȺ��λ��
        fun rotatePoint(point: PointF, center: PointF?, angle: Float): PointF {
            var center = center
            if (center == null) {
                center = PointF(0f, 0f)
            }
            val ret = PointF()
            //��ȡ���λ��
            val x = point.x - center.x
            val y = point.y - center.y
            //����ѡ�������ת��������ĵ�λ��
            ret.x = (x * Math.cos(angle.toDouble()) - y * Math.sin(angle.toDouble()) + center.x).toFloat()
            ret.y = (x * Math.sin(angle.toDouble()) + y * Math.cos(angle.toDouble()) + center.y).toFloat()
            return ret
        }
    }
}