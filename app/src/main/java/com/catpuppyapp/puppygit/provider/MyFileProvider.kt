package com.catpuppyapp.puppygit.provider

import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import androidx.core.content.ContentResolverCompat
import androidx.core.content.FileProvider
import com.catpuppyapp.puppygit.utils.AppModel

class MyFileProvider: FileProvider() {

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
//        若文件修改，这个会被调用，所以只需要在这里把uri转成完整路径再判断路径是否处于safbuf下，若处于再调用safapi去修改saf目标文件即可！
//
        println("fileprovider的openFile被调用了")
        // TODO 只要调用这个函数，就有可能更新文件，开个文件修改监视器，如果发现文件修改，就写入，写入后立刻关闭监视器，每个监视器1分钟后超时自动关闭(避免第一次打开文件后不更新，第一次读取文件时的Listener就会一直运行)。
        //  每个listener都可接收信号，当从saf源更新本地文件时(后称saf pull)，向所有listener发送信号，让他们停止工作，等都停止后，再执行saf pull，
        //  每次进入此函数先把当前文件的所有listener关闭，然后开个新listener。
        //  有点tm的恶心啊，没别的办法监听到文件修改吗？

        //我能不能直接在这把saf的fd分享出去？
//        context.contentResolver.openFile()
        //TODO 直接在这把saf 关联的uri的file descriptor返回不就行了？
        return super.openFile(uri, mode)
    }

    override fun openFile(uri: Uri, mode: String, signal: CancellationSignal?): ParcelFileDescriptor? {
        println("fileprovider的openFile2被调用了")

        return super.openFile(uri, mode, signal)
    }

}
