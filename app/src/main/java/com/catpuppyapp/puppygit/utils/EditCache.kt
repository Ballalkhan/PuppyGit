package com.catpuppyapp.puppygit.utils

import com.catpuppyapp.puppygit.utils.base.DateNamedFileWriter
import java.io.IOException
import java.time.LocalDateTime

object EditCache: DateNamedFileWriter(
    TAG = "EditCache",  //debug TAG
    fileNameTag = "EditCache",
)  {

    private var enable = true  //是否启用EditCache。（这的赋值只是初始值，实际会在init方法里更新此值，那个值才是真正有效的，而init的值与设置项对应条目关联）


    private var isInited = false


    //    public Context context;
    fun init(enableCache:Boolean, cacheDirPath:String, keepInDays: Int=fileKeepDays) {
        try {
            //这两个变量删除过期文件需要用，所以无论是否启用cache，都初始化这两个变量
            fileKeepDays = keepInDays
            saveDirPath = cacheDirPath

            //只有启用cache才有必要初始化writer，否则根本不会写入文件，自然也不需要初始化writer
            //及时禁用也不终止writer协程，不过调用write将不会执行操作
            enable = enableCache
            if(enableCache) {
                isInited=true
                startWriter()
            }else {
                //禁用cache的情况下，不会初始化writer，所以初始化flag应该为假，然后写入的时候会因为未init而不执行写入
                isInited = false
            }
        }catch (e:Exception) {
            isInited=false

            MyLog.e(TAG, "#init err:"+e.stackTraceToString())

        }
    }


    /**
     * @param text  要写入的内容
     */
    fun writeToFile(text: String) {
        //只有初始化成功且启用了edit cache的情况下，才记录cache
        if(!isInited || !enable) {
            return
        }

        //忽略空行
        if (text.isBlank()) {
            return
        }

        doJobThenOffLoading {
            try {
                //初始化完毕并且启用cache，则写入内容到cache
                val nowTimestamp = contentTimestampFormatter.format(LocalDateTime.now())
                val needWriteMessage = "-------- $nowTimestamp :\n$text"

                sendMsgToWriter(nowTimestamp, needWriteMessage)

            } catch (e: IOException) {
                MyLog.e(TAG, "#writeToFile err:"+e.stackTraceToString())
            }
        }
    }

}
