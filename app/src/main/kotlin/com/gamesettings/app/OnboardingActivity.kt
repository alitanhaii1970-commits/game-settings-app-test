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

/**
 * صفحه‌ی ورود اولیه که فقط یک‌بار (پیش از باز شدن صفحه‌ی اصلی) نشان داده می‌شود:
 * خوش‌آمدگویی → انتخاب زبان → قدرت سیستم → شروع.
 * تغییرات واقعی (زبان) فقط در انتهای مسیر اعمال می‌شوند تا میانه‌ی کار، صفحه دوباره‌ساز نشود.
 */
class OnboardingActivity : AppCompatActivity() {

    private lateinit var flipper: ViewFlipper
    private lateinit var button: Button
    private lateinit var dots: List<View>

    private var selectedLang: String = AppPreferences.LANG_FA
    private var selectedTier: String = AppPreferences.TIER_MEDIUM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        flipper = findViewById(R.id.onboarding_flipper)
        button = findViewById(R.id.onboard_button)
        dots = listOf(
            findViewById(R.id.dot_0),
            findViewById(R.id.dot_1),
            findViewById(R.id.dot_2)
        )

        setupLanguageStep()
        setupSystemTierStep()

        button.setOnClickListener {
            it.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()

            if (flipper.displayedChild < 2) {
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

    private fun setupSystemTierStep() {
        val optionWeak = findViewById<LinearLayout>(R.id.option_tier_weak)
        val optionMedium = findViewById<LinearLayout>(R.id.option_tier_medium)
        val optionStrong = findViewById<LinearLayout>(R.id.option_tier_strong)
        val checkWeak = findViewById<ImageView>(R.id.check_tier_weak)
        val checkMedium = findViewById<ImageView>(R.id.check_tier_medium)
        val checkStrong = findViewById<ImageView>(R.id.check_tier_strong)

        fun refresh() {
            optionWeak.setBackgroundResource(if (selectedTier == AppPreferences.TIER_WEAK) R.drawable.bg_selectable_card_selected else R.drawable.bg_selectable_card)
            optionMedium.setBackgroundResource(if (selectedTier == AppPreferences.TIER_MEDIUM) R.drawable.bg_selectable_card_selected else R.drawable.bg_selectable_card)
            optionStrong.setBackgroundResource(if (selectedTier == AppPreferences.TIER_STRONG) R.drawable.bg_selectable_card_selected else R.drawable.bg_selectable_card)
            checkWeak.visibility = if (selectedTier == AppPreferences.TIER_WEAK) View.VISIBLE else View.INVISIBLE
            checkMedium.visibility = if (selectedTier == AppPreferences.TIER_MEDIUM) View.VISIBLE else View.INVISIBLE
            checkStrong.visibility = if (selectedTier == AppPreferences.TIER_STRONG) View.VISIBLE else View.INVISIBLE
        }

        optionWeak.setOnClickListener { selectedTier = AppPreferences.TIER_WEAK; refresh(); bounce(checkWeak) }
        optionMedium.setOnClickListener { selectedTier = AppPreferences.TIER_MEDIUM; refresh(); bounce(checkMedium) }
        optionStrong.setOnClickListener { selectedTier = AppPreferences.TIER_STRONG; refresh(); bounce(checkStrong) }
        refresh()
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
        button.text = if (index == 2) getString(R.string.onboard_finish) else getString(R.string.onboard_next)
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private fun finishOnboarding() {
        AppPreferences.setLanguage(this, selectedLang)
        AppPreferences.setSystemTier(this, selectedTier)
        AppPreferences.setOnboardingDone(this)

        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }
}
