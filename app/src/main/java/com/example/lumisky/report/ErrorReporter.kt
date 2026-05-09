package com.example.lumisky.report

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.UserManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.core.Logger
import com.example.lumisky.R
import java.io.File
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import kotlin.system.exitProcess

object ErrorReporter {
	private const val TAG = "ErrorReporter"
	private const val DIAGNOSTICS_DIR = "diagnostics"
	private const val LAST_CRASH_FILE = "last_uncaught_exception.txt"
	private const val MAX_CRASH_CHARS = 14_000
	private const val MAX_EXIT_QUERY_RECORDS = 24
	private const val MAX_EXIT_REPORT_RECORDS = 8

	@Volatile
	private var crashCaptureInstalled = false

	fun installCrashCapture(context: Context) {
		if (crashCaptureInstalled) return
		synchronized(this) {
			if (crashCaptureInstalled) return
			val appContext = context.applicationContext
			val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
			Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
				runCatching {
					writeUncaughtException(appContext, thread, throwable)
				}
				if (previousHandler != null) {
					previousHandler.uncaughtException(thread, throwable)
				} else {
					exitProcess(2)
				}
			}
			crashCaptureInstalled = true
		}
	}

	fun sendErrorReport(context: Context) {
		val appContext = context.applicationContext
		val report = buildReport(appContext)
		val supportEmail = appContext.getString(R.string.support_email)
		val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
			data = Uri.parse("mailto:")
			putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
			putExtra(Intent.EXTRA_SUBJECT, report.subject)
			putExtra(Intent.EXTRA_TEXT, report.body)
		}
		if (startChooser(context, mailIntent, report.chooserTitle)) {
			Logger.i(TAG, "error report mail chooser opened")
			return
		}

		val shareIntent = Intent(Intent.ACTION_SEND).apply {
			type = "text/plain"
			putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
			putExtra(Intent.EXTRA_SUBJECT, report.subject)
			putExtra(Intent.EXTRA_TEXT, report.body)
		}
		if (startChooser(context, shareIntent, report.chooserTitle)) {
			Logger.i(TAG, "error report share chooser opened")
			return
		}

		Toast.makeText(
			appContext,
			appContext.getString(R.string.error_report_no_mail_app),
			Toast.LENGTH_LONG
		).show()
		Logger.w(TAG, "error report chooser unavailable")
	}

	private fun startChooser(
		context: Context,
		intent: Intent,
		title: String
	): Boolean {
		val chooser = Intent.createChooser(intent, title)
		if (context !is Activity) {
			chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
		return runCatching {
			context.startActivity(chooser)
		}.isSuccess
	}

	private fun buildReport(context: Context): ErrorReport {
		val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
		val subject = context.getString(
			R.string.error_report_email_subject,
			context.getString(R.string.app_name),
			packageInfo.versionName.orEmpty()
		)
		val body = buildString {
			appendLine(context.getString(R.string.error_report_body_prompt))
			appendLine()
			appendLine("=== App ===")
			appendLine("package=${context.packageName}")
			appendLine("versionName=${packageInfo.versionName.orEmpty()}")
			appendLine("versionCode=${packageInfo.versionCodeCompat()}")
			appendLine("installer=${resolveInstallerPackage(context)}")
			appendLine("debuggable=${context.applicationInfo.isDebuggableCompat()}")
			appendLine()
			appendLine("=== Device ===")
			appendLine("manufacturer=${Build.MANUFACTURER}")
			appendLine("brand=${Build.BRAND}")
			appendLine("model=${Build.MODEL}")
			appendLine("device=${Build.DEVICE}")
			appendLine("android=${Build.VERSION.RELEASE} sdk=${Build.VERSION.SDK_INT}")
			appendLine("abis=${Build.SUPPORTED_ABIS.joinToString()}")
			appendLine()
			appendLine("=== Runtime ===")
			appendLine("reportUtc=${Instant.now()}")
			appendLine("uptimeMs=${SystemClock.elapsedRealtime()}")
			appendLine("locale=${Locale.getDefault().toLanguageTag()}")
			appendLine("timeZone=${TimeZone.getDefault().id}")
			appendLine("userUnlocked=${isUserUnlocked(context)}")
			appendLine()
			appendLine("=== Previous Uncaught Crash ===")
			appendLine(readLastCrash(context) ?: "none")
			appendLine()
			appendLine("=== Recent App Process Exits ===")
			appendLine(readHistoricalProcessExits(context))
			appendLine()
			appendLine("=== Notes ===")
			appendLine(
				"Android release apps cannot read the full system logcat or every past system crash file. " +
					"This report includes app/device metadata, the last uncaught crash captured by Lumisky, " +
					"and Android historical process-exit records when available."
			)
		}
		return ErrorReport(
			subject = subject,
			body = body,
			chooserTitle = context.getString(R.string.error_report_chooser_title)
		)
	}

	private fun writeUncaughtException(
		context: Context,
		thread: Thread,
		throwable: Throwable
	) {
		val diagnosticsDir = File(context.filesDir, DIAGNOSTICS_DIR)
		if (!diagnosticsDir.exists()) {
			diagnosticsDir.mkdirs()
		}
		val output = File(diagnosticsDir, LAST_CRASH_FILE)
		output.writeText(
			buildString {
				appendLine("timestampUtc=${Instant.now()}")
				appendLine("thread=${thread.name}")
				appendLine("exception=${throwable::class.java.name}")
				appendLine()
				appendLine(Log.getStackTraceString(throwable))
			}
		)
	}

	private fun readLastCrash(context: Context): String? {
		val file = File(File(context.filesDir, DIAGNOSTICS_DIR), LAST_CRASH_FILE)
		if (!file.isFile) return null
		return runCatching {
			val text = file.readText()
			if (text.length <= MAX_CRASH_CHARS) text else text.take(MAX_CRASH_CHARS) + "\n<truncated>"
		}.getOrNull()
	}

	private fun readHistoricalProcessExits(context: Context): String {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
			return "not available before Android 11"
		}
		return readHistoricalProcessExitsApi30(context)
	}

	@RequiresApi(Build.VERSION_CODES.R)
	private fun readHistoricalProcessExitsApi30(context: Context): String {
		val activityManager = context.getSystemService(ActivityManager::class.java)
			?: return "activity manager unavailable"
		return runCatching {
			val exits = activityManager.getHistoricalProcessExitReasons(
				context.packageName,
				0,
				MAX_EXIT_QUERY_RECORDS
			)
			if (exits.isEmpty()) {
				"none"
			} else {
				formatHistoricalProcessExits(exits)
			}
		}.getOrElse { error ->
			"unavailable: ${error::class.java.simpleName}: ${error.message.orEmpty()}"
		}
	}

	@RequiresApi(Build.VERSION_CODES.R)
	private fun formatHistoricalProcessExits(
		exits: List<android.app.ApplicationExitInfo>
	): String {
		val reportableExits = exits
			.filter { exit -> exit.isReportableExit() }
			.take(MAX_EXIT_REPORT_RECORDS)
		val hiddenCount = exits.count { exit -> exit.isExpectedNonErrorExit() }
		return when {
			reportableExits.isEmpty() && hiddenCount > 0 -> {
				"none\nhiddenNonErrorExits=$hiddenCount (install, user stop, or normal self-exit records)"
			}
			reportableExits.isEmpty() -> {
				"none"
			}
			else -> {
				buildString {
					append(reportableExits.joinToString(separator = "\n\n") { exit -> exit.formatForReport() })
					if (hiddenCount > 0) {
						appendLine()
						appendLine()
						append("hiddenNonErrorExits=$hiddenCount (install, user stop, or normal self-exit records)")
					}
				}
			}
		}
	}

	@RequiresApi(Build.VERSION_CODES.R)
	private fun android.app.ApplicationExitInfo.formatForReport(): String {
		return buildString {
			appendLine("timestampUtc=${Instant.ofEpochMilli(timestamp)}")
			appendLine("process=$processName")
			appendLine("pid=$pid")
			appendLine("reason=${reasonName()} status=$status")
			appendLine("importance=$importance")
			appendLine("pssKb=$pss rssKb=$rss")
			val description = description.orEmpty().trim()
			if (description.isNotBlank()) {
				appendLine("description=$description")
			}
		}.trimEnd()
	}

	@RequiresApi(Build.VERSION_CODES.R)
	private fun android.app.ApplicationExitInfo.isReportableExit(): Boolean {
		return !isExpectedNonErrorExit()
	}

	@RequiresApi(Build.VERSION_CODES.R)
	private fun android.app.ApplicationExitInfo.isExpectedNonErrorExit(): Boolean {
		val normalizedDescription = description.orEmpty().lowercase(Locale.US)
		if ("installpackageli" in normalizedDescription) return true
		return when (reason) {
			android.app.ApplicationExitInfo.REASON_EXIT_SELF,
			android.app.ApplicationExitInfo.REASON_USER_REQUESTED,
			android.app.ApplicationExitInfo.REASON_USER_STOPPED -> true
			else -> false
		}
	}

	private fun resolveInstallerPackage(context: Context): String {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			runCatching {
				context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
			}.getOrNull()
		} else {
			@Suppress("DEPRECATION")
			context.packageManager.getInstallerPackageName(context.packageName)
		} ?: "unknown"
	}

	private fun isUserUnlocked(context: Context): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			runCatching {
				context.getSystemService(UserManager::class.java)?.isUserUnlocked
			}.getOrNull() ?: true
		} else {
			true
		}
	}

	private fun PackageInfo.versionCodeCompat(): Long {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			longVersionCode
		} else {
			@Suppress("DEPRECATION")
			versionCode.toLong()
		}
	}

	private fun ApplicationInfo.isDebuggableCompat(): Boolean {
		return (flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
	}

	@RequiresApi(Build.VERSION_CODES.R)
	private fun android.app.ApplicationExitInfo.reasonName(): String {
		return when (reason) {
			android.app.ApplicationExitInfo.REASON_ANR -> "ANR"
			android.app.ApplicationExitInfo.REASON_CRASH -> "CRASH"
			android.app.ApplicationExitInfo.REASON_CRASH_NATIVE -> "CRASH_NATIVE"
			android.app.ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "DEPENDENCY_DIED"
			android.app.ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "EXCESSIVE_RESOURCE_USAGE"
			android.app.ApplicationExitInfo.REASON_EXIT_SELF -> "EXIT_SELF"
			android.app.ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "INITIALIZATION_FAILURE"
			android.app.ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW_MEMORY"
			android.app.ApplicationExitInfo.REASON_OTHER -> "OTHER"
			android.app.ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "PERMISSION_CHANGE"
			android.app.ApplicationExitInfo.REASON_SIGNALED -> "SIGNALED"
			android.app.ApplicationExitInfo.REASON_UNKNOWN -> "UNKNOWN"
			android.app.ApplicationExitInfo.REASON_USER_REQUESTED -> "USER_REQUESTED"
			android.app.ApplicationExitInfo.REASON_USER_STOPPED -> "USER_STOPPED"
			else -> "reason_$reason"
		}
	}

	private data class ErrorReport(
		val subject: String,
		val body: String,
		val chooserTitle: String
	)
}
