package com.gamesettings.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : BaseActivity() {

    private lateinit var repository: GameRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameAdapter
    private lateinit var skeleton: View
    private lateinit var emptyText: TextView
    private lateinit var searchBox: EditText
    private lateinit var refreshButton: ImageButton
    private lateinit var settingsButton: ImageButton

    private var allGames: List<Game> = emptyList()
    private var isLoading = false

    /** فقط وقتی صفحه تازه باز می‌شود (نه بعد از عوض‌شدن تم/زبان) انیمیشن‌های ورود اجرا می‌شوند */
    private var playEntrance = true

    private var spinAnimator: ObjectAnimator? = null
    private var skeletonPulse: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // بار اول: هدایت به مسیر ورود اولیه (زبان → ظاهر → قدرت سیستم) پیش از نمایش لیست بازی‌ها
        if (!AppPreferences.isOnboardingDone(this)) {
            super.onCreate(savedInstanceState)
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        // اعمال زبان ذخیره‌شده کاربر پیش از رسم صفحه
        AppPreferences.applyLanguage(AppPreferences.getLanguage(this))

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playEntrance = savedInstanceState == null

        repository = GameRepository()

        val rootView = findViewById<View>(android.R.id.content)
        recyclerView = findViewById(R.id.recycler_games)
        skeleton = findViewById(R.id.skeleton_list)
        emptyText = findViewById(R.id.empty_text)
        searchBox = findViewById(R.id.search_box)
        refreshButton = findViewById(R.id.refresh_button)
        settingsButton = findViewById(R.id.settings_button)

        // فونت انتخابی کاربر را روی کل صفحه اعمال کن
        FontManager.applyToViewTree(this, rootView)

        // ورود نرم نوار بالا و جستجو (بالا → پایین، پشت سر هم)
        if (playEntrance) {
            Motion.riseIn(findViewById(R.id.header_bar), 0L, 10, 380L)
            Motion.riseIn(searchBox, 70L, 10, 380L)
        }

        adapter = GameAdapter { game ->
            val intent = Intent(this, GameDetailActivity::class.java)
            intent.putExtra("name", game.name)
            intent.putExtra("imageUrl", game.imageUrl)
            intent.putExtra("settingsGreen", game.settingsGreen)
            intent.putExtra("settingsYellow", game.settingsYellow)
            intent.putExtra("youtubeUrl", game.youtubeUrl)
            intent.putExtra("showYoutubeButton", game.showYoutubeButton)
            startActivity(intent)
            Motion.pushForward(this)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        refreshButton.setOnClickListener {
            if (isLoading) return@setOnClickListener // از چند درخواست هم‌زمان جلوگیری می‌کنه
            loadGames(forceServer = true)
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            Motion.pushForward(this)
        }

        searchBox.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty(), animate = false)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // فقط اولین‌بارِ واقعی (بعد از اولین نصب/onboarding) خودکار از سرور می‌خونه.
        // دفعات بعدی، فقط از حافظه‌ی محلی نشون می‌ده — تا خودش بخواد، دکمه‌ی رفرش رو بزنه.
        if (AppPreferences.hasEverLoadedGames(this)) {
            loadGames(forceServer = false, cacheOnly = true)
        } else {
            loadGames(forceServer = true)
        }
    }

    override fun onResume() {
        super.onResume()
        // اگر کاربر از صفحه تنظیمات برگشته و فونت را عوض کرده، ظاهر لیست را به‌روز کن
        FontManager.applyToViewTree(this, findViewById(android.R.id.content))
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        spinAnimator?.cancel()
        skeletonPulse?.cancel()
    }

    private fun loadGames(forceServer: Boolean, cacheOnly: Boolean = false) {
        isLoading = true
        startSpin()
        if (allGames.isEmpty()) showSkeleton(true)
        emptyText.visibility = View.GONE

        repository.fetchGames(
            forceServer = forceServer,
            cacheOnly = cacheOnly,
            onSuccess = { games ->
                isLoading = false
                stopSpin()
                showSkeleton(false)
                allGames = games
                applyFilter(searchBox.text?.toString().orEmpty(), animate = playEntrance || forceServer)
                if (forceServer) {
                    AppPreferences.markGamesLoaded(this)
                    Toast.makeText(this, "لیست به‌روز شد ✅", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { e ->
                isLoading = false
                stopSpin()
                showSkeleton(false)
                if (cacheOnly) {
                    // حافظه‌ی محلی خالی بود (مثلاً حذف داده‌های اپ) — یک‌بار از سرور تلاش کن
                    loadGames(forceServer = true)
                    return@fetchGames
                }
                if (allGames.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = "اتصال به اینترنت برقرار نیست.\nلیست قبلی موجود نیست."
                } else {
                    Toast.makeText(this, "اتصال برقرار نشد، لیست قبلی نشون داده می‌شه", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    /**
     * فیلتر لیست بر اساس جستجو و نمایش نتیجه.
     * [animate] = true یعنی کارت‌ها پلکانی وارد شوند (فقط بعد از بارگذاری، نه موقع تایپ).
     */
    private fun applyFilter(query: String, animate: Boolean) {
        val filtered = if (query.isBlank()) {
            allGames
        } else {
            allGames.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.submitList(filtered, animateEntrance = animate && query.isBlank())

        emptyText.visibility = if (filtered.isEmpty() && allGames.isNotEmpty()) View.VISIBLE else View.GONE
        if (filtered.isEmpty() && allGames.isNotEmpty()) {
            emptyText.text = "بازی‌ای با این اسم پیدا نشد"
        }
    }

    // ───────────── انیمیشن‌ها ─────────────

    /** دکمه‌ی رفرش تا وقتی بارگذاری ادامه دارد می‌چرخد و بعد نرم می‌ایستد */
    private fun startSpin() {
        refreshButton.animate().cancel()
        refreshButton.animate().alpha(0.55f).setDuration(150).start()
        if (Motion.reduced(this)) return
        spinAnimator?.cancel()
        spinAnimator = ObjectAnimator.ofFloat(refreshButton, View.ROTATION, 0f, 360f).apply {
            duration = 800L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopSpin() {
        spinAnimator?.cancel()
        spinAnimator = null
        refreshButton.animate().cancel()
        val remaining = 360f - (refreshButton.rotation % 360f)
        refreshButton.animate()
            .alpha(1f)
            .rotationBy(remaining)
            .setDuration((remaining / 360f * 420f).toLong() + 120L)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction { refreshButton.rotation = 0f }
            .start()
    }

    /** اسکلت بارگذاری: کارت‌های خالیِ نبض‌دار تا رسیدن داده */
    private fun showSkeleton(show: Boolean) {
        if (show) {
            skeleton.animate().cancel()
            skeleton.alpha = 1f
            if (skeleton.visibility == View.VISIBLE) return
            skeleton.visibility = View.VISIBLE
            if (Motion.reduced(this)) return
            skeletonPulse?.cancel()
            skeletonPulse = ObjectAnimator.ofFloat(skeleton, View.ALPHA, 1f, 0.45f).apply {
                duration = 850L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                start()
            }
        } else {
            if (skeleton.visibility != View.VISIBLE) return
            skeletonPulse?.cancel()
            skeletonPulse = null
            skeleton.animate().alpha(0f).setDuration(180L).withEndAction {
                skeleton.visibility = View.GONE
                skeleton.alpha = 1f
            }.start()
        }
    }

    override fun finish() {
        super.finish()
        Motion.popBack(this)
    }
}
