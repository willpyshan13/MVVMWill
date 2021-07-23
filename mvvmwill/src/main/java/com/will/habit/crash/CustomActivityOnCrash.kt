/*
 * Copyright 2014-2017 Eduard Ereza Martínez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.will.habit.crash

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.annotation.RestrictTo
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipFile

object CustomActivityOnCrash {
    private const val TAG = "CustomActivityOnCrash"

    //Extras passed to the error activity
    private const val EXTRA_CONFIG = "cat.ereza.customactivityoncrash.EXTRA_CONFIG"
    private const val EXTRA_STACK_TRACE = "cat.ereza.customactivityoncrash.EXTRA_STACK_TRACE"
    private const val EXTRA_ACTIVITY_LOG = "cat.ereza.customactivityoncrash.EXTRA_ACTIVITY_LOG"

    //General constants
    private const val INTENT_ACTION_ERROR_ACTIVITY = "cat.ereza.customactivityoncrash.ERROR"
    private const val INTENT_ACTION_RESTART_ACTIVITY = "cat.ereza.customactivityoncrash.RESTART"
    private const val CAOC_HANDLER_PACKAGE_NAME = "cat.ereza.customactivityoncrash"
    private const val DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os"
    private const val MAX_STACK_TRACE_SIZE = 131071 //128 KB - 1
    private const val MAX_ACTIVITIES_IN_LOG = 50

    //Shared preferences
    private const val SHARED_PREFERENCES_FILE = "custom_activity_on_crash"
    private const val SHARED_PREFERENCES_FIELD_TIMESTAMP = "last_crash_timestamp"

    //Internal variables
    @SuppressLint("StaticFieldLeak") //This is an application-wide component
    private var application: Application? = null
    private var config = CaocConfig()
    private val activityLog: Deque<String> = ArrayDeque(MAX_ACTIVITIES_IN_LOG)
    private var lastActivityCreated = WeakReference<Activity?>(null)
    private var isInBackground = true

    /**
     * Installs CustomActivityOnCrash on the application using the default error activity.
     *
     * @param context Context to use for obtaining the ApplicationContext. Must not be null.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun install(context: Context?) {
        try {
            if (context == null) {
                Log.e(TAG, "Install failed: context is null!")
            } else {
                //INSTALL!
                val oldHandler = Thread.getDefaultUncaughtExceptionHandler()
                if (oldHandler != null && oldHandler.javaClass.name.startsWith(CAOC_HANDLER_PACKAGE_NAME)) {
                    Log.e(TAG, "CustomActivityOnCrash was already installed, doing nothing!")
                } else {
                    if (oldHandler != null && !oldHandler.javaClass.name.startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
                        Log.e(TAG, "IMPORTANT WARNING! You already have an UncaughtExceptionHandler, are you sure this is correct? If you use a custom UncaughtExceptionHandler, you must initialize it AFTER CustomActivityOnCrash! Installing anyway, but your original handler will not be called.")
                    }
                    application = context.applicationContext as Application

                    //We define a default exception handler that does what we want so it can be called from Crashlytics/ACRA
                    Thread.setDefaultUncaughtExceptionHandler(Thread.UncaughtExceptionHandler { thread, throwable ->
                        if (config.isEnabled()) {
                            Log.e(TAG, "App has crashed, executing CustomActivityOnCrash's UncaughtExceptionHandler", throwable)
                            if (hasCrashedInTheLastSeconds(application!!)) {
                                Log.e(TAG, "App already crashed recently, not starting custom error activity because we could enter a restart loop. Are you sure that your app does not crash directly on init?", throwable)
                                if (oldHandler != null) {
                                    oldHandler.uncaughtException(thread, throwable)
                                    return@UncaughtExceptionHandler
                                }
                            } else {
                                setLastCrashTimestamp(application!!, Date().time)
                                var errorActivityClass = config.getErrorActivityClass()
                                if (errorActivityClass == null) {
                                    errorActivityClass = guessErrorActivityClass(application!!)
                                }
                                if (isStackTraceLikelyConflictive(throwable, errorActivityClass)) {
                                    Log.e(TAG, "Your application class or your error activity have crashed, the custom activity will not be launched!")
                                    if (oldHandler != null) {
                                        oldHandler.uncaughtException(thread, throwable)
                                        return@UncaughtExceptionHandler
                                    }
                                } else if (config.getBackgroundMode() == CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM || !isInBackground) {
                                    val intent = Intent(application, errorActivityClass)
                                    val sw = StringWriter()
                                    val pw = PrintWriter(sw)
                                    throwable.printStackTrace(pw)
                                    var stackTraceString = sw.toString()

                                    //Reduce data to 128KB so we don't get a TransactionTooLargeException when sending the intent.
                                    //The limit is 1MB on Android but some devices seem to have it lower.
                                    //See: http://developer.android.com/reference/android/os/TransactionTooLargeException.html
                                    //And: http://stackoverflow.com/questions/11451393/what-to-do-on-transactiontoolargeexception#comment46697371_12809171
                                    if (stackTraceString.length > MAX_STACK_TRACE_SIZE) {
                                        val disclaimer = " [stack trace too large]"
                                        stackTraceString = stackTraceString.substring(0, MAX_STACK_TRACE_SIZE - disclaimer.length) + disclaimer
                                    }
                                    intent.putExtra(EXTRA_STACK_TRACE, stackTraceString)
                                    if (config.isTrackActivities()) {
                                        var activityLogString: String? = ""
                                        while (!activityLog.isEmpty()) {
                                            activityLogString += activityLog.poll()
                                        }
                                        intent.putExtra(EXTRA_ACTIVITY_LOG, activityLogString)
                                    }
                                    if (config.isShowRestartButton() && config.getRestartActivityClass() == null) {
                                        //We can set the restartActivityClass because the app will terminate right now,
                                        //and when relaunched, will be null again by default.
                                        config.setRestartActivityClass(guessRestartActivityClass(application!!))
                                    }
                                    intent.putExtra(EXTRA_CONFIG, config)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    if (config.getEventListener() != null) {
                                        config.getEventListener()!!.onLaunchErrorActivity()
                                    }
                                    application!!.startActivity(intent)
                                } else if (config.getBackgroundMode() == CaocConfig.BACKGROUND_MODE_CRASH) {
                                    if (oldHandler != null) {
                                        oldHandler.uncaughtException(thread, throwable)
                                        return@UncaughtExceptionHandler
                                    }
                                    //If it is null (should not be), we let it continue and kill the process or it will be stuck
                                }
                                //Else (BACKGROUND_MODE_SILENT): do nothing and let the following code kill the process
                            }
                            val lastActivity = lastActivityCreated.get()
                            if (lastActivity != null) {
                                //We finish the activity, this solves a bug which causes infinite recursion.
                                //See: https://github.com/ACRA/acra/issues/42
                                lastActivity.finish()
                                lastActivityCreated.clear()
                            }
                            killCurrentProcess()
                        } else oldHandler?.uncaughtException(thread, throwable)
                    })
                    application!!.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
                        var currentlyStartedActivities = 0
                        var dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                            if (activity.javaClass != config.getErrorActivityClass()) {
                                // Copied from ACRA:
                                // Ignore activityClass because we want the last
                                // application Activity that was started so that we can
                                // explicitly kill it off.
                                lastActivityCreated = WeakReference(activity)
                            }
                            if (config.isTrackActivities()) {
                                activityLog.add("""${dateFormat.format(Date())}: ${activity.javaClass.simpleName} created
""")
                            }
                        }

                        override fun onActivityStarted(activity: Activity) {
                            currentlyStartedActivities++
                            isInBackground = currentlyStartedActivities == 0
                            //Do nothing
                        }

                        override fun onActivityResumed(activity: Activity) {
                            if (config.isTrackActivities()) {
                                activityLog.add("""${dateFormat.format(Date())}: ${activity.javaClass.simpleName} resumed
""")
                            }
                        }

                        override fun onActivityPaused(activity: Activity) {
                            if (config.isTrackActivities()) {
                                activityLog.add("""${dateFormat.format(Date())}: ${activity.javaClass.simpleName} paused
""")
                            }
                        }

                        override fun onActivityStopped(activity: Activity) {
                            //Do nothing
                            currentlyStartedActivities--
                            isInBackground = currentlyStartedActivities == 0
                        }

                        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                            //Do nothing
                        }

                        override fun onActivityDestroyed(activity: Activity) {
                            if (config.isTrackActivities()) {
                                activityLog.add("""${dateFormat.format(Date())}: ${activity.javaClass.simpleName} destroyed
""")
                            }
                        }
                    })
                }
                Log.i(TAG, "CustomActivityOnCrash has been installed.")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "An unknown error occurred while installing CustomActivityOnCrash, it may not have been properly initialized. Please report this as a bug if needed.", t)
        }
    }

    /**
     * Given an Intent, returns the stack trace extra from it.
     *
     * @param intent The Intent. Must not be null.
     * @return The stacktrace, or null if not provided.
     */
    fun getStackTraceFromIntent(intent: Intent): String? {
        return intent.getStringExtra(EXTRA_STACK_TRACE)
    }

    /**
     * Given an Intent, returns the config extra from it.
     *
     * @param intent The Intent. Must not be null.
     * @return The config, or null if not provided.
     */
    @JvmStatic
    fun getConfigFromIntent(intent: Intent): CaocConfig {
        return intent.getSerializableExtra(EXTRA_CONFIG) as CaocConfig
    }

    /**
     * Given an Intent, returns the activity log extra from it.
     *
     * @param intent The Intent. Must not be null.
     * @return The activity log, or null if not provided.
     */
    fun getActivityLogFromIntent(intent: Intent): String? {
        return intent.getStringExtra(EXTRA_ACTIVITY_LOG)
    }

    /**
     * Given an Intent, returns several error details including the stack trace extra from the intent.
     *
     * @param context A valid context. Must not be null.
     * @param intent  The Intent. Must not be null.
     * @return The full error details.
     */
    @JvmStatic
    fun getAllErrorDetailsFromIntent(context: Context, intent: Intent): String {
        //I don't think that this needs localization because it's a development string...
        val currentDate = Date()
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

        //Get build date
        val buildDateAsString = getBuildDateAsString(context, dateFormat)

        //Get app version
        val versionName = getVersionName(context)
        var errorDetails = ""
        errorDetails += "Build version: $versionName \n"
        if (buildDateAsString != null) {
            errorDetails += "Build date: $buildDateAsString \n"
        }
        errorDetails += """Current date: ${dateFormat.format(currentDate)}
"""
        //Added a space between line feeds to fix #18.
        //Ideally, we should not use this method at all... It is only formatted this way because of coupling with the default error activity.
        //We should move it to a method that returns a bean, and let anyone format it as they wish.
        errorDetails += "Device: $deviceModelName \n \n"
        errorDetails += "Stack trace:  \n"
        errorDetails += getStackTraceFromIntent(intent)
        val activityLog = getActivityLogFromIntent(intent)
        if (activityLog != null) {
            errorDetails += "\nUser actions: \n"
            errorDetails += activityLog
        }
        return errorDetails
    }

    /**
     * Given an Intent, restarts the app and launches a startActivity to that intent.
     * The flags NEW_TASK and CLEAR_TASK are set if the Intent does not have them, to ensure
     * the app stack is fully cleared.
     * If an event listener is provided, the restart app event is invoked.
     * Must only be used from your error activity.
     *
     * @param activity The current error activity. Must not be null.
     * @param intent   The Intent. Must not be null.
     * @param config   The config object as obtained by calling getConfigFromIntent.
     */
    fun restartApplicationWithIntent(activity: Activity, intent: Intent, config: CaocConfig) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        if (intent.component != null) {
            //If the class name has been set, we force it to simulate a Launcher launch.
            //If we don't do this, if you restart from the error activity, then press home,
            //and then launch the activity from the launcher, the main activity appears twice on the backstack.
            //This will most likely not have any detrimental effect because if you set the Intent component,
            //if will always be launched regardless of the actions specified here.
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
        }
        if (config.getEventListener() != null) {
            config.getEventListener()!!.onRestartAppFromErrorActivity()
        }
        activity.finish()
        activity.startActivity(intent)
        killCurrentProcess()
    }

    @JvmStatic
    fun restartApplication(activity: Activity, config: CaocConfig) {
        val intent = Intent(activity, config.getRestartActivityClass())
        restartApplicationWithIntent(activity, intent, config)
    }

    /**
     * Closes the app.
     * If an event listener is provided, the close app event is invoked.
     * Must only be used from your error activity.
     *
     * @param activity The current error activity. Must not be null.
     * @param config   The config object as obtained by calling getConfigFromIntent.
     */
    @JvmStatic
    fun closeApplication(activity: Activity, config: CaocConfig) {
        if (config.getEventListener() != null) {
            config.getEventListener()!!.onCloseAppFromErrorActivity()
        }
        activity.finish()
        killCurrentProcess()
    }
    /// INTERNAL METHODS NOT TO BE USED BY THIRD PARTIES
    /**
     * INTERNAL method that returns the current configuration of the library.
     * If you want to check the config, use CaocConfig.Builder.get();
     *
     * @return the current configuration
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun getConfig(): CaocConfig {
        return config
    }

    /**
     * INTERNAL method that sets the configuration of the library.
     * You must not use this, use CaocConfig.Builder.apply()
     *
     * @param config the configuration to use
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun setConfig(config: CaocConfig) {
        CustomActivityOnCrash.config = config
    }

    /**
     * INTERNAL method that checks if the stack trace that just crashed is conflictive. This is true in the following scenarios:
     * - The application has crashed while initializing (handleBindApplication is in the stack)
     * - The error activity has crashed (activityClass is in the stack)
     *
     * @param throwable     The throwable from which the stack trace will be checked
     * @param activityClass The activity class to launch when the app crashes
     * @return true if this stack trace is conflictive and the activity must not be launched, false otherwise
     */
    private fun isStackTraceLikelyConflictive(throwable: Throwable, activityClass: Class<out Activity?>): Boolean {
        var throwable = throwable
        do {
            val stackTrace = throwable.stackTrace
            for (element in stackTrace) {
                if (element.className == "android.app.ActivityThread" && element.methodName == "handleBindApplication" || element.className == activityClass.name) {
                    return true
                }
            }
        } while (throwable.cause.also { throwable = it!! } != null)
        return false
    }

    /**
     * INTERNAL method that returns the build date of the current APK as a string, or null if unable to determine it.
     *
     * @param context    A valid context. Must not be null.
     * @param dateFormat DateFormat to use to convert from Date to String
     * @return The formatted date, or "Unknown" if unable to determine it.
     */
    private fun getBuildDateAsString(context: Context, dateFormat: DateFormat): String? {
        var buildDate: Long
        try {
            val ai = context.packageManager.getApplicationInfo(context.packageName, 0)
            val zf = ZipFile(ai.sourceDir)

            //If this failed, try with the old zip method
            val ze = zf.getEntry("classes.dex")
            buildDate = ze.time
            zf.close()
        } catch (e: Exception) {
            buildDate = 0
        }
        return if (buildDate > 312764400000L) {
            dateFormat.format(Date(buildDate))
        } else {
            null
        }
    }

    /**
     * INTERNAL method that returns the version name of the current app, or null if unable to determine it.
     *
     * @param context A valid context. Must not be null.
     * @return The version name, or "Unknown if unable to determine it.
     */
    private fun getVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * INTERNAL method that returns the device model name with correct capitalization.
     * Taken from: http://stackoverflow.com/a/12707479/1254846
     *
     * @return The device model name (i.e., "LGE Nexus 5")
     */
    private val deviceModelName: String
        private get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

    /**
     * INTERNAL method that capitalizes the first character of a string
     *
     * @param s The string to capitalize
     * @return The capitalized string
     */
    private fun capitalize(s: String?): String {
        if (s == null || s.length == 0) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first).toString() + s.substring(1)
        }
    }

    /**
     * INTERNAL method used to guess which activity must be called from the error activity to restart the app.
     * It will first get activities from the AndroidManifest with intent filter <action android:name="cat.ereza.customactivityoncrash.RESTART"></action>,
     * if it cannot find them, then it will get the default launcher.
     * If there is no default launcher, this returns null.
     *
     * @param context A valid context. Must not be null.
     * @return The guessed restart activity class, or null if no suitable one is found
     */
    private fun guessRestartActivityClass(context: Context): Class<out Activity?>? {
        var resolvedActivityClass: Class<out Activity?>?

        //If action is defined, use that
        resolvedActivityClass = getRestartActivityClassWithIntentFilter(context)

        //Else, get the default launcher activity
        if (resolvedActivityClass == null) {
            resolvedActivityClass = getLauncherActivity(context)
        }
        return resolvedActivityClass
    }

    /**
     * INTERNAL method used to get the first activity with an intent-filter <action android:name="cat.ereza.customactivityoncrash.RESTART"></action>,
     * If there is no activity with that intent filter, this returns null.
     *
     * @param context A valid context. Must not be null.
     * @return A valid activity class, or null if no suitable one is found
     */
    private fun getRestartActivityClassWithIntentFilter(context: Context): Class<out Activity?>? {
        val searchedIntent = Intent().setAction(INTENT_ACTION_RESTART_ACTIVITY).setPackage(context.packageName)
        val resolveInfos = context.packageManager.queryIntentActivities(searchedIntent,
                PackageManager.GET_RESOLVED_FILTER)
        if (resolveInfos != null && resolveInfos.size > 0) {
            val resolveInfo = resolveInfos[0]
            try {
                return Class.forName(resolveInfo.activityInfo.name) as Class<out Activity?>
            } catch (e: ClassNotFoundException) {
                //Should not happen, print it to the log!
                Log.e(TAG, "Failed when resolving the restart activity class via intent filter, stack trace follows!", e)
            }
        }
        return null
    }

    /**
     * INTERNAL method used to get the default launcher activity for the app.
     * If there is no launchable activity, this returns null.
     *
     * @param context A valid context. Must not be null.
     * @return A valid activity class, or null if no suitable one is found
     */
    private fun getLauncherActivity(context: Context): Class<out Activity?>? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            try {
                return Class.forName(intent.component!!.className) as Class<out Activity?>
            } catch (e: ClassNotFoundException) {
                //Should not happen, print it to the log!
                Log.e(TAG, "Failed when resolving the restart activity class via getLaunchIntentForPackage, stack trace follows!", e)
            }
        }
        return null
    }

    /**
     * INTERNAL method used to guess which error activity must be called when the app crashes.
     * It will first get activities from the AndroidManifest with intent filter <action android:name="cat.ereza.customactivityoncrash.ERROR"></action>,
     * if it cannot find them, then it will use the default error activity.
     *
     * @param context A valid context. Must not be null.
     * @return The guessed error activity class, or the default error activity if not found
     */
    private fun guessErrorActivityClass(context: Context): Class<out Activity?> {
        var resolvedActivityClass: Class<out Activity?>

        //If action is defined, use that
        resolvedActivityClass = getErrorActivityClassWithIntentFilter(context)!!

        //Else, get the default error activity
        if (resolvedActivityClass == null) {
            resolvedActivityClass = DefaultErrorActivity::class.java
        }
        return resolvedActivityClass
    }

    /**
     * INTERNAL method used to get the first activity with an intent-filter <action android:name="cat.ereza.customactivityoncrash.ERROR"></action>,
     * If there is no activity with that intent filter, this returns null.
     *
     * @param context A valid context. Must not be null.
     * @return A valid activity class, or null if no suitable one is found
     */
    private fun getErrorActivityClassWithIntentFilter(context: Context): Class<out Activity?>? {
        val searchedIntent = Intent().setAction(INTENT_ACTION_ERROR_ACTIVITY).setPackage(context.packageName)
        val resolveInfos = context.packageManager.queryIntentActivities(searchedIntent,
                PackageManager.GET_RESOLVED_FILTER)
        if (resolveInfos != null && resolveInfos.size > 0) {
            val resolveInfo = resolveInfos[0]
            try {
                return Class.forName(resolveInfo.activityInfo.name) as Class<out Activity?>
            } catch (e: ClassNotFoundException) {
                //Should not happen, print it to the log!
                Log.e(TAG, "Failed when resolving the error activity class via intent filter, stack trace follows!", e)
            }
        }
        return null
    }

    /**
     * INTERNAL method that kills the current process.
     * It is used after restarting or killing the app.
     */
    private fun killCurrentProcess() {
        Process.killProcess(Process.myPid())
        System.exit(10)
    }

    /**
     * INTERNAL method that stores the last crash timestamp
     *
     * @param timestamp The current timestamp.
     */
    @SuppressLint("ApplySharedPref") //This must be done immediately since we are killing the app
    private fun setLastCrashTimestamp(context: Context, timestamp: Long) {
        context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).edit().putLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, timestamp).commit()
    }

    /**
     * INTERNAL method that gets the last crash timestamp
     *
     * @return The last crash timestamp, or -1 if not set.
     */
    private fun getLastCrashTimestamp(context: Context): Long {
        return context.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE).getLong(SHARED_PREFERENCES_FIELD_TIMESTAMP, -1)
    }

    /**
     * INTERNAL method that tells if the app has crashed in the last seconds.
     * This is used to avoid restart loops.
     *
     * @return true if the app has crashed in the last seconds, false otherwise.
     */
    private fun hasCrashedInTheLastSeconds(context: Context): Boolean {
        val lastTimestamp = getLastCrashTimestamp(context)
        val currentTimestamp = Date().time
        return lastTimestamp <= currentTimestamp && currentTimestamp - lastTimestamp < config.getMinTimeBetweenCrashesMs()
    }

    /**
     * Interface to be called when events occur, so they can be reported
     * by the app as, for example, Google Analytics events.
     */
    interface EventListener : Serializable {
        fun onLaunchErrorActivity()
        fun onRestartAppFromErrorActivity()
        fun onCloseAppFromErrorActivity()
    }
}