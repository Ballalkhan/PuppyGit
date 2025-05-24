package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.style.MyStyleKt

@Composable
fun <T> TitleDropDownMenu(
    dropDownMenuExpendState: MutableState<Boolean>,
    curSelectedItem:T,
    itemList: List<T>,
    isItemSelected:(T)->Boolean,
    titleClickEnabled:Boolean,
    showHideMenuIconContentDescription:String,  // 这个可能是为视力不好的人设置的语音提示文字
    menuItemFormatter:(T)->String,
    titleFirstLineFormatter:(T)->String,
    titleSecondLineFormatter:(T)->String,
    titleOnLongClick:(T)->Unit,
    itemOnClick: (T)->Unit
) {
    TitleDropDownMenu(
        dropDownMenuExpendState = dropDownMenuExpendState,
        curSelectedItem = curSelectedItem,
        itemList = itemList,
        titleClickEnabled = titleClickEnabled,
        titleFirstLine={
            Text(
                text = titleFirstLineFormatter(curSelectedItem),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = MyStyleKt.Title.firstLineFontSize,
            )
        },
        titleSecondLine={
            Text(
                //  判断仓库是否处于detached，然后显示在这里(例如： "abc1234(detached)" )
                // "main|StateT" or "main", eg, when merging show: "main|Merging", when 仓库状态正常时 show: "main"；如果是detached HEAD状态，则显示“提交号(Detached)|状态“，例如：abc2344(Detached) 或 abc2344(Detached)|Merging
                text = titleSecondLineFormatter(curSelectedItem),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = MyStyleKt.Title.secondLineFontSize,
            )
        },
        titleRightIcon = {
            Icon(
                imageVector = if (dropDownMenuExpendState.value) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowLeft,
                contentDescription = showHideMenuIconContentDescription,
            )
        },
        menuItem = {
            DropDownMenuItemText(
                text = menuItemFormatter(it),
                selected = isItemSelected(it)
            )
        },
        titleOnLongClick = titleOnLongClick,
        itemOnClick = itemOnClick,
    )
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> TitleDropDownMenu(
    dropDownMenuExpendState: MutableState<Boolean>,
    curSelectedItem:T,
    itemList: List<T>,

    // 这个是长按和点按共同的enabled
    titleClickEnabled:Boolean,

    switchDropDownMenuShowHide:()->Unit = {
        dropDownMenuExpendState.value = !dropDownMenuExpendState.value
    },
    closeDropDownMenu:()->Unit  = {
        dropDownMenuExpendState.value = false
    },
    titleFirstLine:@Composable (T)->Unit,
    titleSecondLine:@Composable (T)->Unit,
    titleRightIcon:@Composable (T)->Unit,  // icon at the title text right
    menuItem:@Composable (T)->Unit,
    titleOnLongClick:(T)->Unit,

    //展开的菜单的条目的onClick
    itemOnClick: (T)->Unit,

    titleOnClick: ()->Unit = { switchDropDownMenuShowHide() },  //切换下拉菜单显示隐藏
    showExpandIcon: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    //最多占屏幕宽度一半
    val itemWidth = (LocalConfiguration.current.screenWidthDp / 2).dp

    val iconWidth = 30.dp
    val textWidth = if(showExpandIcon) itemWidth - iconWidth else itemWidth

    Box(
        modifier = Modifier
            .width(itemWidth)
            .combinedClickable(
                enabled = titleClickEnabled,
                onLongClick = {  //长按显示仓库名和分支名
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    titleOnLongClick(curSelectedItem)
                }
            ) { // onClick
                titleOnClick()
            },
    ) {
        Column(
            modifier = Modifier
                .width(textWidth)
                .align(Alignment.CenterStart)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                //限制下宽度，不然仓库名太长就看不到箭头按钮了，用户可能就不知道能点击仓库名切换仓库了
            ) {
                titleFirstLine(curSelectedItem)
            }

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
            ) {
                titleSecondLine(curSelectedItem)
            }
        }

        if(showExpandIcon) {
            Column(
                modifier = Modifier
                    .width(iconWidth)
                    .align(Alignment.CenterEnd)
            ) {
                titleRightIcon(curSelectedItem)
            }
        }
    }

    //下拉菜单
    DropdownMenu(
        expanded = dropDownMenuExpendState.value,
        onDismissRequest = { closeDropDownMenu() }
    ) {
        for (i in itemList.toList()) {
            //列出条目
            DropdownMenuItem(
                modifier = Modifier.width(itemWidth),
                text = { menuItem(i) },
                onClick = {
                    itemOnClick(i)
                    closeDropDownMenu()
                }
            )
        }
    }
}
