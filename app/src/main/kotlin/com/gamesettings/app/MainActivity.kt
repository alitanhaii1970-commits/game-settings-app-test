package com.gamesettings.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var repository: GameRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GameAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var searchBox: EditText
    private lateinit var refreshButton: ImageButton
    private lateinit var settingsButton: ImageButton

    private var allGames: List<Game> = emptyList()
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // بار اول: هدایت به مسیر ورود اولیه (زبان → قدرت سیستم) پیش از نمایش لیست بازی‌ها
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

        repository = GameRepository()

        val rootView = findViewById<View>(android.R.id.content)
        recyclerView = findViewById(R.id.recycler_games)
        progressBar = findViewById(R.id.progress_bar)
        emptyText = findViewById(R.id.empty_text)
        searchBox = findViewById(R.id.search_box)
        refreshButton = findViewById(R.id.refresh_button)
        settingsButton = findViewById(R.id.settings_button)

        // فونت انتخابی کاربر را روی کل صفحه اعمال کن
        FontManager.applyToViewTree(this, rootView)

        // انیمیشن ورود ملایم کل صفحه هنگام باز شدن
        rootView.alpha = 0f
        rootView.animate().alpha(1f).setDuration(260).start()

        adapter = GameAdapter { game ->
            val intent = Intent(this, GameDetailActivity::class.java)
            intent.putExtra("name", game.name)
            intent.putExtra("imageUrl", game.imageUrl)
            intent.putExtra("settingsGreen", game.settingsGreen)
            intent.putExtra("settingsYellow", game.settingsYellow)
            intent.putExtra("youtubeUrl", game.youtubeUrl)
            intent.putExtra("showYoutubeButton", game.showYoutubeButton)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        refreshButton.setOnClickListener {
            if (isLoading) return@setOnClickListener // از چند درخواست هم‌زمان جلوگیری می‌کنه

            it.animate()
                .rotationBy(360f)
                .setDuration(500)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
            loadGames(forceServer = true)
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        searchBox.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
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

    private fun loadGames(forceServer: Boolean, cacheOnly: Boolean = false) {
        isLoading = true
        refreshButton.animate().alpha(0.4f).setDuration(150).start()
        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        repository.fetchGames(
            forceServer = forceServer,
            cacheOnly = cacheOnly,
            onSuccess = { games ->
                isLoading = false
                refreshButton.animate().alpha(1f).setDuration(200).start()
                progressBar.visibility = View.GONE
                allGames = games
                applyFilter(searchBox.text?.toString().orEmpty())
                if (forceServer) {
                    AppPreferences.markGamesLoaded(this)
                    Toast.makeText(this, "لیست به‌روز شد ✅", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { e ->
                isLoading = false
                refreshButton.animate().alpha(1f).setDuration(200).start()
                progressBar.visibility = View.GONE
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
     */
    private fun applyFilter(query: String) {
        val filtered = if (query.isBlank()) {
            allGames
        } else {
            allGames.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.submitList(filtered)

        emptyText.visibility = if (filtered.isEmpty() && allGames.isNotEmpty()) View.VISIBLE else View.GONE
        if (filtered.isEmpty() && allGames.isNotEmpty()) {
            emptyText.text = "بازی‌ای با این اسم پیدا نشد"
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
