package com.example.studenttaskmanager.data.repository

import com.example.studenttaskmanager.data.local.SubjectDao
import com.example.studenttaskmanager.data.local.UserDao
import com.example.studenttaskmanager.data.model.Subject
import com.example.studenttaskmanager.data.model.SubjectPalette
import com.example.studenttaskmanager.data.model.User
import com.example.studenttaskmanager.data.security.PasswordHasher
import com.example.studenttaskmanager.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Accounts: registration, login and lookup. Passwords are salted and hashed;
 * a new account receives the starter subject set.
 */
class UserRepository(
    private val userDao: UserDao,
    private val subjectDao: SubjectDao
) {

    suspend fun register(username: String, fullName: String, password: String): Result<User> = runCatching {
        val cleanUsername = username.trim()
        val cleanName = fullName.trim()
        require(cleanUsername.length >= 3) { "Username must be at least 3 characters." }
        require(cleanName.isNotEmpty()) { "Please enter your full name." }
        require(password.length >= 6) { "Password must be at least 6 characters." }
        if (userDao.findByUsername(cleanUsername) != null) {
            error("That username is already taken.")
        }

        val salt = PasswordHasher.randomSalt()
        // PBKDF2 is deliberately expensive: keep it off the UI dispatcher.
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password, salt) }
        val draft = User(
            username = cleanUsername,
            fullName = cleanName,
            passwordHash = hash,
            salt = salt,
            createdAt = DateUtils.timestamp()
        )
        val id = userDao.insert(draft)

        SubjectPalette.starterNames.forEachIndexed { index, name ->
            subjectDao.insert(
                Subject(
                    userId = id,
                    name = name,
                    color = SubjectPalette.all[index % SubjectPalette.all.size]
                )
            )
        }
        draft.copy(id = id)
    }

    suspend fun login(username: String, password: String): Result<User> = runCatching {
        val user = userDao.findByUsername(username.trim())
            ?: error("No account found with that username.")
        val matches = withContext(Dispatchers.Default) {
            PasswordHasher.verify(password, user.salt, user.passwordHash)
        }
        if (!matches) {
            error("Incorrect password. Try again.")
        }
        user
    }

    suspend fun getById(id: Long): User? = userDao.findById(id)

    fun observeById(id: Long): Flow<User?> = userDao.observeById(id)
}
