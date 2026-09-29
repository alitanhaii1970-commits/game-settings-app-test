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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import coil.load

class GameDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppPreferences.applyLanguage(AppPreferences.getLanguage(this))
        AppPreferences.applyTheme(AppPreferences.getTheme(this))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_detail)

        val rootView = findViewById<View>(android.R.id.content)
        FontManager.applyToViewTree(this, rootView)
        rootView.alpha = 0f
        rootView.animate().alpha(1f).setDuration(280).start()

        val toolbar = findViewById<Toolbar>(R.id.detail_toolbar)
        setSupportActionBar(toolbar)
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

        // ظاهر شیشه‌ای روی کادرهای تنظیمات (در صورت فعال بودن)
        GlassStyler.applyCard(this, greenText)
        GlassStyler.applyCard(this, yellowText)
        GlassStyler.applyCard(this, youtubeSection)

        val name = intent.getStringExtra("name").orEmpty()
        val imageUrl = intent.getStringExtra("imageUrl").orEmpty()
        val settingsGreen = intent.getStringExtra("settingsGreen").orEmpty()
        val settingsYellow = intent.getStringExtra("settingsYellow").orEmpty()
        val youtubeUrl = intent.getStringExtra("youtubeUrl").orEmpty()
        val showYoutubeButton = intent.getBooleanExtra("showYoutubeButton", false)

        title = name
        toolbar.title = name

        image.load(imageUrl) {
            crossfade(400)
            placeholder(R.drawable.image_placeholder)
            error(R.drawable.image_placeholder)
        }

        // لمس عکس، پیش‌نمایش تمام‌صفحه (بدون کراپ، با نسبت اصلی) را باز می‌کند
        imageTapArea.setOnClickListener {
            val intent = Intent(this, ImagePreviewActivity::class.java)
            intent.putExtra("imageUrl", imageUrl)
            startActivity(intent)
        }

        // آیا این بازی حالت «فقط راهنمای یوتیوب» دارد؟ (لینک معتبر + فعال)
        val hasValidYoutubeLink = showYoutubeButton &&
            youtubeUrl.isNotBlank() &&
            (youtubeUrl.startsWith("http://") || youtubeUrl.startsWith("https://"))

        if (hasValidYoutubeLink) {
            // حالت انحصاری یوتیوب: هیچ بخش تنظیماتی نشان داده نمی‌شود
            youtubeSection.visibility = View.VISIBLE
            greenSection.visibility = View.GONE
            yellowSection.visibility = View.GONE

            watchButton.setOnClickListener {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "لینک یوتیوب قابل باز شدن نیست", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // حالت عادی: تنظیمات بهینه سبز/زرد
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
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
