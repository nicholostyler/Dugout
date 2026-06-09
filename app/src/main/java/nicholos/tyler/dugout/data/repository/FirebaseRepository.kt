package nicholos.tyler.dugout.data.repository

import android.content.Context
import android.provider.Settings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging

class DugoutFirebaseRepository(
    private val context: Context
) {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    fun initializeUser() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            createOrUpdateUser(currentUser.uid)
            registerDevice(currentUser.uid)
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                createOrUpdateUser(uid)
                registerDevice(uid)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun createOrUpdateUser(uid: String) {
        val userData = mapOf(
            "favoriteTeamId" to 143,
            "tier" to "personal",
            "notificationsEnabled" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val createOnlyData = mapOf(
            "createdAt" to FieldValue.serverTimestamp()
        )

        val userRef = db.collection("users").document(uid)

        userRef.set(userData, SetOptions.merge())

        userRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.contains("createdAt")) {
                    userRef.set(createOnlyData, SetOptions.merge())
                }
            }
    }

    fun registerDevice(uid: String, token: String? = null) {
        if (token != null) {
            saveDeviceToken(uid, token)
        } else {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { fetchedToken ->
                    saveDeviceToken(uid, fetchedToken)
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    private fun saveDeviceToken(uid: String, token: String) {
        val deviceId = getDeviceId()

        val deviceData = mapOf(
            "platform" to "android",
            "enabled" to true,
            "fcmToken" to token,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val createOnlyData = mapOf(
            "createdAt" to FieldValue.serverTimestamp()
        )

        val deviceRef = db.collection("users")
            .document(uid)
            .collection("devices")
            .document(deviceId)

        deviceRef.set(deviceData, SetOptions.merge())

        deviceRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.contains("createdAt")) {
                    deviceRef.set(createOnlyData, SetOptions.merge())
                }
            }
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}