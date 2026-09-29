package com.gamesettings.app

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * ذخیره و بازیابی تنظیمات کاربر: زبان برنامه، فونت و قدرت سیستم.
 * از SharedPreferences استفاده می‌کند تا انتخاب کاربر بین بازکردن‌های مختلف اپ باقی بماند.
 * (تم برنامه همیشه تیره است — تم روشن به‌طور کامل حذف شده.)
 */
object AppPreferences {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_FONT = "app_font"
    private const val KEY_SYSTEM_TIER = "system_tier"
    private const val KEY_HAS_LOADED_GAMES = "has_loaded_games"

    const val LANG_FA = "fa"
    const val LANG_EN = "en"

    const val TIER_WEAK = "weak"
    const val TIER_MEDIUM = "medium"
    const val TIER_STRONG = "strong"

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

    /** آیا کاربر مراحل ورود اولیه (زبان → قدرت سیستم) را قبلاً طی کرده؟ */
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

    /**
     * قدرت سیستمِ (کامپیوتر) کاربر — نه گوشی. کاربر یک‌بار در تنظیمات انتخاب می‌کند
     * و برنامه بر همین اساس، در صفحه‌ی هر بازی، بخش سبز یا زرد را به‌عنوان
     * «توصیه‌شده برای سیستم شما» علامت می‌زند. مقدار خالی یعنی هنوز انتخاب نکرده،
     * که در این حالت هیچ توصیه‌ای نمایش داده نمی‌شود.
     */
    fun getSystemTier(context: Context): String =
        prefs(context).getString(KEY_SYSTEM_TIER, "") ?: ""

    fun setSystemTier(context: Context, tier: String) {
        prefs(context).edit().putString(KEY_SYSTEM_TIER, tier).apply()
    }

    /**
     * آیا لیست بازی‌ها حداقل یک‌بار با موفقیت از سرور گرفته شده؟ فقط وقتی
     * true می‌شه که یک fetch از سرور واقعاً موفق باشه (نه صرفاً تلاش بشه) —
     * تا اگه اولین‌بار اینترنت نبود، دفعه‌ی بعد دوباره تلاش خودکار بشه.
     */
    fun hasEverLoadedGames(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAS_LOADED_GAMES, false)

    fun markGamesLoaded(context: Context) {
        prefs(context).edit().putBoolean(KEY_HAS_LOADED_GAMES, true).apply()
    }
}
