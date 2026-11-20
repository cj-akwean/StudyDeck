package com.example.studenttaskmanager.data.repository

import com.example.studenttaskmanager.data.local.SubjectDao
import com.example.studenttaskmanager.data.model.Subject
import kotlinx.coroutines.flow.Flow

/**
 * Subject tags: user-defined, coloured, one per user.
 */
class SubjectRepository(private val subjectDao: SubjectDao) {

    fun observeForUser(userId: Long): Flow<List<Subject>> = subjectDao.observeForUser(userId)

    suspend fun getForUser(userId: Long): List<Subject> = subjectDao.getForUser(userId)

    suspend fun add(userId: Long, name: String, color: Int): Long =
        subjectDao.insert(Subject(userId = userId, name = name.trim(), color = color))

    suspend fun delete(id: Long) = subjectDao.deleteById(id)
}
