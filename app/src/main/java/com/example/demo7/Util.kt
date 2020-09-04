package com.example.demo7

import android.content.Context
import android.content.SharedPreferences

class Util private constructor(){
    companion object{
        private var instance:Util?=null
        private var mycontext:Context?=null
        fun getInstance(context: Context):Util{
            mycontext=context
            if(instance == null){
                instance= Util()
            }
            return instance!!
            }
        }

    fun savaPassword(pwd:String){
        //获取sharedPerference对象
        val sharedPerference:SharedPreferences?=mycontext?.getSharedPreferences("zdw", Context.MODE_PRIVATE)
        //获取edit对象-》写数据
        val edit:SharedPreferences.Editor?=sharedPerference?.edit()
        //写入数据
        edit?.putString("pwd",pwd)

        //提交
        edit?.commit()
    }

    fun getPassword():String?{
        //获取sharedPerference对象
        val sharedPerference:SharedPreferences?=mycontext?.getSharedPreferences("zdw", Context.MODE_PRIVATE)
        return sharedPerference?.getString("pwd",null)

    }
}