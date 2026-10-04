package com.gamesettings.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import kotlin.math.hypot
import kotlin.math.max

/**
 * مدیریت ظاهر (تم) برنامه: روشن / تیره / خودکار.
 *
 * تغییر تم: انتخاب کاربر ذخیره می‌شود، حالت شب AppCompat عوض می‌شود (که صفحه‌ها را دوباره می‌سازد)
 * و برای اینکه تغییر ناگهانی و چشمک‌دار نباشد، قبلش از صفحه عکس می‌گیریم و روی صفحه‌ی تازه‌ساخته‌شده
 * یک «موج» دایره‌ای از نقطه‌ی لمس پخش می‌کنیم که ظاهر جدید را آشکار می‌کند.
 * اگر هر مرحله‌ی این انیمیشن خطا بدهد، فقط انیمیشن حذف می‌شود و برنامه عادی ادامه می‌دهد.
 */
object ThemeManager {

    private fun modeFor(theme: String): Int = when (theme) {
        AppPreferences.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        AppPreferences.THEME_SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        else -> AppCompatDelegate.MODE_NIGHT_YES
    }

    /** اعمال تم ذخیره‌شده — از PcMaxApp و پیش از ساخته‌شدن اولین صفحه صدا زده می‌شود */
    fun applySaved(context: Context) {
        AppCompatDelegate.setDefaultNightMode(modeFor(AppPreferences.getTheme(context)))
    }

    fun isNight(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    /** آیکون‌های نوار وضعیت و ناوبری: روی تم روشن تیره، روی تم تیره روشن */
    fun applySystemBars(activity: Activity) {
        try {
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            val light = !isNight(activity)
            controller.isAppearanceLightStatusBars = light
            controller.isAppearanceLightNavigationBars = light
        } catch (e: Exception) {
            // مهم نیست؛ فقط رنگ آیکون‌های نوار وضعیت است
        }
    }

    // ───────────────────────── تغییر تم با انیمیشن موج ─────────────────────────

    private var pendingSnapshot: Bitmap? = null
    private var pendingTarget: Class<*>? = null
    private var pendingX = 0f
    private var pendingY = 0f
    private var pendingAt = 0L

    /**
     * عوض‌کردن تم. [originX] و [originY] مختصات صفحه‌ی نقطه‌ای هستند که موج از آن شروع می‌شود
     * (معمولاً مرکز کارتی که کاربر لمس کرده).
     */
    fun changeTheme(activity: Activity, theme: String, originX: Int, originY: Int) {
        if (AppPreferences.getTheme(activity) == theme) return
        captureSnapshot(activity, originX, originY)
        AppPreferences.setTheme(activity, theme)
        AppCompatDelegate.setDefaultNightMode(modeFor(theme))
        // اگر ظاهر واقعی عوض نشد (مثلاً «تیره» → «خودکار» روی گوشی با حالت شب)، صفحه دوباره ساخته نمی‌شود؛
        // عکسِ بلااستفاده را رها کن تا حافظه اشغال نماند.
        Handler(Looper.getMainLooper()).postDelayed({
            if (pendingSnapshot != null && SystemClock.elapsedRealtime() - pendingAt >= 1500L) releaseSnapshot()
        }, 1700L)
    }

    private fun captureSnapshot(activity: Activity, x: Int, y: Int) {
        releaseSnapshot()
        try {
            val decor = activity.window.decorView
            if (decor.width <= 0 || decor.height <= 0) return
            val bitmap = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
            decor.draw(Canvas(bitmap))
            pendingSnapshot = bitmap
            pendingTarget = activity.javaClass
            pendingX = x.toFloat()
            pendingY = y.toFloat()
            pendingAt = SystemClock.elapsedRealtime()
        } catch (t: Throwable) {
            pendingSnapshot = null
            pendingTarget = null
        }
    }

    private fun releaseSnapshot() {
        pendingSnapshot?.recycle()
        pendingSnapshot = null
        pendingTarget = null
    }

    /** از BaseActivity صدا زده می‌شود؛ فقط صفحه‌ای که تم را عوض کرده انیمیشن را اجرا می‌کند */
    fun playPendingReveal(activity: Activity) {
        val bitmap = pendingSnapshot ?: return
        if (pendingTarget != activity.javaClass) return
        if (SystemClock.elapsedRealtime() - pendingAt > 2500L || Motion.reduced(activity)) {
            releaseSnapshot()
            return
        }
        val cx = pendingX
        val cy = pendingY
        pendingSnapshot = null
        pendingTarget = null
        try {
            val decor = activity.window.decorView as ViewGroup
            val overlay = RevealOverlay(activity, bitmap, cx, cy)
            decor.addView(
                overlay,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            val maxRadius = hypot(
                max(cx, bitmap.width - cx),
                max(cy, bitmap.height - cy)
            )
            ValueAnimator.ofFloat(0f, maxRadius).apply {
                duration = 560L
                interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
                addUpdateListener { overlay.radius = it.animatedValue as Float }
                addListener(object : AnimatorListenerAdapter() {
                    private fun finish() {
                        try {
                            decor.removeView(overlay)
                        } catch (e: Exception) {
                        }
                        bitmap.recycle()
                    }

                    override fun onAnimationEnd(animation: Animator) = finish()
                    override fun onAnimationCancel(animation: Animator) = finish()
                })
                start()
            }
        } catch (t: Throwable) {
            bitmap.recycle()
        }
    }

    /** لایه‌ی روی صفحه: عکسِ ظاهر قبلی که یک «سوراخِ» دایره‌ای در آن بزرگ می‌شود و ظاهر جدید را نشان می‌دهد */
    private class RevealOverlay(
        context: Context,
        private val bitmap: Bitmap,
        private val cx: Float,
        private val cy: Float
    ) : View(context) {

        var radius: Float = 0f
            set(value) {
                field = value
                invalidate()
            }

        private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        private val dst = Rect()

        override fun onDraw(canvas: Canvas) {
            dst.set(0, 0, width, height)
            val saved = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            canvas.drawBitmap(bitmap, null, dst, null)
            canvas.drawCircle(cx, cy, radius, clearPaint)
            canvas.restoreToCount(saved)
        }

        // تا پایان انیمیشن لمس‌ها را نگیر که کاربر روی صفحه‌ی نیمه‌کاره چیزی را نزند
        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_UP) performClick()
            return true
        }

        override fun performClick(): Boolean = super.performClick()
    }

