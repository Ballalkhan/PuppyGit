package com.catpuppyapp.puppygit.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.git.IgnoreItem
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.github.git24j.core.Repository
import java.io.File


@Composable
fun GitIgnoreDialog(
    showIgnoreDialog: MutableState<Boolean>,
    loadingOn: (String) -> Unit,
    loadingOff: () -> Unit,
    activityContext: Context,
    getIgnoreItems: ()->List<IgnoreItem>,
    getRepository: () -> Repository?,
    onSuccess: suspend () -> Unit = { Msg.requireShow(activityContext.getString(R.string.success)) },
    onCatch: suspend (Exception)->Unit,
    onFinally: suspend ()->Unit,
) {
    ConfirmDialog(
        title = stringResource(R.string.ignore),
        text = stringResource(R.string.will_ignore_selected_files_are_you_sure),
        okTextColor = MyStyleKt.TextColor.danger(),  // 因为remove from git标红了，而ignore包含remove from git 操作，所以逻辑上也该标红，而且这些操作恢复起来比较麻烦，所以最好高亮确认，暗示用户谨慎操作
        onCancel = { showIgnoreDialog.value = false }
    ) {
        showIgnoreDialog.value = false

        doJobThenOffLoading(loadingOn, loadingOff, activityContext.getString(R.string.loading)) {
            try {
                val items = getIgnoreItems()

                if (items.isEmpty()) {
                    return@doJobThenOffLoading
                }

                val repository = getRepository()

                if(repository == null) {
                    return@doJobThenOffLoading
                }

                repository.use { repo ->
                    val repoIndex = repo.index()

                    //拼接 "\npath1\npath2\npath3...省略....\n"
                    val linesWillIgnore = Cons.lineBreak +
                            (items.map {
                                // remove from git
                                Libgit2Helper.removeFromGit(it.pathspec, repoIndex, it.isFile)

                                //返回将添加到 .gitignore 的path
                                //相对路径前加/会从仓库根目录开始匹配，若不加杠，会匹配子目录，容易误匹配
                                if(it.pathspec.startsWith(Cons.slashChar)) it.pathspec else (Cons.slash + it.pathspec)
                            }.joinToString(Cons.lineBreak)) +
                            Cons.lineBreak
                    ;

                    val ignoreFile = File(Libgit2Helper.getRepoIgnoreFilePathNoEndsWithSlash(repo, createIfNonExists = true))
                    // 追加路径到仓库workdir下的 .gitignore
                    FsUtils.appendTextToFile(ignoreFile, linesWillIgnore)

                    //将repoIndex修改写入硬盘
                    repoIndex.write()
                }

                onSuccess()
            } catch (e: Exception) {
                onCatch(e)
            } finally {
                onFinally()
            }

        }
    }
}
