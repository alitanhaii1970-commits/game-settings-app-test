package com.gamesettings.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import coil.load

class GameDetailActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppPreferences.applyLanguage(AppPreferences.getLanguage(this))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        val rootView = findViewById<View>(android.R.id.content)
        FontManager.applyToViewTree(this, rootView)
        // انیمیشن‌های ورود فقط وقتی صفحه تازه باز می‌شود (نه بعد از عوض‌شدن تم)
        val playEntrance = savedInstanceState == null

        val toolbar = findViewById<Toolbar>(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
        // آیکون برگشت با رنگ متن اصلی هماهنگ می‌شه
        toolbar.navigationIcon?.setTint(androidx.core.content.ContextCompat.getColor(this, R.color.text_primary))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val imageTapArea = findViewById<FrameLayout>(R.id.image_tap_area)
        val image = findViewById<ImageView>(R.id.detail_image)
        val youtubeSection = findViewById<LinearLayout>(R.id.youtube_section)
        val greenSection = findViewById<LinearLayout>(R.id.green_section)
        val yellowSection = findViewById<LinearLayout>(R.id.yellow_section)
        val greenText = findViewById<TextView>(R.id.detail_settings_green)
        val yellowText = findViewById<TextView>(R.id.detail_settings_yellow)
        val watchButton = findViewById<Button>(R.id.watch_youtube_button)

        // ✅ اصلاح: دریافت فیلدهای Intent درست
        val name = intent.getStringExtra("name").orEmpty()
        val imageUrl = intent.getStringExtra("imageUrl").orEmpty()
        val settingsGreen = intent.getStringExtra("settingsGreen").orEmpty()
        val settingsYellow = intent.getStringExtra("settingsYellow").orEmpty()
        val youtubeUrl = intent.getStringExtra("youtubeUrl").orEmpty()
        val showYoutubeButton = intent.getBooleanExtra("showYoutubeButton", false)

        // ✅ اصلاح: Debug log برای بررسی اینکه چی دریافت شده
        android.util.Log.d("GameDetail", "YouTube URL received: '$youtubeUrl'")
        android.util.Log.d("GameDetail", "Show YouTube Button: $showYoutubeButton")

        title = name
        toolbar.title = name

        if (playEntrance) {
            // عکس اصلی: کمی کوچک‌تر شروع می‌کند و جا می‌افتد
            imageTapArea.alpha = 0f
            imageTapArea.scaleX = 0.94f
            imageTapArea.scaleY = 0.94f
            imageTapArea.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(480L)
                .setInterpolator(android.view.animation.DecelerateInterpolator(1.6f)).start()
        }

        image.load(imageUrl) {
            crossfade(400)
            placeholder(R.drawable.image_placeholder)
            error(R.drawable.image_placeholder)
        }

        // لمس عکس → پیش‌نمایش تمام‌صفحه (با فیدبک لمسی ملایم روی خود عکس)
        imageTapArea.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN ->
                    image.animate().scaleX(0.98f).scaleY(0.98f).setDuration(100).start()
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL ->
                    image.animate().scaleX(1f).scaleY(1f)
                        .setDuration(200)
                        .setInterpolator(android.view.animation.OvershootInterpolator(1.5f))
                        .start()
            }
            false
        }
        imageTapArea.setOnClickListener {
            val intent = Intent(this, ImagePreviewActivity::class.java)
            intent.putExtra("imageUrl", imageUrl)
            startActivity(intent)
        }

        // ✅ اصلاح: لاجیک صحیح برای یوتیوب
        val hasValidYoutubeLink = showYoutubeButton &&
            youtubeUrl.isNotBlank() &&
            youtubeUrl.trim().isNotEmpty() &&
            (youtubeUrl.contains("youtube") || youtubeUrl.startsWith("http"))

        if (hasValidYoutubeLink) {
            // حالت یوتیوب انحصاری
            if (playEntrance) Motion.riseIn(youtubeSection, 140L, 16, 460L)
            youtubeSection.visibility = View.VISIBLE
            greenSection.visibility = View.GONE
            yellowSection.visibility = View.GONE

            // ✅ اصلاح: دکمه کار کنه درست
            watchButton.setOnClickListener {
                try {
                    val cleanUrl = youtubeUrl.trim()
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "مرورگر یافت نشد — Chrome/Firefox نصب کنید",
                        Toast.LENGTH_SHORT
                    ).show()
                    android.util.Log.e("GameDetail", "YouTube Error: ${e.message}")
                }
            }
        } else {
            // حالت عادی: تنظیمات سبز/زرد
            youtubeSection.visibility = View.GONE

            if (settingsGreen.isNotBlank()) {
                greenSection.visibility = View.VISIBLE
                greenText.text = settingsGreen
            } else {
                greenSection.visibility = View.GONE
            }

            if (settingsYellow.isNotBlank()) {
                yellowSection.visibility = View.VISIBLE
                yellowText.text = settingsYellow
            } else {
                yellowSection.visibility = View.GONE
            }

            if (settingsGreen.isBlank() && settingsYellow.isBlank()) {
                greenSection.visibility = View.VISIBLE
                greenText.text = "برای این بازی هنوز تنظیماتی ثبت نشده."
            }

            // ورود پلکانی بخش‌ها (بعد از عکس)
            if (playEntrance) {
                if (greenSection.visibility == View.VISIBLE) Motion.riseIn(greenSection, 140L, 16, 460L)
                if (yellowSection.visibility == View.VISIBLE) Motion.riseIn(yellowSection, 240L, 16, 460L)
            }

            // افکت درخشش روی بخشی که برای قدرت سیستمِ کاربر مناسب‌تره — فقط اگر
            // کاربر قبلاً قدرت سیستمش رو در تنظیمات انتخاب کرده باشه، و فقط روی
            // بخشی که واقعاً محتوا داره (سبز برای ضعیف/متوسط، زرد برای قوی)
            val systemTier = AppPreferences.getSystemTier(this)
            if (systemTier.isNotBlank()) {
                val recommendGreen = systemTier != AppPreferences.TIER_STRONG
                val fallbackOnly = settingsGreen.isBlank() && settingsYellow.isBlank()
                if (recommendGreen && greenSection.visibility == View.VISIBLE && !fallbackOnly) {
                    applyGlow(greenText, R.drawable.bg_card_glow_green)
                    findViewById<View>(R.id.badge_green).visibility = View.VISIBLE
                } else if (!recommendGreen && yellowSection.visibility == View.VISIBLE) {
                    applyGlow(yellowText, R.drawable.bg_card_glow_yellow)
                    findViewById<View>(R.id.badge_yellow).visibility = View.VISIBLE
                }
            }
        }
    }

    /** حاشیه‌ی رنگی روشن + یک نفسِ ملایم و پیوسته (پالس ظریف مقیاس) برای جلب توجه بدون مزاحمت. */
    private fun applyGlow(view: View, glowDrawableRes: Int) {
        view.setBackgroundResource(glowDrawableRes)

        val reduceMotion = android.provider.Settings.Global.getFloat(
            contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
        if (reduceMotion) return

        val pulse = android.animation.ValueAnimator.ofFloat(1f, 1.018f, 1f).apply {
            duration = 1600
            repeatCount = android.animation.ValueAnimator.INFINITE
            interpolator = android.view.animation.AccelerateDecelerateInterpolator()
            addUpdateListener {
                val scale = it.animatedValue as Float
                view.scaleX = scale
                view.scaleY = scale
            }
        }
        pulse.start()
        glowAnimator = pulse
    }

    private var glowAnimator: android.animation.ValueAnimator? = null

    override fun finish() {
        super.finish()
        Motion.popBack(this)
    }

    override fun onDestroy() {
        glowAnimator?.cancel()
        super.onDestroy()
    }
}
