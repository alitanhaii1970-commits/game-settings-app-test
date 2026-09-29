package com.gamesettings.app

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * ذخیره و بازیابی تنظیمات کاربر: زبان برنامه و تم (تیره/روشن/سیستم).
 * از SharedPreferences استفاده می‌کند تا انتخاب کاربر بین بازکردن‌های مختلف اپ باقی بماند.
 */
object AppPreferences {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_THEME = "theme"
    private const val KEY_GLASS = "glass_effect"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_FONT = "app_font"

    const val LANG_FA = "fa"
    const val LANG_EN = "en"

    const val THEME_DARK = "dark"
    const val THEME_LIGHT = "light"
    const val THEME_SYSTEM = "system"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLanguage(context: Context): String =
        prefs(context).getString(KEY_LANGUAGE, LANG_FA) ?: LANG_FA

    fun setLanguage(context: Context, lang: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, lang).apply()
        applyLanguage(lang)
    }

    fun applyLanguage(lang: String) {
        val locales = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(locales)
    }

    fun getTheme(context: Context): String =
        prefs(context).getString(KEY_THEME, THEME_DARK) ?: THEME_DARK

    fun setTheme(context: Context, theme: String) {
        prefs(context).edit().putString(KEY_THEME, theme).apply()
        applyTheme(theme)
    }

    fun applyTheme(theme: String) {
        val mode = when (theme) {
            THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    /** حالت شیشه‌ای (glassmorphism) — یک لایه‌ی ظاهری اضافه، مستقل از تیره/روشن بودن تم. */
    fun getGlassEffect(context: Context): Boolean =
        prefs(context).getBoolean(KEY_GLASS, false)

    fun setGlassEffect(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_GLASS, enabled).apply()
    }

    /** آیا کاربر مراحل ورود اولیه (زبان → تم → شیشه‌ای) را قبلاً طی کرده؟ */
    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    /** فونت انتخابی برنامه — مقدار پیش‌فرض "system" یعنی فونت خود سیستم. */
    fun getFontId(context: Context): String =
        prefs(context).getString(KEY_FONT, FontManager.SYSTEM_DEFAULT) ?: FontManager.SYSTEM_DEFAULT

    fun setFontId(context: Context, fontId: String) {
        prefs(context).edit().putString(KEY_FONT, fontId).apply()
    }
}
