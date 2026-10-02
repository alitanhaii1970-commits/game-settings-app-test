package com.gamesettings.app

import android.app.Application

/**
 * کلاس Application: ظاهر (تم) ذخیره‌شده‌ی کاربر را پیش از ساخته‌شدن اولین صفحه اعمال می‌کند،
 * تا برنامه حتی یک فریم هم با تم اشتباه نمایش داده نشود.
 */
class PcMaxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySaved(this)
    }
}
