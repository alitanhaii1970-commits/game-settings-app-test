# 🎮 PC Max — Game Optimal Settings App

> **بهترین تنظیمات بازی‌های محبوب در یک جا**

PC Max یک اپلیکیشن اندروید است که کاربران را کمک می‌کند تا بهترین تنظیمات گرافیکی و کارایی را برای صدها بازی محبوب پیدا کنند. تنظیمات توسط متخصصین بازی به دو دسته تقسیم شده‌اند:
- **تنظیمات سبز:** بهترین FPS و کارایی
- **تنظیمات زرد:** بهترین کیفیت گرافیک

---

## ✨ ویژگی‌های اصلی

- 🎯 **۵۰۰+ بازی** — بیش از ۵۰۰ بازی محبوب پشتیبانی‌شده
- 📱 **دو زبان** — فارسی و انگلیسی
- 🌙 **تم روشن/تیره** — دید راحت‌تر هر وقت
- 🎨 **۵ فونت پیشنهادی** — وزیرمتن، ساحل، Montserrat، Inter
- 🎥 **راهنمای ویدیویی** — برای برخی بازی‌ها
- 🖼️ **عکس تمام‌صفحه** — مشاهده بهتر تنظیمات
- ⚡ **سریع** — بارگذاری < ۴۰۰ میلی‌ثانیه
- 📴 **کار بدون اینترنت** — داده محلی cache شده

---

## 📥 نصب

