package com.gamesettings.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * پایه‌ی صفحه‌های برنامه:
 *  • رنگ آیکون‌های نوار وضعیت/ناوبری را بر اساس تم فعلی تنظیم می‌کند؛
 *  • اگر صفحه به‌خاطر عوض‌شدن تم دوباره ساخته شده باشد، انیمیشن «موجِ» تغییر تم را اجرا می‌کند.
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.applySystemBars(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        ThemeManager.playPendingReveal(this)
    }
}
