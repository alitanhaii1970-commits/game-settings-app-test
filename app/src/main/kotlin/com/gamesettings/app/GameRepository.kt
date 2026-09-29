package com.gamesettings.app

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

/**
 * تمام ارتباط با Firestore از اینجا انجام میشه.
 * Firestore خودش داده‌ها رو محلی cache می‌کنه، برای همین اپ آفلاین هم
 * آخرین لیستی که قبلاً گرفته رو نشون میده.
 */
class GameRepository {

    private val db = FirebaseFirestore.getInstance()
    private val gamesCollection = db.collection("games")

    /**
     * لیست بازی‌ها رو می‌گیره.
     * forceServer = true یعنی حتماً از اینترنت بخونه (دکمه‌ی رفرش، یا اولین‌بار
     * که برنامه اجرا می‌شه).
     * cacheOnly = true یعنی فقط از حافظه‌ی محلی بخونه و اصلاً به سرور سر نزنه —
     * برای باز شدن‌های عادی بعد از اولین‌بار، تا مصرف Firestore کم بشه. کاربر با
     * زدن دکمه‌ی رفرش، خودش می‌تونه هر وقت خواست لیست تازه رو بگیره.
     */
    fun fetchGames(
        forceServer: Boolean,
        cacheOnly: Boolean = false,
        onSuccess: (List<Game>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val source = when {
            forceServer -> Source.SERVER
            cacheOnly -> Source.CACHE
            else -> Source.DEFAULT
        }

        gamesCollection
            .get(source)
            .addOnSuccessListener { snapshot ->
                val games = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Game::class.java)?.apply { id = doc.id }
                }.sortedBy { it.name.lowercase() }
                onSuccess(games)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
