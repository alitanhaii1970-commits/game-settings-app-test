package com.gamesettings.app

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppPreferences.applyLanguage(AppPreferences.getLanguage(this))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val rootView = findViewById<View>(android.R.id.content)
        val backButton: ImageButton = findViewById(R.id.back_button)
        val languageGroup: RadioGroup = findViewById(R.id.language_group)
        val systemTierGroup: RadioGroup = findViewById(R.id.system_tier_group)
        val fontGroup: RadioGroup = findViewById(R.id.font_group)

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
        when (AppPreferences.getSystemTier(this)) {
            AppPreferences.TIER_MEDIUM -> systemTierGroup.check(R.id.tier_medium)
            AppPreferences.TIER_STRONG -> systemTierGroup.check(R.id.tier_strong)
            AppPreferences.TIER_WEAK -> systemTierGroup.check(R.id.tier_weak)
            else -> systemTierGroup.clearCheck() // هنوز انتخاب نکرده — هیچ‌کدام تیک نخورده
        }

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

        systemTierGroup.setOnCheckedChangeListener { _, checkedId ->
            val tier = when (checkedId) {
                R.id.tier_medium -> AppPreferences.TIER_MEDIUM
                R.id.tier_strong -> AppPreferences.TIER_STRONG
                else -> AppPreferences.TIER_WEAK
            }
            AppPreferences.setSystemTier(this, tier)
        }

        fontGroup.setOnCheckedChangeListener { group, checkedId ->
            val selected = group.findViewById<RadioButton>(checkedId)
            val fontId = selected?.tag as? String ?: FontManager.SYSTEM_DEFAULT
            AppPreferences.setFontId(this, fontId)
            FontManager.applyToViewTree(this, rootView)
        }

        // ==================== بررسی و دانلود خودکار آپدیت ====================
        val versionText: android.widget.TextView = findViewById(R.id.version_text)
        val checkUpdateButton: android.widget.Button = findViewById(R.id.check_update_button)
        versionText.text = getString(R.string.version_label, BuildConfig.VERSION_NAME)

        checkUpdateButton.setOnClickListener {
            checkUpdateButton.isEnabled = false
            checkUpdateButton.text = getString(R.string.checking_for_update)

            UpdateChecker.check(
                onUpdateAvailable = { info ->
                    checkUpdateButton.text = getString(R.string.update_available)
                    UpdateChecker.downloadAndPromptInstall(this, info.downloadUrl)
                    checkUpdateButton.isEnabled = true
                },
                onUpToDate = {
                    checkUpdateButton.text = getString(R.string.up_to_date)
                    checkUpdateButton.isEnabled = true
                    checkUpdateButton.postDelayed({
                        checkUpdateButton.text = getString(R.string.check_for_update)
                    }, 2500)
                },
                onError = { message ->
                    android.widget.Toast.makeText(
                        this,
                        getString(R.string.update_check_failed) + ": $message",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    checkUpdateButton.text = getString(R.string.check_for_update)
                    checkUpdateButton.isEnabled = true
                }
            )
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
