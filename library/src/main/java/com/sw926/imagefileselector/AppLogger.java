package com.sw926.imagefileselector;

/**
 * User: Jiang Qi
 * Date: 12-7-31
 */

import android.util.Log;

import java.util.Stack;


/**
 * Wrapper API for sending log output.
 */
public class AppLogger {

    static boolean DEBUG = false;
    protected static final String TAG = "music_more_fun";
    protected static final String TIMER_TAG = "TraceTime";

    private AppLogger() {
    }

    /**
     * Send a VERBOSE log message.
     *
     * @param msg The message you would like logged.
     */
    public static void v(String msg) {
        if (DEBUG)
            Log.v(TAG, buildMessage(msg));
    }

    /**
     * Send a VERBOSE log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void v(String msg, Throwable thr) {
        if (DEBUG)
            Log.v(TAG, buildMessage(msg), thr);
    }

    /**
     * Send a DEBUG log message.
     *
     * @param msg The message you would like logged.
     */
    public static void d(String msg) {
        if (DEBUG)
            Log.d(TAG, buildMessage(msg));
    }

    public static void d(String tag, String msg) {
        if (DEBUG)
            Log.d(tag, msg);
//            android.util.Log.d(tag, buildMessage(msg));
    }

    /**
     * Send a DEBUG log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void d(String msg, Throwable thr) {
        if (DEBUG)
            Log.d(TAG, buildMessage(msg), thr);
    }

    /**
     * Send an INFO log message.
     *
     * @param msg The message you would like logged.
     */
    public static void i(String msg) {
        if (DEBUG)
            Log.i(TAG, buildMessage(msg));
    }

    public static void i(String tag, String msg) {
        if (DEBUG)
            Log.i(tag, msg);
//            Log.i(tag, buildMessage(msg));
    }

    /**
     * Send a INFO log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void i(String msg, Throwable thr) {
        if (DEBUG)
            Log.i(TAG, buildMessage(msg), thr);
    }

    /**
     * Send an ERROR log message.
     *
     * @param msg The message you would like logged.
     */
    public static void e(String msg) {
        if (DEBUG)
            Log.e(TAG, buildMessage(msg));
    }

    public static void e(String tag, String msg) {
        if (DEBUG)
            Log.e(tag, msg);
//            android.util.Log.e(tag, buildMessage(msg));
    }

    /**
     * Send a WARN log message
     *
     * @param msg The message you would like logged.
     */
    public static void w(String msg) {
        if (DEBUG)
            Log.w(TAG, buildMessage(msg));
    }

    public static void w(String tag, String msg) {
        if (DEBUG)
            Log.w(tag, buildMessage(msg));
    }

    /**
     * Send a WARN log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void w(String msg, Throwable thr) {
        if (DEBUG)
            Log.w(TAG, buildMessage(msg), thr);
    }

    /**
     * Send an empty WARN log message and log the exception.
     *
     * @param thr An exception to log
     */
    public static void w(Throwable thr) {
        if (DEBUG)
            Log.w(TAG, buildMessage(""), thr);
    }

    /**
     * Send an ERROR log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param thr An exception to log
     */
    public static void e(String msg, Throwable thr) {
        if (DEBUG)
            Log.e(TAG, buildMessage(msg), thr);
    }

    public static void e(String tag, String msg, Throwable thr) {
        if (DEBUG)
            Log.e(tag, buildMessage(msg), thr);
    }

    public static void printStackTrace(Exception e) {
        if (DEBUG)
            e.printStackTrace();
    }

    private static final Stack<Long> traceTimeStack = new Stack<Long>();

    public static void resetTraceTime() {
        traceTimeStack.clear();
    }

    public static void startTraceTime(String msg) {
        traceTimeStack.push(System.currentTimeMillis());
        if (DEBUG) {
            Log.d(TIMER_TAG, msg + " time = " + System.currentTimeMillis());
        }
    }

    public static void stopTraceTime(String msg) {
        if (!traceTimeStack.isEmpty()) {
            long time = traceTimeStack.pop();
            long diff = System.currentTimeMillis() - time;
            if (DEBUG) {
                Log.d(TIMER_TAG, "[" + diff + "]" + msg + " time = " + System.currentTimeMillis());
            }
        }
    }

    /**
     * Building Message
     *
     * @param msg The message you would like logged.
     * @return Message String
     */
    protected static String buildMessage(String msg) {
        StackTraceElement caller = new Throwable().fillInStackTrace().getStackTrace()[2];
        return caller.getClassName() + "." + caller.getMethodName() + "(): \n" + msg;
    }
}
