import Foundation

enum Lang: String, CaseIterable {
    case fa
    case en
}

/// رشته‌ها — همان متن‌های نسخه‌ی اندروید (فارسی/انگلیسی)
enum L {
    static func t(_ key: String, _ lang: Lang) -> String {
        let table = (lang == .fa) ? fa : en
        return table[key] ?? key
    }

    static let fa: [String: String] = [
        "search_hint": "جستجوی بازی…",
        "empty_games": "هنوز بازی‌ای اضافه نشده",
        "no_results": "بازی‌ای با این اسم پیدا نشد",
        "offline_empty": "اتصال به اینترنت برقرار نیست.\nلیست قبلی موجود نیست.",
        "offline_toast": "اتصال برقرار نشد، لیست قبلی نشون داده می‌شه",
        "list_updated": "لیست به‌روز شد ✅",
        "retry": "تلاش دوباره",
        "app_tagline": "تنظیمات بهینه بازی‌ها، همیشه در دسترس شما",
        "green_settings": "تنظیمات بهینه سبز",
        "yellow_settings": "تنظیمات بهینه زرد",
        "no_settings": "برای این بازی هنوز تنظیماتی ثبت نشده.",
        "recommended": "پیشنهادی برای سیستم شما",
        "copy": "کپی",
        "copied": "کپی شد",
        "youtube_hint": "برای دیدن تنظیمات این بازی روی لینک یوتیوب کلیک کنید",
        "watch_on_youtube": "مشاهده در یوتیوب",
        "settings_title": "تنظیمات",
        "settings_language": "زبان برنامه",
        "settings_font": "فونت برنامه",
        "settings_system_power": "قدرت سیستم شما (کامپیوتر)",
        "tier_weak": "ضعیف",
        "tier_medium": "متوسط",
        "tier_strong": "قوی",
        "tier_weak_desc": "کارت گرافیک و پردازنده‌ی پایین‌رده",
        "tier_medium_desc": "سیستم معمولی و میان‌رده",
        "tier_strong_desc": "سخت‌افزار قدرتمند و جدید",
        "lang_fa": "فارسی",
        "lang_en": "English",
        "about_title": "درباره",
        "about_text": "PC Max — تنظیمات بهینه بازی‌ها برای کامپیوتر",
        "check_for_update": "بررسی آپدیت",
        "checking_for_update": "در حال بررسی…",
        "up_to_date": "شما به‌روز هستید ✅",
        "update_check_failed": "بررسی آپدیت ناموفق بود",
        "update_found_title": "نسخه‌ی جدید پیدا شد",
        "update_ios_hint": "فایل نصبی جدید دانلود می‌شه. بعد از دانلود، اون رو با AltStore یا Sideloadly نصب کن تا جایگزین نسخه‌ی فعلی بشه.",
        "download": "دانلود",
        "later": "بعداً",
        "version_label": "نسخه‌ی فعلی: %@",
        "onboard_welcome_title": "به PC Max خوش آمدید",
        "onboard_welcome_subtitle": "تنظیمات بهینه بازی‌ها، همیشه در دسترس شما",
        "onboard_lang_title": "زبان برنامه را انتخاب کنید",
        "onboard_lang_subtitle": "می‌توانید بعداً از تنظیمات آن را تغییر دهید",
        "onboard_tier_title": "قدرت سیستم شما (کامپیوتر) چطوره؟",
        "onboard_tier_subtitle": "بر این اساس، توی هر بازی بهت نشون می‌دیم کدوم تنظیمات مناسب‌تره",
        "onboard_next": "بعدی",
        "onboard_finish": "شروع کنید"
    ]

    static let en: [String: String] = [
        "search_hint": "Search game…",
        "empty_games": "No games added yet",
        "no_results": "No game found with that name",
        "offline_empty": "No internet connection.\nNo previous list available.",
        "offline_toast": "Couldn't connect, showing the previous list",
        "list_updated": "List updated ✅",
        "retry": "Retry",
        "app_tagline": "Optimal game settings, always at hand",
        "green_settings": "Green Optimal Settings",
        "yellow_settings": "Yellow Optimal Settings",
        "no_settings": "No settings have been added for this game yet.",
        "recommended": "Recommended for your PC",
        "copy": "Copy",
        "copied": "Copied",
        "youtube_hint": "Tap the YouTube link to see this game's settings",
        "watch_on_youtube": "Watch on YouTube",
        "settings_title": "Settings",
        "settings_language": "App language",
        "settings_font": "App font",
        "settings_system_power": "Your System Power (PC)",
        "tier_weak": "Weak",
        "tier_medium": "Medium",
        "tier_strong": "Strong",
        "tier_weak_desc": "Entry-level GPU and CPU",
        "tier_medium_desc": "Everyday mid-range system",
        "tier_strong_desc": "Powerful, modern hardware",
        "lang_fa": "فارسی",
        "lang_en": "English",
        "about_title": "About",
        "about_text": "PC Max — optimal game settings for PC",
        "check_for_update": "Check for Update",
        "checking_for_update": "Checking…",
        "up_to_date": "You're up to date ✅",
        "update_check_failed": "Update check failed",
        "update_found_title": "New version found",
        "update_ios_hint": "The new installer will be downloaded. Once it's done, install it with AltStore or Sideloadly to replace the current version.",
        "download": "Download",
        "later": "Later",
        "version_label": "Current version: %@",
        "onboard_welcome_title": "Welcome to PC Max",
        "onboard_welcome_subtitle": "Optimal game settings, always at hand",
        "onboard_lang_title": "Choose your language",
        "onboard_lang_subtitle": "You can change this later in Settings",
        "onboard_tier_title": "How powerful is your system (PC)?",
        "onboard_tier_subtitle": "We'll use this to show which settings fit your PC best, for each game",
        "onboard_next": "Next",
        "onboard_finish": "Get Started"
    ]
}
