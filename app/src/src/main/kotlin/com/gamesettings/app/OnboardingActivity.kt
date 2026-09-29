package com.gamesettings.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ViewFlipper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

/**
 * صفحه‌ی ورود اولیه که فقط یک‌بار (پیش از باز شدن صفحه‌ی اصلی) نشان داده می‌شود:
 * خوش‌آمدگویی → انتخاب زبان → انتخاب تم → حالت شیشه‌ای → شروع.
 * تغییرات واقعی (زبان/تم) فقط در انتهای مسیر اعمال می‌شوند تا میانه‌ی کار، صفحه دوباره‌ساز نشود.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var flipper: ViewFlipper
    private lateinit var button: Button
    private lateinit var dots: List<View>

    private var selectedLang: String = AppPreferences.LANG_FA
    private var selectedTheme: String = AppPreferences.THEME_DARK
    private var glassEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // ظاهر خود ویزارد همیشه تیره است (صرف‌نظر از تنظیم سیستم)، چون گزینه‌ی
        // «تیره» به‌طور پیش‌فرض در مرحله‌ی تم به‌عنوان انتخاب‌شده نشان داده می‌شود.
        // انتخاب نهایی کاربر در finishOnboarding() به‌درستی جایگزین این حالت می‌شود.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        flipper = findViewById(R.id.onboarding_flipper)
        button = findViewById(R.id.onboard_button)
        dots = listOf(
            findViewById(R.id.dot_0),
            findViewById(R.id.dot_1),
            findViewById(R.id.dot_2),
            findViewById(R.id.dot_3)
        )

        setupLanguageStep()
        setupThemeStep()
        setupGlassStep()

        button.setOnClickListener {
            it.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()

            if (flipper.displayedChild < 3) {
                flipper.showNext()
                updatePage(flipper.displayedChild)
            } else {
                finishOnboarding()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (flipper.displayedChild > 0) {
                    flipper.showPrevious()
                    updatePage(flipper.displayedChild)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        updatePage(0)
    }

    private fun setupLanguageStep() {
        val optionFa = findViewById<LinearLayout>(R.id.option_lang_fa)
        val optionEn = findViewById<LinearLayout>(R.id.option_lang_en)
        val checkFa = findViewById<ImageView>(R.id.check_lang_fa)
        val checkEn = findViewById<ImageView>(R.id.check_lang_en)

        fun refresh() {
            val isFa = selectedLang == AppPreferences.LANG_FA
            optionFa.setBackgroundResource(if (isFa) R.drawable.bg_selectable_card_selected else R.drawable.bg_selectable_card)
            optionEn.setBackgroundResource(if (!isFa) R.drawable.bg_selectable_card_selected else R.drawable.bg_selectable_card)
            checkFa.visibility = if (isFa) View.VISIBLE else View.INVISIBLE
            checkEn.visibility = if (!isFa) View.VISIBLE else View.INVISIBLE
        }

        optionFa.setOnClickListener { selectedLang = AppPreferences.LANG_FA; refresh(); bounce(checkFa) }
        optionEn.setOnClickListener { selectedLang = AppPreferences.LANG_EN; refresh(); bounce(checkEn) }
        refresh()
    }

    private fun setupThemeStep() {
        val optionDark = findViewById<LinearLayout>(R.id.option_theme_dark)
        val optionLight = findViewById<LinearLayout>(R.id.option_theme_light)
        val checkDark = findViewById<ImageView>(R.id.check_theme_dark)
        val checkLight = findViewById<ImageView>(R.id.check_theme_light)

        fun refresh() {
            val isDark = selectedTheme == AppPreferences.THEME_DARK
            optionDark.setBackgroundResource(if (isDark) R.drawable.bg_selectable_card_selected else R.drawable.bg_selectable_card)
            optionLight.setBackgroundResource(if (!isDark) R.drawable.bg_selectable_card_selected else R.drawable.bg_selectable_card)
            checkDark.visibility = if (isDark) View.VISIBLE else View.INVISIBLE
            checkLight.visibility = if (!isDark) View.VISIBLE else View.INVISIBLE
        }

        optionDark.setOnClickListener { selectedTheme = AppPreferences.THEME_DARK; refresh(); bounce(checkDark) }
        optionLight.setOnClickListener { selectedTheme = AppPreferences.THEME_LIGHT; refresh(); bounce(checkLight) }
        refresh()
    }

    private fun setupGlassStep() {
        val glassSwitch = findViewById<SwitchCompat>(R.id.onboard_glass_switch)
        val previewCard = findViewById<View>(R.id.glass_preview_card)

        glassSwitch.setOnCheckedChangeListener { _, isChecked ->
            glassEnabled = isChecked
            previewCard.setBackgroundResource(if (isChecked) R.drawable.bg_card_glass else R.drawable.bg_card)
        }
    }

    private fun bounce(view: View) {
        view.scaleX = 0.4f
        view.scaleY = 0.4f
        view.animate().scaleX(1f).scaleY(1f).setDuration(260).setInterpolator(OvershootInterpolator()).start()
    }

    private fun updatePage(index: Int) {
        dots.forEachIndexed { i, dot ->
            val params = dot.layoutParams
            params.width = dpToPx(if (i == index) 22 else 8)
            dot.layoutParams = params
            dot.setBackgroundResource(if (i == index) R.drawable.dot_active else R.drawable.dot_inactive)
        }
        button.text = if (index == 3) getString(R.string.onboard_finish) else getString(R.string.onboard_next)
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private fun finishOnboarding() {
        AppPreferences.setLanguage(this, selectedLang)
        AppPreferences.setTheme(this, selectedTheme)
        AppPreferences.setGlassEffect(this, glassEnabled)
        AppPreferences.setOnboardingDone(this)

        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}
