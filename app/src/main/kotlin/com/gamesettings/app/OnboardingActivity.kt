package com.gamesettings.app

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ViewFlipper
import androidx.activity.OnBackPressedCallback

/**
 * صفحه‌ی ورود اولیه که فقط یک‌بار (پیش از باز شدن صفحه‌ی اصلی) نشان داده می‌شود:
 * خوش‌آمدگویی → انتخاب زبان → انتخاب ظاهر (روشن/تیره/خودکار) → قدرت سیستم → شروع.
 *
 * ظاهر همان لحظه‌ی انتخاب اعمال می‌شود (با انیمیشن موج) تا کاربر نتیجه را ببیند؛
 * چون اعمال تم صفحه را دوباره می‌سازد، مرحله‌ی فعلی و انتخاب‌ها در onSaveInstanceState نگه داشته می‌شوند.
 * زبان فقط در انتهای مسیر اعمال می‌شود تا میانه‌ی کار صفحه دوباره‌ساز نشود.
 */
class OnboardingActivity : BaseActivity() {

    private lateinit var flipper: ViewFlipper
    private lateinit var button: Button
    private lateinit var dots: List<View>

    private var selectedLang: String = AppPreferences.LANG_FA
    private var selectedTier: String = AppPreferences.TIER_MEDIUM
    private var selectedTheme: String = AppPreferences.THEME_DARK
    private var currentPage = 0

    private val welcomeAnimators = mutableListOf<ObjectAnimator>()

    override fun onCreate(savedInstanceState: Bundle?) {
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

        selectedTheme = AppPreferences.getTheme(this)
        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt(STATE_PAGE, 0)
            selectedLang = savedInstanceState.getString(STATE_LANG) ?: selectedLang
            selectedTier = savedInstanceState.getString(STATE_TIER) ?: selectedTier
        }

        setupLanguageStep()
        setupThemeStep()
        setupSystemTierStep()

        // بعد از عوض‌شدن تم، همان مرحله‌ای که کاربر در آن بود (بدون انیمیشن) برگردانده می‌شود
        if (currentPage > 0) {
            flipper.inAnimation = null
            flipper.outAnimation = null
            flipper.displayedChild = currentPage
        }

        button.setOnClickListener {
            it.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }.start()

            if (flipper.displayedChild < LAST_PAGE) {
                goTo(forward = true)
            } else {
                finishOnboarding()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (flipper.displayedChild > 0) {
                    goTo(forward = false)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        updatePage(currentPage, animate = false)

        // ورود پلکانی صفحه‌ی خوش‌آمد (فقط بار اول، نه بعد از عوض‌شدن تم)
        if (savedInstanceState == null && !Motion.reduced(this)) {
            val logo = findViewById<View>(R.id.welcome_logo)
            logo.alpha = 0f
            logo.scaleX = 0.6f
            logo.scaleY = 0.6f
            logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(80L).setDuration(620L)
                .setInterpolator(OvershootInterpolator(1.6f)).start()
            Motion.riseIn(findViewById(R.id.welcome_title), 280L, 16, 520L)
            Motion.riseIn(findViewById(R.id.welcome_subtitle), 400L, 16, 520L)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_PAGE, flipper.displayedChild)
        outState.putString(STATE_LANG, selectedLang)
        outState.putString(STATE_TIER, selectedTier)
    }

    override fun onResume() {
        super.onResume()
        if (currentPage == 0) startWelcomeAnimations()
    }

    override fun onPause() {
        stopWelcomeAnimations()
        super.onPause()
    }

    // ───────────── مراحل ─────────────

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

        optionFa.setOnClickListener { selectedLang = AppPreferences.LANG_FA; refresh(); bounce(checkFa); pulse(optionFa) }
        optionEn.setOnClickListener { selectedLang = AppPreferences.LANG_EN; refresh(); bounce(checkEn); pulse(optionEn) }
        refresh()
    }

    private fun setupThemeStep() {
        ThemeManager.bindTiles(findViewById(R.id.theme_tiles), selectedTheme) { theme, tile ->
            selectedTheme = theme
            val loc = IntArray(2)
            tile.getLocationOnScreen(loc)
            ThemeManager.changeTheme(this, theme, loc[0] + tile.width / 2, loc[1] + tile.height / 2)
        }
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

        optionWeak.setOnClickListener { selectedTier = AppPreferences.TIER_WEAK; refresh(); bounce(checkWeak); pulse(optionWeak) }
        optionMedium.setOnClickListener { selectedTier = AppPreferences.TIER_MEDIUM; refresh(); bounce(checkMedium); pulse(optionMedium) }
        optionStrong.setOnClickListener { selectedTier = AppPreferences.TIER_STRONG; refresh(); bounce(checkStrong); pulse(optionStrong) }
        refresh()
    }