    // ───────────────────────── کارت‌های انتخاب تم (ورود اولیه و تنظیمات) ─────────────────────────

    /**
     * کارت‌های «روشن / تیره / خودکار» را به لیوت view_theme_tiles وصل می‌کند.
     * [onPick] با تمِ انتخاب‌شده و خودِ کارت صدا زده می‌شود. ظاهر انتخاب‌شده هم همان لحظه نشان داده می‌شود.
     */
    fun bindTiles(root: View, selected: String, onPick: (theme: String, tile: View) -> Unit) {
        val tiles = linkedMapOf(
            AppPreferences.THEME_LIGHT to root.findViewById<View>(R.id.tile_theme_light),
            AppPreferences.THEME_DARK to root.findViewById<View>(R.id.tile_theme_dark),
            AppPreferences.THEME_SYSTEM to root.findViewById<View>(R.id.tile_theme_system)
        )
        val checks = mapOf(
            AppPreferences.THEME_LIGHT to root.findViewById<View>(R.id.check_theme_light),
            AppPreferences.THEME_DARK to root.findViewById<View>(R.id.check_theme_dark),
            AppPreferences.THEME_SYSTEM to root.findViewById<View>(R.id.check_theme_system)
        )
        val previews = mapOf(
            AppPreferences.THEME_LIGHT to root.findViewById<View>(R.id.preview_theme_light),
            AppPreferences.THEME_DARK to root.findViewById<View>(R.id.preview_theme_dark),
            AppPreferences.THEME_SYSTEM to root.findViewById<View>(R.id.preview_theme_system)
        )

        fun render(current: String, animate: Boolean) {
            for ((key, tile) in tiles) {
                val isSelected = key == current
                tile.setBackgroundResource(
                    if (isSelected) R.drawable.bg_selectable_card_selected else R.drawable.bg_selectable_card
                )
                checks[key]?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
                val preview = previews[key] ?: continue
                if (isSelected && animate && !Motion.reduced(root.context)) {
                    preview.animate().cancel()
                    preview.scaleX = 0.92f
                    preview.scaleY = 0.92f
                    preview.animate().scaleX(1f).scaleY(1f).setDuration(380)
                        .setInterpolator(android.view.animation.OvershootInterpolator(2.2f)).start()
                } else {
                    preview.scaleX = 1f
                    preview.scaleY = 1f
                }
            }
        }

        render(selected, animate = false)
        for ((key, tile) in tiles) {
            tile.setOnClickListener {
                render(key, animate = true)
                onPick(key, tile)
            }
        }
    }
}
