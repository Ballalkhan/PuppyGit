package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class DiffSettings (
    /**
    if ture, will show delLine and addLine closer, else, maybe will split
     * e.g.
     * if true, show:
     * -1 abc1
     * +1 abc2
     * -2 def3
     * +2 def4
     *
     * if false, show:
     * -1 abc1
     * -2 def3
     * +1 abc2
     * +2 def4
     */
    var groupDiffContentByLineNum:Boolean = true,

    var diffContentSizeMaxLimit:Long = 0L,  // 0=no limit, unit is Byte, e.g. 1MB should set to 1000000L,

    /**
     * load how many lines check once abort signal
     */
    var loadDiffContentCheckAbortSignalLines:Int=1000,
    /**
     * load how much size will check once abort signal, when this or `loadDiffContentCheckAbortSignalFrequency` reached will check abort signal
     */
    var loadDiffContentCheckAbortSignalSize:Long=1000000L,  // unit byte, default 1MB


    var showLineNum:Boolean=true,
    var showOriginType:Boolean=false,
    var fontSize:Int = SettingsCons.defaultFontSize,  //字体大小，单位sp
    var lineNumFontSize:Int = SettingsCons.defaultLineNumFontSize,  //行号字体大小

    var enableBetterButSlowCompare:Boolean=false,

    /**
     * matching lines by words
     * 按单词比较行
     */
    var matchByWords:Boolean=false,

    /**
     * enable select line for compare
     * 启用选择行以比较，可以自定义比较两个行号不同的行，显示它们的区别
     */
    var enableSelectCompare:Boolean = true,


    /**
     * if enable, when edit line, before saving, will create snapshot for origin file first
     * 若启用，编辑行后，保存之前，会先为原始文件创建快照
     */
    var createSnapShotForOriginFileBeforeSave:Boolean = false
)
