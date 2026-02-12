package com.example.core

object Logger {
	fun d(tag: String, msg: String) = android.util.Log.d(tag, msg)
	fun e(tag: String, msg: String, tr: Throwable? = null) = android.util.Log.e(tag, msg, tr)
}
