package com.gamesettings.app

import android.view.LayoutInflater
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
            // مهم: هر انیمیشن نیمه‌کاره‌ی قبلی را لغو و شفافیت/اندازه/عمق کارت را کامل می‌کنیم.
            // این ریست‌ها به‌عنوان محافظ باقی می‌مونن، حتی با این‌که دیگه خودمون انیمیشن
            // سفارشی لمسی نمی‌سازیم — اگر یه‌جای دیگه‌ی کد (فعلی یا آینده) این مقادیر رو
            // تغییر بده، این تضمین می‌کنه که کارت‌ها هیچ‌وقت با حالت نیمه‌کاره گیر نکنن.
            itemView.clearAnimation()
            itemView.animate().cancel()
            itemView.alpha = 1f
            itemView.scaleX = 1f
            itemView.scaleY = 1f
            itemView.translationZ = 0f

            // فونت کارت — هر بار bind می‌شه دوباره چک می‌شه، پس اگر کاربر
            // این تنظیم را در صفحه‌ی تنظیمات عوض کرده باشد، بلافاصله (حتی موقع اسکرول) اعمال می‌شود
            FontManager.applyToViewTree(itemView.context, itemView)

            // رنگ متن رو صریحاً از نو تنظیم می‌کنیم (محافظ اضافی، حتی اگه از قبل درست باشه)
            name.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.context, R.color.text_primary))
            name.text = game.name

            image.load(game.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
            }

            // فیدبک لمسی از طریق ریپل استاندارد خودِ اندروید (که در XML تعریف شده) —
            // عمداً دیگه انیمیشن سفارشی scale/translationZ نمی‌سازیم، چون منبع
            // یک باگ تکرارشونده بود (اگه لمس وسط ناوبری/اسکرول قطع می‌شد، مقدار
            // نیمه‌کاره می‌موند). ریپل native هیچ‌وقت این‌طوری گیر نمی‌کنه.
            itemView.setOnClickListener { onClick(game) }
        }
    }
}
