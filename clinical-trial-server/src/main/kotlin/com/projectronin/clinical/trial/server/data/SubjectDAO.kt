package com.projectronin.clinical.trial.server.data

import com.projectronin.clinical.trial.server.data.binding.StudySiteDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectStatusDOs
import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import com.projectronin.clinical.trial.server.data.model.SubjectDO
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.inList
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.springframework.stereotype.Repository

@Repository
class SubjectDAO(private val database: Database) {
    /**
     * get all subjects
     */
    fun getSubjects(): List<SubjectDO> {
        return database.from(SubjectDOs).select().map { SubjectDOs.createEntity(it) }
    }

    /**
     * get Ronin FHIR ID for one subject
     */
    fun getFhirIdBySubject(subject: String): String? {
        return database.from(SubjectDOs).select(SubjectDOs.roninPatientId)
            .where(SubjectDOs.subjectId eq subject).map { SubjectDOs.createEntity(it) }.firstOrNull()?.roninPatientId
    }

    /**
     * Retrieves list of Ronin FHIR Ids for a list of SubjectStatus values
     */
    fun getFhirIdsByStatus(statuses: List<SubjectStatus>): Set<String> {
        return database.from(SubjectDOs)
            .innerJoin(SubjectStatusDOs, SubjectDOs.subjectId eq SubjectStatusDOs.subjectId)
            .select()
            .where(SubjectStatusDOs.status inList statuses)
            .map { SubjectDOs.createEntity(it).roninPatientId }.toSet()
    }

    /**
     * get subject for Ronin FHIR Id
     */
    fun getSubjectByFhirId(fhirId: String): String? {
        return database.from(SubjectDOs).select(SubjectDOs.subjectId)
            .where(SubjectDOs.roninPatientId eq fhirId).map { SubjectDOs.createEntity(it) }.firstOrNull()?.subjectId
    }

    /**
     * get subject id and number for Ronin FHIR Id
     */
    fun getFullSubjectByFhirId(fhirId: String): SubjectDO? {
        return database.from(SubjectDOs).select()
            .where(SubjectDOs.roninPatientId eq fhirId).map { SubjectDOs.createEntity(it) }.firstOrNull()
    }

    /**
     * Insert subject
     */
    fun insertSubject(subjectDO: SubjectDO): String {
        database.insert(SubjectDOs) {
            set(it.subjectId, subjectDO.subjectId)
            set(it.roninPatientId, subjectDO.roninPatientId)
            set(it.subjectNumber, subjectDO.subjectNumber)
        }
        return subjectDO.subjectId
    }

    /**
     * get active Subject, with corresponding SubjectStatus and StudySite for given Ronin FHIR ID
     */
    fun getSubjectByRoninFhirId(fhirId: String): Triple<SubjectDO, SubjectStatusDO, StudySiteDO>? {
        return database.from(SubjectDOs).innerJoin(SubjectStatusDOs, SubjectDOs.subjectId eq SubjectStatusDOs.subjectId).innerJoin(
            StudySiteDOs,
            SubjectStatusDOs.studySiteId eq StudySiteDOs.studySiteId,
        ).select().where { (SubjectDOs.roninPatientId eq fhirId) and (SubjectStatusDOs.status eq SubjectStatus.ACTIVE) }
            .map {
                val subjectDO = SubjectDOs.createEntity(it)
                val subjectStatusDO = SubjectStatusDOs.createEntity(it)
                val studySiteDO = StudySiteDOs.createEntity(it)
                Triple(subjectDO, subjectStatusDO, studySiteDO)
            }.firstOrNull()
    }

    /**
     * get active Subject, with corresponding SubjectStatus and StudySite for that matches given subject number, site id and study id
     */
    fun getFullSubjectBySubjectNumberAndSiteIdAndStudyId(
        subjectNumber: String,
        siteId: String,
        studyId: String,
    ): Triple<SubjectDO, SubjectStatusDO, StudySiteDO>? {
        return database.from(SubjectDOs).innerJoin(SubjectStatusDOs, SubjectDOs.subjectId eq SubjectStatusDOs.subjectId).innerJoin(
            StudySiteDOs,
            SubjectStatusDOs.studySiteId eq StudySiteDOs.studySiteId,
        ).select().where {
            (SubjectDOs.subjectNumber eq subjectNumber) and
                (StudySiteDOs.siteId eq siteId) and
                (StudySiteDOs.studyId eq studyId) and
                (SubjectStatusDOs.status eq SubjectStatus.ACTIVE)
        }
            .map {
                val subjectDO = SubjectDOs.createEntity(it)
                val subjectStatusDO = SubjectStatusDOs.createEntity(it)
                val studySiteDO = StudySiteDOs.createEntity(it)
                Triple(subjectDO, subjectStatusDO, studySiteDO)
            }.firstOrNull()
    }
}
