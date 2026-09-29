package com.gamesettings.app

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import coil.load

/**
 * پیش‌نمایش تمام‌صفحه‌ی عکس بازی — دقیقاً همان تصویر اصلی، بدون کراپ یا زوم ناخواسته.
 * نسبت تصویر با scaleType=fitCenter کاملاً حفظ می‌شود.
 * با لمس پس‌زمینه یا دکمه‌ی بستن، صفحه بسته می‌شود.
 */
class ImagePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppPreferences.applyLanguage(AppPreferences.getLanguage(this))
        AppPreferences.applyTheme(AppPreferences.getTheme(this))

        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_scale_in, 0)
        setContentView(R.layout.activity_image_preview)

        val backdrop = findViewById<FrameLayout>(R.id.preview_backdrop)
        val image = findViewById<ImageView>(R.id.preview_image)
        val closeButton = findViewById<ImageButton>(R.id.preview_close)

        val imageUrl = intent.getStringExtra("imageUrl").orEmpty()

        image.scaleX = 0.92f
        image.scaleY = 0.92f
        image.alpha = 0f
        image.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(260).start()

        image.load(imageUrl) {
            crossfade(200)
            placeholder(R.drawable.image_placeholder)
            error(R.drawable.image_placeholder)
        }

        // لمس پس‌زمینه (بیرون از خود تصویر) صفحه را می‌بندد
        backdrop.setOnClickListener { finish() }
        image.setOnClickListener { /* لمس خود تصویر چیزی را نمی‌بندد */ }
        closeButton.setOnClickListener { finish() }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.fade_scale_out)
    }
}
