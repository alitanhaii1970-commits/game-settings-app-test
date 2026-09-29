package com.gamesettings.app

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class GameAdapter(
    private val onClick: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    private var items: List<Game> = emptyList()

    fun submitList(newItems: List<Game>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GameViewHolder(
        itemView: android.view.View,
        private val onClick: (Game) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView = itemView.findViewById(R.id.game_image)
        private val name: TextView = itemView.findViewById(R.id.game_name)

        fun bind(game: Game) {
            // مهم: هر انیمیشن نیمه‌کاره‌ی قبلی (مثلاً وقتی کاربر وسط انیمیشن ورود لیست، صفحه رو ترک کرده)
            // را لغو و شفافیت/اندازه‌ی کارت را به حالت کامل برمی‌گردانیم — تا کارت‌ها محو یا کوچک نمانند.
            itemView.clearAnimation()
            itemView.animate().cancel()
            itemView.alpha = 1f
            itemView.scaleX = 1f
            itemView.scaleY = 1f

            // ظاهر شیشه‌ای و فونت کارت — هر بار bind می‌شه دوباره چک می‌شن
            GlassStyler.applyCard(itemView.context, itemView)
            FontManager.applyToViewTree(itemView.context, itemView)

            name.text = game.name
            image.load(game.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
            }

            itemView.setOnClickListener { onClick(game) }

            // انیمیشن فشرده‌شدن ملایم کارت هنگام لمس
            itemView.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(120).start()
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.animate().scaleX(1f).scaleY(1f).setDuration(160).start()
                    }
                }
                false
            }
        }
    }
}
