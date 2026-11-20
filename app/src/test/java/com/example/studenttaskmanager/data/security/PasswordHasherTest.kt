package com.example.studenttaskmanager.data.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class PasswordHasherTest {

    @Test
    fun `salt is 16 random bytes encoded as base64`() {
        val salt = PasswordHasher.randomSalt()
        assertEquals(16, Base64.getDecoder().decode(salt).size)
        assertNotEquals(salt, PasswordHasher.randomSalt())
    }

    @Test
    fun `hashing is deterministic for the same salt`() {
        val salt = PasswordHasher.randomSalt()
        assertEquals(
            PasswordHasher.hash("correct horse battery staple", salt),
            PasswordHasher.hash("correct horse battery staple", salt)
        )
    }

    @Test
    fun `different salts produce different hashes`() {
        val first = PasswordHasher.hash("studydeck", PasswordHasher.randomSalt())
        val second = PasswordHasher.hash("studydeck", PasswordHasher.randomSalt())
        assertNotEquals(first, second)
    }

    @Test
    fun `verify accepts the right password and rejects others`() {
        val salt = PasswordHasher.randomSalt()
        val hash = PasswordHasher.hash("Deadline2026", salt)

        assertTrue(PasswordHasher.verify("Deadline2026", salt, hash))
        assertFalse(PasswordHasher.verify("deadline2026", salt, hash))
        assertFalse(PasswordHasher.verify("", salt, hash))
    }

    @Test
    fun `hashes never contain the plain password`() {
        val hash = PasswordHasher.hash("MySecret123", PasswordHasher.randomSalt())
        assertFalse(hash.contains("MySecret123"))
    }
}