    // ───────────── انیمیشن‌ها ─────────────

    private fun bounce(view: View) {
        view.scaleX = 0.4f
        view.scaleY = 0.4f
        view.animate().scaleX(1f).scaleY(1f).setDuration(260).setInterpolator(OvershootInterpolator()).start()
    }

    /** یک ضربه‌ی کوچک روی کارت انتخاب‌شده */
    private fun pulse(view: View) {
        if (Motion.reduced(this)) return
        view.animate().cancel()
        view.scaleX = 0.97f
        view.scaleY = 0.97f
        view.animate().scaleX(1f).scaleY(1f).setDuration(300L).setInterpolator(OvershootInterpolator(2.4f)).start()
    }

    /** جابه‌جایی بین مراحل با حرکتِ سازگار با جهت زبان (راست‌به‌چپ یا چپ‌به‌راست) */
    private fun goTo(forward: Boolean) {
        if (Motion.reduced(this)) {
            flipper.inAnimation = null
            flipper.outAnimation = null
        } else {
            val rtl = Motion.isRtl(this)
            flipper.inAnimation = Motion.pageAnimation(entering = true, forward = forward, rtl = rtl)
            flipper.outAnimation = Motion.pageAnimation(entering = false, forward = forward, rtl = rtl)
        }
        if (forward) flipper.showNext() else flipper.showPrevious()
        currentPage = flipper.displayedChild
        updatePage(currentPage, animate = true)
    }

    private fun updatePage(index: Int, animate: Boolean) {
        dots.forEachIndexed { i, dot ->
            val targetWidth = dpToPx(if (i == index) 22 else 8)
            dot.setBackgroundResource(if (i == index) R.drawable.dot_active else R.drawable.dot_inactive)
            val params = dot.layoutParams
            if (animate && !Motion.reduced(this) && params.width != targetWidth) {
                ValueAnimator.ofInt(params.width, targetWidth).apply {
                    duration = 280L
                    interpolator = DecelerateInterpolator(1.4f)
                    addUpdateListener {
                        val lp = dot.layoutParams
                        lp.width = it.animatedValue as Int
                        dot.layoutParams = lp
                    }
                    start()
                }
            } else {
                params.width = targetWidth
                dot.layoutParams = params
            }
        }
        button.text = if (index == LAST_PAGE) getString(R.string.onboard_finish) else getString(R.string.onboard_next)
        if (index == 0) startWelcomeAnimations() else stopWelcomeAnimations()
    }

    /** لوگوی خوش‌آمد آرام شناور می‌شود و هاله‌ی پشتش نفس می‌کشد */
    private fun startWelcomeAnimations() {
        stopWelcomeAnimations()
        if (Motion.reduced(this)) return
        val logo = findViewById<View>(R.id.welcome_logo) ?: return
        val glow = findViewById<View>(R.id.welcome_glow) ?: return

        welcomeAnimators += ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, 0f, -dpToPx(8).toFloat()).apply {
            duration = 2600L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        welcomeAnimators += ObjectAnimator.ofPropertyValuesHolder(
            glow,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f, 1.12f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f, 1.12f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0.65f, 1f)
        ).apply {
            duration = 2600L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun stopWelcomeAnimations() {
        welcomeAnimators.forEach { it.cancel() }
        welcomeAnimators.clear()
    }

    override fun onDestroy() {
        stopWelcomeAnimations()
        super.onDestroy()
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private fun finishOnboarding() {
        AppPreferences.setLanguage(this, selectedLang)
        AppPreferences.setSystemTier(this, selectedTier)
        AppPreferences.setTheme(this, selectedTheme)
        AppPreferences.setOnboardingDone(this)

        startActivity(Intent(this, MainActivity::class.java))
        Motion.pushForward(this)
        finish()
    }

    private companion object {
        const val LAST_PAGE = 3
        const val STATE_PAGE = "onboarding_page"
        const val STATE_LANG = "onboarding_lang"
        const val STATE_TIER = "onboarding_tier"
    }
}
