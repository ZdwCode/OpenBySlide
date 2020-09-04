package com.example.demo7

import android.app.Activity
import android.app.UiAutomation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    //用一个数组保存九个红色圆点的对象  用于滑动过程中遍历
    /**private var dots:Array<ImageView>?=null
    fun  init():Unit{
        dots=arrayOf(dot1,dot2,dot3,dot4,dot5,dot6,dot7,dot8,dot9)
    }*/
    private  val dotsView:Array<ImageView> by lazy {
        arrayOf(sdot1,sdot2,sdot3,sdot4,sdot5,sdot6,sdot7,sdot8,sdot9)
    }

    //使用懒加载获取屏幕状态栏的高度
    private val barheight:Int by lazy {
        val display=DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(display)
        //获取操作区域的尺寸
        val rect=Rect()
        window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT).getDrawingRect(rect)
        display.heightPixels-rect.height()
    }

    //定义一个数组保存当前被点亮的点—— 用来熄灭
    private val selectedViews= mutableListOf<ImageView>()

    //记录滑动过程中的轨迹
    private  val password=StringBuilder()

    //保存所有线tag值
    private val allLineTag= arrayOf(12,23,45,56,78,89,14,25,36,47,58,69,15,26,48,59,24,35,57,68)

    //记录最后被点亮的圆点对象
    private  var lastdot:ImageView?= null

    //记录原始密码
    private  var orginalPwd:String?=null

    //记录第一次设置的密码
    private  var firstPwd:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //保存密码
        Util.getInstance(this).getPassword().also {
            if(it == null){//没有密码->现在设置
                mText.text="请设置密码"
            }else{//已有密码
                mText.text="请解锁"
                orginalPwd=it
            }
        }

        //给头像图片添加点击事件
        myHead.setOnClickListener{
            //从相机里获取图片
            val intent=Intent()

            intent.action=Intent.ACTION_PICK

            intent.type="image/*"

            startActivityForResult(intent,123)
        }

        //获取头像
        BitmapFactory.decodeFile("${filesDir.path}/header.jpg")
            .also {
                myHead.setImageBitmap(it)
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            123->{
                //图片
                //判断用户是否取消操作
                if(resultCode != Activity.RESULT_CANCELED){
                    data?.data?.let {uri->
                        contentResolver.openInputStream(uri).use {
                            BitmapFactory.decodeStream(it).also { image->
                                myHead.setImageBitmap(image)

                                //把图片缓存起来
                                val myFile=File(filesDir,"header.jpg")
                                FileOutputStream(myFile).also {fos->
                                    image.compress(Bitmap.CompressFormat.JPEG,50,fos)

                                }


                            }
                        }
                    }


                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                val point=convertTouchLocationToContainer(event)
                findViewContainPoint(point).also {
                    if(it != null && it.visibility == ImageView.INVISIBLE){
                        hightView(it)
                    }
                }
            }
            MotionEvent.ACTION_MOVE->{
                val point=convertTouchLocationToContainer(event)
                findViewContainPoint(point).also {
                    if(it != null && it.visibility == ImageView.INVISIBLE){

                        hightView(it)
                    }
                }
            }
            MotionEvent.ACTION_UP->{
                //判断是不是第一次设置密码
                if(orginalPwd == null){//第一次->设置密码
                    if(firstPwd==null) {  //设置密码分两次 第一次设置密码
                        firstPwd = password.toString()
                        mText.text="请输入和上次相同的密码"
                    }else{
                        if(firstPwd==password.toString()){
                            //两次密码一致
                            mText.text="设置密码成功"

                            //保存密码
                            Util.getInstance(this).savaPassword(firstPwd!!)
                        }else{
                            //两次密码不一致
                            mText.text="密码不一致，请重新输入"
                            firstPwd=null
                        }
                    }
                }else{//不是第一次
                    //判断密码是否相同
                    if(orginalPwd==password.toString()){
                        mText.text="解锁成功"
                    }else{
                        mText.text="密码错误请重新输入"
                    }
                }
                reset()
            }
        }
        return true
    }

    //获取标题栏和状态栏的高度 -> 改用懒加载
    /**private  fun barheight():Int{
        //获取屏幕的尺寸
        val display=DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(display)
        //获取操作区域的尺寸
        val rect=Rect()
        window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT).getDrawingRect(rect)
        return display.heightPixels-rect.height()
    }*/

    //讲触摸点的坐标转化为相对容器的坐标
    private fun convertTouchLocationToContainer(event: MotionEvent):Point{
        val point=Point().apply {
            x=(event.x-container.x).toInt()
            y=(event.y-barheight-container.y).toInt()
        }
        return point
    }

    //获取当前触摸点所在的圆点
    private  fun findViewContainPoint(point: Point):ImageView?{
        //遍历所有的点是都包含point
        for (dotView:ImageView in dotsView){
            //判断触摸点是否在这个点中
            getRect(dotView).also {
                if(it.contains(point.x,point.y)){
                    return dotView
                }
            }
        }
        return null
    }

    private  fun getRect(v:ImageView):Rect{
        val rect=Rect().apply {
            left=v.left
            top=v.top
            right=v.right
            bottom=v.bottom
        }
        return  rect
    }
    //点亮
    private  fun hightView(view:ImageView){
        //判断这个点是否为第一个点->点亮并保存
        if(lastdot==null) {
            highlightDot(view)
        }else{//在滑动的时候已经点亮了其他的点了
            //1.获取线的tag值
            val lastTag=(lastdot?.tag as String ).toInt()
            val currentTag=(view.tag as String).toInt()
            val lineTag=if(lastTag>currentTag) currentTag*10+lastTag else lastTag*10+currentTag

            //判断是否有这条线
            if(allLineTag.contains(lineTag)){ //如果有
                //点亮点
                highlightDot(view)
                //点亮线
                container.findViewWithTag<ImageView>(lineTag.toString()).also {
                    it.visibility=ImageView.VISIBLE
                    selectedViews.add(it)
                }

            }
        }
    }

    private  fun highlightDot(view:ImageView){
        view.visibility = ImageView.VISIBLE
        selectedViews.add(view)
        password.append(view.tag)
        lastdot = view
    }
    //还原点亮的点和线
    private fun reset(){
        for (selectView:ImageView in selectedViews){
            selectView.visibility=ImageView.INVISIBLE
        }
        lastdot=null
        selectedViews.clear()

        Log.v("zdw","${password.toString()}")

        password.clear()
    }
}
