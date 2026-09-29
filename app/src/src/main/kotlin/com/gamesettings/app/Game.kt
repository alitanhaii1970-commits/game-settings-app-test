package com.gamesettings.app

import com.google.firebase.firestore.PropertyName

/**
 * مدل داده هر بازی که از Firestore خونده می‌شه.
 * اسم فیلدها باید دقیقاً با اسم فیلدهای Firestore یکی باشه.
 *
 * هر بازی دو دسته تنظیمات پیشنهادی دارد:
 *  - settingsGreen: تنظیمات بهینه سبز
 *  - settingsYellow: تنظیمات بهینه زرد
 * هر دو اختیاری‌اند؛ اگر خالی باشند در جزئیات بازی نمایش داده نمی‌شوند.
 *
 * علاوه بر این، هر بازی می‌تواند به‌جای تنظیمات، یک راهنمای ویدیویی یوتیوب داشته باشد:
 *  - youtubeUrl: لینک ویدیوی راهنما (اختیاری)
 *  - showYoutubeButton: آیا حالت «فقط یوتیوب» برای این بازی فعال است؟
 * اگر لینک خالی یا نامعتبر باشد، دکمه نمایش داده نمی‌شود و برنامه کرش نمی‌کند.
 */
data class Game(
    var id: String = "",
    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl")
    var imageUrl: String = "",
    @get:PropertyName("settingsGreen") @set:PropertyName("settingsGreen")
    var settingsGreen: String = "",
    @get:PropertyName("settingsYellow") @set:PropertyName("settingsYellow")
    var settingsYellow: String = "",
    @get:PropertyName("youtubeUrl") @set:PropertyName("youtubeUrl")
    var youtubeUrl: String = "",
    @get:PropertyName("showYoutubeButton") @set:PropertyName("showYoutubeButton")
    var showYoutubeButton: Boolean = false,
    @get:PropertyName("updatedAt") @set:PropertyName("updatedAt")
    var updatedAt: Long = 0L
)
