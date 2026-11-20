package com.example.studenttaskmanager.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted password hashing with PBKDF2-HMAC-SHA256 (documentation pack section 2.6).
 * Plain-text passwords are never written to the database.
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun randomSalt(): String {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(password: String, saltBase64: String): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            Base64.getDecoder().decode(saltBase64),
            ITERATIONS,
            KEY_LENGTH_BITS
        )
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            val encoded = factory.generateSecret(spec).encoded
            Base64.getEncoder().encodeToString(encoded)
        } finally {
            spec.clearPassword()
        }
    }

    fun verify(password: String, saltBase64: String, expectedHash: String): Boolean {
        val actual = hash(password, saltBase64)
        return MessageDigest.isEqual(actual.toByteArray(Charsets.UTF_8), expectedHash.toByteArray(Charsets.UTF_8))
    }
}
