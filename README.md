<div align="center">
  <img src="https://raw.githubusercontent.com/alitanhaii1970-commits/game-settings-app/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" alt="PC Max Logo" />

  # PC Max

  **بهترین تنظیمات گرافیکی بازی‌ها، یک‌جا و رایگان**

  [![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://github.com/alitanhaii1970-commits/game-settings-app/releases/latest)
  [![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
  [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](#مجوز)

  [دانلود آخرین نسخه](https://github.com/alitanhaii1970-commits/game-settings-app/releases/latest) · [ویژگی‌ها](#-ویژگی‌ها) · [نصب](#-نصب) · [ساختار پروژه](#-ساختار-پروژه)

</div>

---

## درباره‌ی پروژه

خیلی از بازی‌ها روی گوشی، به‌خاطر تنظیمات پیش‌فرض نامناسب، یا به کندی اجرا می‌شن یا کیفیت گرافیکی‌شون افت می‌کنه. **PC Max** یک اپلیکیشن اندروید است که برای هر بازی، دو دسته تنظیمات پیشنهادیِ آماده ارائه می‌ده:

- 🟢 **تنظیمات سبز** — بهترین FPS و روان‌ترین اجرا
- 🟡 **تنظیمات زرد** — بهترین کیفیت گرافیکی

برای بعضی بازی‌ها هم به‌جای متن، یک **راهنمای ویدیویی در یوتیوب** در دسترسه.

محتوای برنامه از یک پنل مدیریت اختصاصی به‌روزرسانی می‌شه، بدون نیاز به انتشار نسخه‌ی جدید اپلیکیشن.

---

## ✨ ویژگی‌ها

| | |
|---|---|
| 🎮 **کتابخانه‌ی بازی‌ها** | جستجوی سریع در میان بازی‌های پشتیبانی‌شده |
| 🟢🟡 **دو دسته تنظیمات** | پیشنهاد جداگانه برای عملکرد و برای کیفیت |
| ▶️ **راهنمای یوتیوب** | برای بازی‌هایی که تنظیمات ویدیویی دارن |
| 🌗 **تم روشن/تیره** | مطابق سلیقه و شرایط نوری کاربر |
| 🌍 **دو زبانه** | فارسی (راست‌به‌چپ) و انگلیسی، به‌طور کامل |
| 🔤 **فونت قابل انتخاب** | وزیرمتن، ساحل، Montserrat، Inter، یا پیش‌فرض سیستم |
| 🖼️ **پیش‌نمایش عکس** | نمایش تمام‌صفحه با حفظ نسبت اصلی تصویر |
| ✨ **ظاهر شیشه‌ای** | Glassmorphism ظریف و قابل خاموش/روشن کردن |
| ⚡ **سبک و سریع** | بدون وابستگی‌های اضافی، بارگذاری آنی |

---

## 📱 نصب

۱. آخرین نسخه‌ی APK را از اینجا دانلود کنید:
**[github.com/alitanhaii1970-commits/game-settings-app/releases/latest](https://github.com/alitanhaii1970-commits/game-settings-app/releases/latest)**

۲. فایل را باز کنید. اگر اندروید هشدار «منبع نامشخص» داد:
`تنظیمات ← برنامه‌ها ← مجوزهای خاص ← نصب از منابع نامشخص` را فعال کنید — این یک هشدار استاندارد اندروید برای هر برنامه‌ی خارج از Google Play است، نه نشانه‌ی ناامن بودن.

۳. نصب کنید و از برنامه لذت ببرید 🎮

---

## 🏗 ساختار پروژه

```
game-settings-app/
├── app/
│   ├── src/main/kotlin/com/gamesettings/app/   # کد اصلی (Kotlin)
│   │   ├── MainActivity.kt                     # صفحه‌ی لیست بازی‌ها
│   │   ├── GameDetailActivity.kt                # صفحه‌ی جزئیات و تنظیمات
│   │   ├── ImagePreviewActivity.kt              # پیش‌نمایش تمام‌صفحه‌ی عکس
│   │   ├── SettingsActivity.kt                  # تنظیمات برنامه (تم/زبان/فونت)
│   │   ├── OnboardingActivity.kt                # مسیر خوش‌آمدگویی اولین اجرا
│   │   ├── GameRepository.kt                    # لایه‌ی ارتباط با Firestore
│   │   └── GlassBubblesView.kt                  # افکت تزئینی پس‌زمینه
│   └── src/main/res/                            # چیدمان‌ها، رنگ‌ها، فونت‌ها، آیکون‌ها
└── .github/workflows/                           # ساخت خودکار APK
```

**پشته‌ی فنی:**
- **زبان:** Kotlin
- **معماری رابط کاربری:** Android View system (XML) + RecyclerView
- **پایگاه‌داده:** Cloud Firestore (بلادرنگ، بدون نیاز به سرور اختصاصی)
- **بارگذاری تصویر:** Coil
- **مدیریت محتوا:** پنل وب اختصاصی (افزودن/ویرایش بازی‌ها بدون نیاز به کد)
- **CI/CD:** GitHub Actions — هر تغییر، به‌صورت خودکار build و منتشر می‌شه

---

## 🛠 توسعه

```bash
git clone https://github.com/alitanhaii1970-commits/game-settings-app.git
cd game-settings-app
```

پروژه را در Android Studio باز کنید و اجرا کنید (`Run ▶`)، یا از خط فرمان:

```bash
./gradlew assembleDebug
```

خروجی در مسیر `app/build/outputs/apk/debug/` قرار می‌گیرد.

---

## 🤝 مشارکت

برای گزارش باگ یا پیشنهاد قابلیت جدید، از بخش [Issues](https://github.com/alitanhaii1970-commits/game-settings-app/issues) استفاده کنید. Pull request‌ها هم با آغوش باز پذیرفته می‌شن.

## مجوز

این پروژه تحت مجوز MIT منتشر شده است.

---

<div align="center">

ساخته‌شده برای گیمرها ❤️

</div>
support in telegram:@Aloneix1
