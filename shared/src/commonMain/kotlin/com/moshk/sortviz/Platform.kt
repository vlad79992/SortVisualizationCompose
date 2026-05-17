package com.moshk.sortviz

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform