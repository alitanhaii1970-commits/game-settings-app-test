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
     * forceServer = true یعنی دکمه Refresh زده شده و باید حتماً از اینترنت بخونه.
     * forceServer = false یعنی اول از cache محلی بخون (سریع‌تر و آفلاین هم کار می‌کنه).
     */
    fun fetchGames(
        forceServer: Boolean,
        onSuccess: (List<Game>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val source = if (forceServer) Source.SERVER else Source.DEFAULT

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
