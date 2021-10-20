package com.kyhsgeekcode.fixzip

interface IConsole {
    fun print(s: String?)

    suspend fun readLine(): String?
}