package com.gamesettings.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
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

    override fun onCreate(savedInstanceState: Bundle?) {
        // بار اول: هدایت به مسیر ورود اولیه (زبان → تم → شیشه‌ای) پیش از نمایش لیست بازی‌ها
        if (!AppPreferences.isOnboardingDone(this)) {
            super.onCreate(savedInstanceState)
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        // اعمال زبان و تم ذخیره‌شده کاربر پیش از رسم صفحه
        AppPreferences.applyLanguage(AppPreferences.getLanguage(this))
        AppPreferences.applyTheme(AppPreferences.getTheme(this))

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

        // ظاهر شیشه‌ای (در صورت فعال بودن) روی عناصر کارت‌مانند
        GlassStyler.applySearchBox(this, searchBox)
        GlassStyler.applyRoundButton(this, refreshButton)
        GlassStyler.applyRoundButton(this, settingsButton)

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
            it.animate()
                .rotationBy(360f)
                .setDuration(450)
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
                applyFilter(s?.toString().orEmpty(), animate = false)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // اولین بار: اول از حافظه محلی (سریع)، بعد خودش سعی می‌کنه سینک کنه
        loadGames(forceServer = false)
    }

    override fun onResume() {
        super.onResume()
        // اگر کاربر از صفحه تنظیمات برگشته و حالت شیشه‌ای/تم/فونت را عوض کرده، ظاهر لیست را به‌روز کن
        FontManager.applyToViewTree(this, findViewById(android.R.id.content))
        GlassStyler.applySearchBox(this, searchBox)
        GlassStyler.applyRoundButton(this, refreshButton)
        GlassStyler.applyRoundButton(this, settingsButton)
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun loadGames(forceServer: Boolean) {
        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        repository.fetchGames(
            forceServer = forceServer,
            onSuccess = { games ->
                progressBar.visibility = View.GONE
                allGames = games
                applyFilter(searchBox.text?.toString().orEmpty(), animate = true)
                if (forceServer) {
                    Toast.makeText(this, "لیست به‌روز شد ✅", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { e ->
                progressBar.visibility = View.GONE
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
     * انیمیشن ورود ردیفی فقط زمانی اجرا می‌شود که داده‌ی تازه‌ای لود شده باشد (animate=true)،
     * نه به‌ازای هر ضربه کیبورد در جستجو — تا تایپ کردن نرم و بدون لرزش بماند.
     */
    private fun applyFilter(query: String, animate: Boolean) {
        val filtered = if (query.isBlank()) {
            allGames
        } else {
            allGames.filter { it.name.contains(query, ignoreCase = true) }
        }
        adapter.submitList(filtered)

        if (animate) {
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_games)
            recyclerView.scheduleLayoutAnimation()
        }

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
