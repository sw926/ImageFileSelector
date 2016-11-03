package com.sw926.imagefileselector

import android.util.Log
import java.util.*


/**
 * Wrapper API for sending log output.
 */
internal object AppLogger {

    var DEBUG = false
    val TAG = "music_more_fun"
    val TIMER_TAG = "TraceTime"

    /**
     * Send a VERBOSE log message.

     * @param msg The message you would like logged.
     */
    fun v(msg: String) {
        if (DEBUG)
            Log.v(TAG, buildMessage(msg))
    }

    /**
     * Send a VERBOSE log message and log the exception.

     * @param msg The message you would like logged.
     * *
     * @param thr An exception to log
     */
    fun v(msg: String, thr: Throwable) {
        if (DEBUG)
            Log.v(TAG, buildMessage(msg), thr)
    }

    /**
     * Send a DEBUG log message.

     * @param msg The message you would like logged.
     */
    fun d(msg: String) {
        if (DEBUG)
            Log.d(TAG, buildMessage(msg))
    }

    fun d(tag: String, msg: String) {
        if (DEBUG)
            Log.d(tag, msg)
        //            android.util.Log.d(tag, buildMessage(msg));
    }

    /**
     * Send a DEBUG log message and log the exception.

     * @param msg The message you would like logged.
     * *
     * @param thr An exception to log
     */
    fun d(msg: String, thr: Throwable) {
        if (DEBUG)
            Log.d(TAG, buildMessage(msg), thr)
    }

    /**
     * Send an INFO log message.

     * @param msg The message you would like logged.
     */
    fun i(msg: String) {
        if (DEBUG)
            Log.i(TAG, buildMessage(msg))
    }

    fun i(tag: String, msg: String) {
        if (DEBUG)
            Log.i(tag, msg)
        //            Log.i(tag, buildMessage(msg));
    }

    /**
     * Send a INFO log message and log the exception.

     * @param msg The message you would like logged.
     * *
     * @param thr An exception to log
     */
    fun i(msg: String, thr: Throwable) {
        if (DEBUG)
            Log.i(TAG, buildMessage(msg), thr)
    }

    /**
     * Send an ERROR log message.

     * @param msg The message you would like logged.
     */
    fun e(msg: String) {
        if (DEBUG)
            Log.e(TAG, buildMessage(msg))
    }

    fun e(tag: String, msg: String) {
        if (DEBUG)
            Log.e(tag, msg)
        //            android.util.Log.e(tag, buildMessage(msg));
    }

    /**
     * Send a WARN log message

     * @param msg The message you would like logged.
     */
    fun w(msg: String) {
        if (DEBUG)
            Log.w(TAG, buildMessage(msg))
    }

    fun w(tag: String, msg: String) {
        if (DEBUG)
            Log.w(tag, buildMessage(msg))
    }

    /**
     * Send a WARN log message and log the exception.

     * @param msg The message you would like logged.
     * *
     * @param thr An exception to log
     */
    fun w(msg: String, thr: Throwable) {
        if (DEBUG)
            Log.w(TAG, buildMessage(msg), thr)
    }

    /**
     * Send an empty WARN log message and log the exception.

     * @param thr An exception to log
     */
    fun w(thr: Throwable) {
        if (DEBUG)
            Log.w(TAG, buildMessage(""), thr)
    }

    /**
     * Send an ERROR log message and log the exception.

     * @param msg The message you would like logged.
     * *
     * @param thr An exception to log
     */
    fun e(msg: String, thr: Throwable) {
        if (DEBUG)
            Log.e(TAG, buildMessage(msg), thr)
    }

    fun e(tag: String, msg: String, thr: Throwable) {
        if (DEBUG)
            Log.e(tag, buildMessage(msg), thr)
    }

    fun printStackTrace(e: Throwable) {
        if (DEBUG)
            e.printStackTrace()
    }

    private val traceTimeStack = Stack<Long>()

    fun resetTraceTime() {
        traceTimeStack.clear()
    }

    fun startTraceTime(msg: String) {
        traceTimeStack.push(System.currentTimeMillis())
        if (DEBUG) {
            Log.d(TIMER_TAG, msg + " time = " + System.currentTimeMillis())
        }
    }

    fun stopTraceTime(msg: String) {
        if (!traceTimeStack.isEmpty()) {
            val time = traceTimeStack.pop()
            val diff = System.currentTimeMillis() - time
            if (DEBUG) {
                Log.d(TIMER_TAG, "[" + diff + "]" + msg + " time = " + System.currentTimeMillis())
            }
        }
    }

    /**
     * Building Message

     * @param msg The message you would like logged.
     * *
     * @return Message String
     */
    fun buildMessage(msg: String): String {
        val caller = java.lang.Throwable().stackTrace[2]
        return StringBuilder().append(caller.className).append(".").append(caller.methodName).append("(): \n").append(msg).toString()
    }
}
