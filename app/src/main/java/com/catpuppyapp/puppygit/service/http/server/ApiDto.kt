package com.catpuppyapp.puppygit.service.http.server

import kotlinx.serialization.Serializable

@Serializable
data class ApiDto (
    val protocol:String = "http",
    val host:String = "127.0.0.1",
    val port:Int = 52520,
    val token:String="",
    val pull:String = "/pull",
    val push:String = "/push",
    //少加点参数，少写少错
    val pull_example:String = "",
    val push_example:String = "",
)
