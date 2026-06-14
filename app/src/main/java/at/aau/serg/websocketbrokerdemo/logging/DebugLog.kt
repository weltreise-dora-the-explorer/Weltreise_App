package at.aau.serg.websocketbrokerdemo.logging

import android.util.Log
import com.example.myapplication.BuildConfig

/**
 * Prevents diagnostic messages and their data from being evaluated in release builds.
 */
object DebugLog {
    fun d(tag: String, message: () -> String) {
        if (BuildConfig.DEBUG) Log.d(tag, message())
    }

    fun i(tag: String, message: () -> String) {
        if (BuildConfig.DEBUG) Log.i(tag, message())
    }

    fun w(tag: String, throwable: Throwable? = null, message: () -> String) {
        if (!BuildConfig.DEBUG) return
        if (throwable == null) Log.w(tag, message()) else Log.w(tag, message(), throwable)
    }

    fun e(tag: String, throwable: Throwable? = null, message: () -> String) {
        if (!BuildConfig.DEBUG) return
        if (throwable == null) Log.e(tag, message()) else Log.e(tag, message(), throwable)
    }
}
