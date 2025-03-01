package com.catpuppyapp.puppygit.utils

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

object SafAndFileCmpUtil {

    class SafAndFileCompareResult (
        /**
         * saf to files的时候，这个应该在files被添加到files；files to saf的时候，这个应该从saf被删除
         */
        val onlyInSaf:MutableList<DocumentFile> = mutableListOf(),
        /**
         * saf to files，这个应该从files里删除；files to saf，这个应该被添加到saf目录。
         */
        val onlyInFiles:MutableList<File> = mutableListOf(),

        /**
         * 两者相同目录都有同样文件，但文件内容或类型不同
         * saf to files，用saf的覆盖files的；files to saf，用files的覆盖saf的
         */
        val bothAndNotSame:MutableList<SafAndFileDiffPair> = mutableListOf(),

        //缺一个 bothAndSame，不过没必要记录一样的，并不需要操作它们
    ) {
        override fun toString(): String {
            return "onlyInSaf.size=${onlyInSaf.size}, onlyInFiles.size=${onlyInFiles.size}, bothAndNotSame.size=${bothAndNotSame.size}\n\n\n" +
                    "onlyInSaf=${onlyInSaf.map { it.uri.toString() + "\n\n" }}\n\n" +
                    "onlyInFiles=${onlyInFiles.map { it.canonicalPath + "\n\n" }}\n\n" +
                    "bothAndNotSame=${bothAndNotSame.map { 
                        "safFile=${it.safFile.uri}, file=${it.file.canonicalPath}, diffType=${it.diffType}\n\n"
                    }}\n\n"
        }
    }

    /**
     * 类似git的delta，保存两个目标的pair
     */
    class SafAndFileDiffPair(
        val safFile: DocumentFile,
        val file: File,
        val diffType:SafAndFileDiffType
    )

    enum class SafAndFileDiffType(val code: Int) {
        /**
         * 没区别，完全一样
         */
        NONE(0),

        /**
         * 内容不同
         */
        CONTENT(1),

        /**
         * 类型不同，例如一个是目录，另一个是文件
         */
        TYPE(2)
        ;

        companion object {
            fun fromCode(code: Int): SafAndFileDiffType? {
                return SafAndFileDiffType.entries.find { it.code == code }
            }
        }
    }

    /**
     * 打开uri失败
     */
    class OpenInputStreamFailed(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
        // 可以在这里添加额外的属性或方法
    }

    /**
     * 比较DocumentFile和File目录的区别，保存结果盗result
     */
    fun recursiveCompareFiles_Saf(
        contentResolver: ContentResolver,
        safFiles:Array<DocumentFile>,
        files:Array<File>,
        result:SafAndFileCompareResult,
        canceled:()->Boolean,
    ) {
        if(canceled()) {
            throw CancellationException()
        }

        val safFiles = safFiles.toMutableList()
        val files = files.toMutableList()

        for(f in files) {

            if(canceled()) {
                throw CancellationException()
            }

            //saff = saf file
            val saffIndex = safFiles.indexOfFirst { f.name == it.name }
            if(saffIndex != -1) {
                val saff = safFiles[saffIndex]

                //注：没考虑符号链接或者documentFile的isVirtual之类的
                // 比较两个文件是否一样
                if((f.isFile.not() && saff.isFile) || (f.isFile && saff.isFile.not()) ) {
                    result.bothAndNotSame.add(
                        SafAndFileDiffPair(
                            safFile = saff,
                            file = f,
                            diffType = SafAndFileDiffType.TYPE
                        )
                    )
                }else if(f.isFile && saff.isFile) {
                    val diffType = if(f.length() != saff.length()) {
                        SafAndFileDiffType.CONTENT
                    }else { //大小一样，逐字节比较
                        var type = SafAndFileDiffType.NONE

                        val fi = f.inputStream()
                        val saffi = contentResolver.openInputStream(saff.uri) ?: throw OpenInputStreamFailed(message = "open InputStream for uri failed: uri = '${saff.uri}'")
                        fi.use { fiit->
                            saffi.use { saffiit->
                                while (true) {
                                    val fb = fiit.read()
                                    if(fb == -1) {
                                        break
                                    }

                                    val saffb = saffiit.read()
                                    if(saffb != fb) {
                                        type = SafAndFileDiffType.CONTENT
                                        break
                                    }
                                }
                            }
                        }

                        type
                    }

                    //不一样就添加，一样就算了
                    if(diffType != SafAndFileDiffType.NONE) {
                        result.bothAndNotSame.add(
                            SafAndFileDiffPair(
                                safFile = saff,
                                file = f,
                                diffType = diffType
                            )
                        )
                    }  // else files are the same
                }else if(f.isDirectory && saff.isDirectory) {
                    recursiveCompareFiles_Saf(contentResolver, saff.listFiles() ?: arrayOf(), f.listFiles() ?: arrayOf(), result, canceled)
                }

                safFiles.removeAt(saffIndex)
            }else {  //no matched only f in files
                result.onlyInFiles.add(f)
            }
        }

        result.onlyInSaf.addAll(safFiles)
    }

}
