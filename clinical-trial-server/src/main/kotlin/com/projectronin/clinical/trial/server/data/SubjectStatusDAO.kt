package com.projectronin.clinical.trial.server.data

import com.projectronin.clinical.trial.server.data.binding.SubjectStatusDOs
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.inList
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Repository
class SubjectStatusDAO(private val database: Database) {

    /**
     * Retrieves list of all [SubjectStatusDO]s
     */
    fun getSubjectStatus(): List<SubjectStatusDO> {
        return database.from(SubjectStatusDOs).select()
            .map { SubjectStatusDOs.createEntity(it) }
    }

    /**
     * Retrieves one [SubjectStatusDO] by Subject ID
     */
    fun getSubjectStatusBySubjectId(subjectId: String): List<SubjectStatusDO> {
        return database.from(SubjectStatusDOs).select()
            .where {
                (SubjectStatusDOs.subjectId eq subjectId)
            }
            .map { SubjectStatusDOs.createEntity(it) }
    }

    /**
     * Retrieves list of [SubjectStatusDO]s based on a list of SubjectStatus values
     */
    fun getSubjectsByStatus(statuses: List<SubjectStatus>): List<SubjectStatusDO> {
        return database.from(SubjectStatusDOs).select()
            .where {
                (SubjectStatusDOs.status inList statuses)
            }.map { SubjectStatusDOs.createEntity(it) }
    }

    /**
     * Retrieves list of [SubjectStatusDO]s for a StudySite [UUID] and a list of SubjectStatus values
     */
    fun getSubjectStatusByStudySiteAndStatus(studySiteId: UUID, statuses: List<SubjectStatus>): List<SubjectStatusDO> {
        return database.from(SubjectStatusDOs).select()
            .where {
                (SubjectStatusDOs.status inList statuses) and (SubjectStatusDOs.studySiteId eq studySiteId)
            }.map { SubjectStatusDOs.createEntity(it) }
    }

    /**
     * Insert [SubjectStatusDO] and returns pair of [UUID] from study-site and [SubjectId]
     */
    fun insertSubjectStatus(subjectStatusDO: SubjectStatusDO): Pair<UUID, String> {
        database.insert(SubjectStatusDOs) {
            set(it.studySiteId, subjectStatusDO.studySiteId)
            set(it.subjectId, subjectStatusDO.subjectId)
            set(it.status, subjectStatusDO.status)
            set(it.createdDateTime, OffsetDateTime.now(ZoneOffset.UTC))
        }

        return Pair(subjectStatusDO.studySiteId, subjectStatusDO.subjectId)
    }

    /**
     * Update Subject Status, automatically update the updatedDateTime on the record
     */
    fun updateSubjectStatus(studySiteId: UUID, subjectId: String, updateFunction: (SubjectStatusDO) -> Unit): SubjectStatusDO? {
        val subjectStatus = database.from(SubjectStatusDOs)
            .select()
            .where { (SubjectStatusDOs.subjectId eq subjectId) and (SubjectStatusDOs.studySiteId eq studySiteId) }
            .map { SubjectStatusDOs.createEntity(it) }.singleOrNull()

        subjectStatus?.let {
            updateFunction(subjectStatus)
            subjectStatus.updatedDateTime = OffsetDateTime.now(ZoneOffset.UTC)
            subjectStatus.flushChanges()
        }
        return subjectStatus
    }
}
