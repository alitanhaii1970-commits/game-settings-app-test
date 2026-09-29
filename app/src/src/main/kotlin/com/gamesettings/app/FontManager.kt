package com.gamesettings.app

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat

/**
 * مدیریت فونت‌های قابل‌انتخاب برنامه.
 * چند فونت معروف ایرانی و خارجی به‌صورت واقعی همراه اپ باندل شده‌اند (بدون نیاز به اینترنت).
 */
object FontManager {

    const val SYSTEM_DEFAULT = "system"

    data class FontOption(
        val id: String,
        val displayName: String,
        val regularRes: Int,
        val boldRes: Int
    )

    val OPTIONS: List<FontOption> = listOf(
        FontOption(SYSTEM_DEFAULT, "پیش‌فرض سیستم / System Default", -1, -1),
        FontOption("vazirmatn", "وزیرمتن", R.font.vazirmatn_regular, R.font.vazirmatn_bold),
        FontOption("sahel", "ساحل", R.font.sahel_regular, R.font.sahel_bold),
        FontOption("montserrat", "Montserrat", R.font.montserrat_regular, R.font.montserrat_bold),
        FontOption("inter", "Inter", R.font.inter_regular, R.font.inter_bold)
    )

    fun findOption(id: String): FontOption =
        OPTIONS.firstOrNull { it.id == id } ?: OPTIONS.first()

    private fun safeLoad(context: Context, resId: Int): Typeface? =
        try {
            ResourcesCompat.getFont(context, resId)
        } catch (e: Exception) {
            null
        }

    /**
     * فونت انتخابی فعلی کاربر را روی تمام متن‌های داخل یک درخت View اعمال می‌کند
     * (وزن Bold/Regular هر عنصر بر اساس ظاهر فعلی‌اش حفظ می‌شود).
     * اگر فونت انتخابی «پیش‌فرض سیستم» باشد، همه‌چیز به فونت خود اندروید برمی‌گردد.
     */
    fun applyToViewTree(context: Context, root: View) {
        val option = findOption(AppPreferences.getFontId(context))

        val regular: Typeface?
        val bold: Typeface?
        if (option.id == SYSTEM_DEFAULT) {
            regular = Typeface.DEFAULT
            bold = Typeface.DEFAULT_BOLD
        } else {
            regular = safeLoad(context, option.regularRes) ?: Typeface.DEFAULT
            bold = safeLoad(context, option.boldRes) ?: Typeface.DEFAULT_BOLD
        }

        applyRecursive(root, regular, bold)
    }

    private fun applyRecursive(view: View, regular: Typeface?, bold: Typeface?) {
        if (view is TextView) {
            val isBold = view.typeface?.isBold == true || view.typeface?.style == Typeface.BOLD
            view.typeface = if (isBold) bold else regular
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyRecursive(view.getChildAt(i), regular, bold)
            }
        }
    }
}
