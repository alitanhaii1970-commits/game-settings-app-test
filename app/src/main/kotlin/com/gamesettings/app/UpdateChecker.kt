package com.gamesettings.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * بررسی و نصب خودکار آپدیت از GitHub Releases.
 *
 * محدودیت مهم اندروید: اپلیکیشن‌های معمولی (خارج از Google Play) اجازه‌ی
 * نصب کاملاً بی‌صدا رو ندارن — این یک محدودیت امنیتی خودِ سیستم‌عامله و
 * هیچ اپی (حتی با روت‌نشده بودن گوشی) نمی‌تونه دورش بزنه مگر برنامه‌ی
 * سیستمی باشه. پس فرایند اینجا این‌طوریه:
 *   ۱. بررسی نسخه‌ی جدید (خودکار)
 *   ۲. دانلود در پس‌زمینه (خودکار)
 *   ۳. باز کردن صفحه‌ی نصب اندروید (فقط یک تپ از کاربر برای «Install» لازمه)
 */
object UpdateChecker {

    private const val RELEASES_API =
        "https://api.github.com/repos/alitanhaii1970-commits/game-settings-app-test/releases/latest"
    private const val APK_FILENAME = "pcmax_update.apk"

    data class UpdateInfo(
        val remoteBuild: Int,
        val downloadUrl: String,
        val sizeBytes: Long
    )

    /** بررسی نسخه‌ی جدید. نتیجه روی Main thread برمی‌گرده. */
    fun check(
        onUpdateAvailable: (UpdateInfo) -> Unit,
        onUpToDate: () -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            val mainHandler = Handler(Looper.getMainLooper())
            try {
                val connection = URL(RELEASES_API).openConnection() as HttpURLConnection
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode != 200) {
                    mainHandler.post { onError("HTTP ${connection.responseCode}") }
                    return@Thread
                }

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val name = json.optString("name", "")
                // فرمت اسم release الان "v1.0.N" هست (N = شماره‌ی build)
                val remoteBuild = Regex("v1\\.0\\.(\\d+)").find(name)
                    ?.groupValues?.get(1)?.toIntOrNull() ?: 0

                var downloadUrl = ""
                var sizeBytes = 0L
                val assets = json.getJSONArray("assets")
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name") == "app-debug.apk") {
                        downloadUrl = asset.getString("browser_download_url")
                        sizeBytes = asset.getLong("size")
                    }
                }

                val localBuild = BuildConfig.VERSION_CODE

                mainHandler.post {
                    if (remoteBuild > localBuild && downloadUrl.isNotBlank()) {
                        onUpdateAvailable(UpdateInfo(remoteBuild, downloadUrl, sizeBytes))
                    } else {
                        onUpToDate()
                    }
                }
            } catch (e: Exception) {
                mainHandler.post { onError(e.message ?: e.javaClass.simpleName) }
            }
        }.start()
    }

    /** دانلود APK در پس‌زمینه با DownloadManager، و باز کردن خودکار صفحه‌ی نصب پس از پایان. */
    fun downloadAndPromptInstall(activityContext: Context, downloadUrl: String) {
        // ⚠️ باگ قبلی: گیرنده‌ی «پایان دانلود» روی خودِ صفحه‌ی تنظیمات ثبت می‌شد. اگه کاربر حین دانلود
        // (که روی اینترنت کند چند دقیقه طول می‌کشه) از تنظیمات بیرون می‌رفت، گیرنده با بسته‌شدن صفحه
        // حذف می‌شد و صفحه‌ی نصب هیچ‌وقت باز نمی‌شد. حالا روی Context کل برنامه ثبت می‌شه.
        val context = activityContext.applicationContext
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
        val destinationFile = File(downloadsDir, APK_FILENAME)
        if (destinationFile.exists()) destinationFile.delete()

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(context.getString(R.string.update_download_title))
            .setDescription(context.getString(R.string.update_download_desc))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destinationFile))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val finishedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (finishedId == downloadId) {
                    try {
                        ctx.unregisterReceiver(this)
                    } catch (ignored: Exception) {
                    }
                    promptInstall(ctx, destinationFile)
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // ⚠️ باگ قبلی: با RECEIVER_NOT_EXPORTED اندروید ۱۳+ پیام «پایان دانلود» (که از سمت
            // سرویس DownloadManager سیستم می‌آید) را به برنامه نمی‌رساند؛ پس صفحه‌ی نصب هیچ‌وقت باز نمی‌شد.
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
    }

    /** باز کردن صفحه‌ی نصب اندروید — تنها قدمی که نمی‌شه خودکارش کرد (محدودیت امنیتی سیستم). */
    private fun promptInstall(context: Context, apkFile: File) {
        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            // اگه نتونستیم مستقیم صفحه‌ی نصب رو باز کنیم، حداقل فایل دانلود شده و کاربر
            // می‌تونه از اعلان دانلود (که سیستم نشون می‌ده) خودش بازش کنه
        }
    }
}
