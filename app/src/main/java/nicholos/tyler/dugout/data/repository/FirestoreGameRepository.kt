package nicholos.tyler.dugout.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import nicholos.tyler.dugout.model.domain.CachedGame

class FirestoreGamesRepository {
    private val db = Firebase.firestore

    fun observeTodayGames(): Flow<List<CachedGame>> = callbackFlow {
        val listener = db.collection("games")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val games = snapshot
                    ?.documents
                    ?.mapNotNull { it.toObject(CachedGame::class.java) }
                    ?.sortedBy { it.gameDate }
                    ?: emptyList()

                trySend(games)
            }

        awaitClose {
            listener.remove()
        }
    }
}
