package com.gamesettings.app

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation

/** ابزارهای انیمیشن مشترک: انتقال بین صفحه‌ها (سازگار با راست‌به‌چپ)، ورود نرم و احترام به «کاهش انیمیشن» */
object Motion {

    fun isRtl(context: Context): Boolean =
        context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()

    /** اگر کاربر انیمیشن‌های سیستم را خاموش کرده باشد، ما هم انیمیشن نمی‌سازیم */
    fun reduced(context: Context): Boolean = try {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    } catch (e: Exception) {
        false
    }

    @Suppress("DEPRECATION")
    fun pushForward(activity: Activity) {
        if (isRtl(activity)) activity.overridePendingTransition(R.anim.push_enter_rtl, R.anim.push_exit_rtl)
        else activity.overridePendingTransition(R.anim.push_enter, R.anim.push_exit)
    }

    @Suppress("DEPRECATION")
    fun popBack(activity: Activity) {
        if (isRtl(activity)) activity.overridePendingTransition(R.anim.pop_enter_rtl, R.anim.pop_exit_rtl)
        else activity.overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit)
    }

    /** ورود نرم یک View: محو‌شدن + کمی بالا آمدن */
    fun riseIn(view: View, delay: Long, distanceDp: Int = 14, duration: Long = 420L) {
        if (reduced(view.context)) return
        view.animate().cancel()
        view.alpha = 0f
        view.translationY = dp(view.context, distanceDp).toFloat()
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator(1.8f))
            .start()
    }

    /**
     * انیمیشن صفحه‌های ورود اولیه. [forward] یعنی رفتن به مرحله‌ی بعد؛
     * جهت حرکت در زبان‌های راست‌به‌چپ برعکس می‌شود.
     */
    fun pageAnimation(entering: Boolean, forward: Boolean, rtl: Boolean): Animation {
        val side = if (rtl) -1f else 1f
        val fromX: Float
        val toX: Float
        if (entering) {
            fromX = (if (forward) 0.14f else -0.14f) * side
            toX = 0f
        } else {
            fromX = 0f
            toX = (if (forward) -0.14f else 0.14f) * side
        }
        val set = AnimationSet(true)
        set.addAnimation(
            TranslateAnimation(
                Animation.RELATIVE_TO_SELF, fromX, Animation.RELATIVE_TO_SELF, toX,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f
            )
        )
        set.addAnimation(AlphaAnimation(if (entering) 0f else 1f, if (entering) 1f else 0f))
        set.duration = if (entering) 380L else 240L
        set.interpolator = if (entering) DecelerateInterpolator(1.6f) else AccelerateInterpolator(1.1f)
        return set
    }
}
