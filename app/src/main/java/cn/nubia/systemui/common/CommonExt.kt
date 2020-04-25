package cn.nubia.systemui.common

import android.view.View
import android.widget.Button
import android.widget.EditText
/*
    扩展点击事件
 */
fun View.onClick(listener:View.OnClickListener):View{
    setOnClickListener(listener)
    return this
}

/*
    扩展点击事件，参数为方法
 */
fun View.onClick(method:() -> Unit):View{
    setOnClickListener { method() }
    return this
}
/*
    扩展视图可见性
 */
fun View.setVisible(visible:Boolean){
    this.visibility = if (visible) View.VISIBLE else View.GONE
}
/**
表达式	 对应的函数
a+b	     a.plus(b)
a-b	     a.minus(b)
a*b	     a.tims(b)
a/b	     a.div(b)
a%b      a.mod(b)
a.b    	 a.rangeTo(b)
a in b	 b.contains(a)
a !in b	!b.contains(a)
*/