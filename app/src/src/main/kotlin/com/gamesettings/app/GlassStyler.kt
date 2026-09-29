package com.gamesettings.app

import android.content.Context
import android.view.View

/**
 * اعمال ظاهر شیشه‌ای (glass) روی view های کارت‌مانند، بدون تغییر چیدمان یا اندازه‌ی آن‌ها.
 * فقط drawable پس‌زمینه بر اساس تنظیم کاربر عوض می‌شود.
 */
object GlassStyler {

    fun applyCard(context: Context, view: View) {
        view.setBackgroundResource(
            if (AppPreferences.getGlassEffect(context)) R.drawable.bg_card_glass else R.drawable.bg_card
        )
    }

    fun applyRoundButton(context: Context, view: View) {
        view.setBackgroundResource(
            if (AppPreferences.getGlassEffect(context)) R.drawable.bg_round_button_glass else R.drawable.bg_round_button
        )
    }

    fun applySearchBox(context: Context, view: View) {
        view.setBackgroundResource(
            if (AppPreferences.getGlassEffect(context)) R.drawable.bg_search_box_glass else R.drawable.bg_search_box
        )
    }
}
