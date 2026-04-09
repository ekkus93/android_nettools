package dev.nettools.android.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure credential storage backed by [EncryptedSharedPreferences] and the Android Keystore.
 * Passwords are **never** logged or included in exception messages.
 *
 * @param context Application context injected by Hilt.
 */
@Singleton
class CredentialStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        private const val FILE_NAME = "nettools_credentials"
        private const val MASTER_KEY_ALIAS = "nettools_master_key"
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Persists a password for the given profile ID in encrypted storage.
     * The password value is never written to any log.
     *
     * @param profileId UUID of the connection profile.
     * @param password The password to encrypt and store.
     */
    fun savePassword(profileId: String, password: String) {
        prefs.edit().putString(profileId, password).apply()
    }

    /**
     * Retrieves the stored password for the given profile ID.
     *
     * @param profileId UUID of the connection profile.
     * @return The decrypted password, or null if not found.
     */
    fun getPassword(profileId: String): String? =
        prefs.getString(profileId, null)

    /**
     * Removes the stored password for the given profile ID.
     *
     * @param profileId UUID of the connection profile.
     */
    fun deletePassword(profileId: String) {
        prefs.edit().remove(profileId).apply()
    }
}
