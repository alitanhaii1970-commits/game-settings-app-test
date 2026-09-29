package com.gamesettings.app

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppPreferences.applyLanguage(AppPreferences.getLanguage(this))
        AppPreferences.applyTheme(AppPreferences.getTheme(this))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val rootView = findViewById<View>(android.R.id.content)
        val backButton: ImageButton = findViewById(R.id.back_button)
        val languageGroup: RadioGroup = findViewById(R.id.language_group)
        val themeGroup: RadioGroup = findViewById(R.id.theme_group)
        val fontGroup: RadioGroup = findViewById(R.id.font_group)
        val glassSwitch: SwitchCompat = findViewById(R.id.glass_switch)
        val languageCard: LinearLayout = findViewById(R.id.language_card)
        val themeCard: LinearLayout = findViewById(R.id.theme_card)
        val glassCard: LinearLayout = findViewById(R.id.glass_card)

        // ظاهر شیشه‌ای روی کارت‌های همین صفحه (در صورت فعال بودن)؛
        // کارت فونت همیشه استایل زرد-شیشه‌ای مخصوص خودش را دارد (در XML ست شده)
        GlassStyler.applyCard(this, languageCard)
        GlassStyler.applyCard(this, themeCard)
        GlassStyler.applyCard(this, glassCard)

        // فونت انتخابی فعلی را روی همین صفحه هم اعمال کن
        FontManager.applyToViewTree(this, rootView)

        rootView.alpha = 0f
        rootView.animate().alpha(1f).setDuration(260).start()

        backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // مقداردهی اولیه بر اساس تنظیمات فعلی کاربر
        when (AppPreferences.getLanguage(this)) {
            AppPreferences.LANG_EN -> languageGroup.check(R.id.lang_en)
            else -> languageGroup.check(R.id.lang_fa)
        }
        when (AppPreferences.getTheme(this)) {
            AppPreferences.THEME_LIGHT -> themeGroup.check(R.id.theme_light)
            AppPreferences.THEME_SYSTEM -> themeGroup.check(R.id.theme_system)
            else -> themeGroup.check(R.id.theme_dark)
        }
        glassSwitch.isChecked = AppPreferences.getGlassEffect(this)

        // ساخت پویا‌ی ردیف‌های انتخاب فونت
        buildFontOptions(fontGroup)
        val currentFontId = AppPreferences.getFontId(this)
        for (i in 0 until fontGroup.childCount) {
            val child = fontGroup.getChildAt(i)
            if (child is RadioButton && child.tag == currentFontId) {
                fontGroup.check(child.id)
            }
        }

        languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val lang = if (checkedId == R.id.lang_en) AppPreferences.LANG_EN else AppPreferences.LANG_FA
            if (lang != AppPreferences.getLanguage(this)) {
                AppPreferences.setLanguage(this, lang)
                recreate()
            }
        }

        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                R.id.theme_light -> AppPreferences.THEME_LIGHT
                R.id.theme_system -> AppPreferences.THEME_SYSTEM
                else -> AppPreferences.THEME_DARK
            }
            AppPreferences.setTheme(this, theme)
        }

        fontGroup.setOnCheckedChangeListener { group, checkedId ->
            val selected = group.findViewById<RadioButton>(checkedId)
            val fontId = selected?.tag as? String ?: FontManager.SYSTEM_DEFAULT
            AppPreferences.setFontId(this, fontId)
            FontManager.applyToViewTree(this, rootView)
        }

        glassSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setGlassEffect(this, isChecked)
            GlassStyler.applyCard(this, languageCard)
            GlassStyler.applyCard(this, themeCard)
            GlassStyler.applyCard(this, glassCard)
        }
    }

    /** ردیف‌های رادیویی انتخاب فونت را بر اساس لیست FontManager.OPTIONS به‌صورت پویا می‌سازد. */
    private fun buildFontOptions(group: RadioGroup) {
        FontManager.OPTIONS.forEachIndexed { index, option ->
            if (index > 0) {
                val dividerParams = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, dpToPx(1))
                dividerParams.marginStart = dpToPx(16)
                val divider = View(this)
                divider.layoutParams = dividerParams
                divider.setBackgroundColor(getColorCompat(R.color.border))
                group.addView(divider)
            }

            val radio = RadioButton(this)
            radio.id = View.generateViewId()
            radio.tag = option.id
            radio.text = option.displayName
            radio.setTextColor(getColorCompat(R.color.text_primary))
            radio.textSize = 15f
            radio.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14))
            radio.buttonTintList = android.content.res.ColorStateList.valueOf(getColorCompat(R.color.tag_yellow))
            radio.layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            group.addView(radio)
        }
    }

    private fun getColorCompat(resId: Int): Int =
        androidx.core.content.ContextCompat.getColor(this, resId)

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