### روش 1: دانلود مستقیم APK
1. به [Release Page](https://github.com/alitanhaii1970-commits/game-settings-app/releases) بروید
2. آخرین `app-debug.apk` را دانلود کنید
3. فایل `.apk` را باز کنید (یا روی آن دوبار کلیک کنید)
4. اگر مسئله امنیتی پیدا شد، [اینجا](#مسئله-امنیتی-هنگام-نصب-apk) ببینید

### روش 2: درون Google Play Store
⏳ **در حال آماده‌سازی برای ارائه رسمی به Google Play Store**

---

## ⚠️ مسئله امنیتی هنگام نصب APK

وقتی فایل `.apk` را نصب می‌کنید، Android ممکن است هشداری مثل این نشان دهد:
> "برای مدیریت نصب از دستگاه، اجازه دهید این اپلیکیشن"

### چرا؟
- APK از **منبع نامشخص** نیست (یعنی از Google Play Store نیست)
- Android برای حفاظت از کاربر، هشدار می‌دهد

### حل:
1. **تنظیمات** → **برنامه‌ها و اطلاعات دستگاه**
2. **نصب از منابع نامشخص** یا **منابع غیرمعتبر** را فعال کنید
3. مجدداً APK را باز کنید
4. **نصب** را بزنید

**نکته:** این APK ۱۰۰% امن است — فقط Android احتیاطی می‌کند چون از مجموعه رسمی Google نیست.

---

## 🍎 آیا برای iOS (اپل) وجود دارد؟

### پاسخ مختصر: **نه، اما اینجا گزینه‌هایی هستند:**

#### گزینه 1: **وب‌سایت (Free)**
PC Max را به عنوان یک **سایت وب** می‌توانید استفاده کنید — هیچ نصبی لازم نیست!

**چطور:**
1. صفحه‌ی [pc-max.web.app](https://pc-max.web.app) را باز کنید (وقتی آماده بشه)
2. **هیچ دانلوادی لازم نیست** — درحال کار می‌کند
3. می‌تونید به **Home Screen** اضافه کنید (مثل یک اپ)

**مزایا:**
- ✅ رایگان
- ✅ برای iOS/Android/Windows/Mac
- ✅ همیشه آپ‌دیت شده
- ✅ بدون نگرانی از اندازه فایل

#### گزینه 2: **اپ iOS نیتیو (هزینه‌دار)**
اگر دقیقاً می‌خواهید یک اپ iOS اصلی:

**چرا سخت است:**
- Apple برای ارائه در App Store **حق رسمی** می‌گیرد (معمولاً **۹۹ دلار/سال**)
- باید **Xcode** (نرم‌افزار توسعه اپل) استفاده کنید
- کد کاملاً متفاوت است (Kotlin → Swift)
- تایید Apple **۲-۴ روز** طول می‌کشد

**هزینه:**
| آیتم | قیمت |
|-----|------|
| Apple Developer Account | $99/سال |
| توسعه دوباره (Kotlin → Swift) | $۱۵۰۰-۳۰۰۰ |
| **کل** | **~$۱۶۰۰-۳۱۰۰** |

#### گزینه 3: **Flutter (توصیه شده)**
اگر بخواهید **یک‌بار کد** و برای **iOS + Android + Web**:

**چطور:**
- PC Max را به **Flutter** منتقل کنید
- یک‌بار برنامه‌ریزی کنید، سه جایت اجرا شود
- برای iOS: $99/سال Apple Developer
- برای Android: بدون هزینه

**زمان:** ۲-۳ ماه کار توسعه

---

## 🌐 سایت رایگان درست کنم چطور؟

### روش 1: **Firebase Hosting** (توصیه شده - تماماً رایگان)

```bash
# ۱. نصب Firebase CLI
npm install -g firebase-tools

# ۲. وارد شوید
firebase login

# ۳. پروژه را مقداردهی کنید
firebase init hosting

# ۴. بسازید
npm run build

# ۵. آپلود کنید
firebase deploy
```

**نتیجه:** سایت شما در `https://pc-max-XXXXX.web.app` فعال است

**هزینه:** ۰ دلار (تا ۱۰ GB ترافیک/ماه رایگان)

### روش 2: **Vercel** (خیلی آسان - رایگان)

```bash
npm install -g vercel
vercel login
vercel
```

**نتیجه:** سایت آپلود شده و آپ‌دیت خودکار

**هزینه:** ۰ دلار

### روش 3: **Netlify** (رایگان - بسیار ساده)

1. `https://netlify.com` را باز کنید
2. GitHub repository را وصل کنید
3. **Deploy** را بزنید
4. تمام!

**نتیجه:** هر بار push، سایت خودکار آپ‌دیت می‌شود

---

## 🔧 درست کردن سایت (من خودم)

### مراحل:

**۱. React یا Vue اپلیکیشن بسازید:**
```bash
npx create-react-app pc-max-web
cd pc-max-web
npm install
```

**۲. PC Max کارت‌ها را اضافه کنید:**
```jsx
import React, { useState, useEffect } from 'react';
import { getGames } from './firebaseConfig';

export default function GameList() {
  const [games, setGames] = useState([]);

  useEffect(() => {
    getGames().then(setGames);
  }, []);

  return (
    <div className="grid gap-4">
      {games.map(game => (
        <GameCard key={game.id} game={game} />
      ))}
    </div>
  );
}
```

**۳. Firebase وصل کنید:**
```js
import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';

const app = initializeApp({
  apiKey: process.env.REACT_APP_API_KEY,
  projectId: "pc-max-a0a4b",
  // ...
});

export const db = getFirestore(app);
```

**۴. آپلود کنید:**
```bash
npm run build
firebase deploy
```

---

## 📋 مقایسه: اندروید vs iOS vs وب

| ویژگی | اندروید | iOS | وب |
|------|---------|-----|-----|
| **هزینه اولیه** | رایگان | $99/سال | رایگان |
| **نصب** | APK یا Google Play | App Store | بدون نصب |
| **توسعه** | Kotlin (موجود) | Swift (جدید) | React/Vue (جدید) |
| **زمان ساخت** | ✅ تمام شده | ۲-۳ ماه | ۱-۲ هفته |
| **کاربران** | ✅ میلیون‌ها | معتدل | جهانی (همه دستگاه‌ها) |
| **بهترین گزینه** | ✅ فعلاً | کند/گران | ✅ توصیه شده |

---

## 🚀 راه‌نمای شروع توسعه

### اگر می‌خواهید خود توسعه دهید:

**الزامات:**
- Git (`git --version`)
- Android Studio (برای اندروید)
- یا Node.js (برای وب)

**Clone کنید:**
```bash
git clone https://github.com/alitanhaii1970-commits/game-settings-app.git
cd game-settings-app
```

**اندروید:**
```bash
# Android Studio را باز کنید
# Build → Make Project
# Run
```

**وب (اگر تبدیل شد):**
```bash
npm install
npm start
```

---

## 🤝 مشارکت

برای اضافه کردن بازی جدید یا بهبود:
1. Fork کنید
2. Branch جدید بسازید (`git checkout -b feature/new-game`)
3. Commit کنید (`git commit -m "Add new game"`)
4. Push کنید (`git push origin feature/new-game`)
5. Pull Request کنید

---

## 📞 سوالات متداول

**Q: چرا Kotlin به جای Java؟**
A: Kotlin جدیدتر، ایمن‌تر، و کد کمتری لازم دارد.

**Q: چه زمانی در iOS منتشر می‌شود؟**
A: اگر بودجه‌ای برای iOS وجود داشته باشد، ۲-۳ ماه بعد.

**Q: آیا می‌توانم بازی‌های خود را اضافه کنم؟**
A: بله! Admin Panel استفاده کنید یا Pull Request بدهید.

**Q: چطور بازی‌ها به‌روز می‌شوند؟**
A: از Firestore مستقیماً — نیازی به نسخه جدید نیست.

---

## 📄 مجوز

PC Max تحت **MIT License** است — آزادانه استفاده، تغییر، و توزیع کنید.

---

## 👤 نویسنده

**توسعه‌یافته توسط:** [alitanhaii](https://github.com/alitanhaii1970-commits)

---

## 📊 آمار

- **مخزن کد:** ~۳۵۰۰ خط (Kotlin + XML + HTML)
- **بازی‌های پشتیبانی‌شده:** ۵۰۰+
- **فونت‌های دریافتی:** ۵ (Vazirmatn, Sahel, Montserrat, Inter, System)
- **زبان‌ها:** ۲ (فارسی، انگلیسی)
- **حداقل API:** 24 (Android 7.0)
- **نسخه هدف:** 34 (Android 14)

---

**اگر سوال یا پیشنهادی دارید، GitHub Issues را باز کنید! 🚀**

