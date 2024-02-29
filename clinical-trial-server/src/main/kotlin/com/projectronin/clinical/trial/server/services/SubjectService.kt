package com.projectronin.clinical.trial.server.services

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.clinicalone.ClinicalOneClient
import com.projectronin.clinical.trial.server.data.StudySiteDAO
import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.clinical.trial.server.data.SubjectStatusDAO
import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import com.projectronin.clinical.trial.server.data.model.SubjectDO
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubjectService(
    val clinicalOneClient: ClinicalOneClient,
    val studySiteDAO: StudySiteDAO,
    val subjectDAO: SubjectDAO,
    val subjectStatusDAO: SubjectStatusDAO,
) {
    fun getSubjectStatus(
        subjectId: String,
        studySiteId: UUID,
    ): SubjectStatusDO? {
        return subjectStatusDAO.getSubjectStatusBySubjectId(
            subjectId,
        ).filter { it.studySiteId == studySiteId }.firstOrNull()
    }

    fun getFhirIdBySubjectId(subjectId: String): String? {
        return subjectDAO.getFhirIdBySubject(subjectId)
    }

    fun getSubjectIdByFhirId(fhirId: String): String? {
        return subjectDAO.getSubjectByFhirId(fhirId)
    }

    fun getSubjectsByRoninFhirId(fhirId: String): Subject? {
        return subjectDAO.getSubjectByRoninFhirId(fhirId)?.let {
            Subject(
                id = it.first.subjectId,
                roninFhirId = it.first.roninPatientId,
                siteId = it.third.siteId,
                status = it.second.status.toString(),
                studyId = it.third.studyId,
                number = it.first.subjectNumber,
            )
        }
    }

    fun createSubject(subject: Subject): Subject? =
        getStudySiteByStudyIdAndSiteId(
            subject.studyId,
            subject.siteId,
        )?.let { studySite ->
            subjectDAO.getFullSubjectByFhirId(subject.roninFhirId)?.let {
                updateSubjectStatus(it.subjectId, studySite.studySiteId, SubjectStatus.ACTIVE)
                return subject.copy(
                    id = it.subjectId,
                    status = SubjectStatus.ACTIVE.toString(),
                    number = it.subjectNumber,
                )
            }

            val (newSubjectId, newSubjectNumber) =
                clinicalOneClient.getSubjectIdAndSubjectNumber(
                    subject.siteId,
                    subject.studyId,
                )

            val newSubject =
                subject.copy(
                    id = newSubjectId,
                    status = SubjectStatus.ACTIVE.toString(),
                    number = newSubjectNumber,
                )

            subjectDAO.insertSubject(newSubject.toSubjectDO())
            insertSubjectStatus(newSubjectId, studySite.studySiteId, SubjectStatus.ACTIVE)
            newSubject
        }

    fun createSubjectWithSubjectNumber(subject: Subject): Subject? {
        val dbSubject = getSubjectBySubjectNumberAndSiteIdAndStudyId(subject.number, subject.siteId, subject.studyId)
        if (dbSubject != null) {
            if (dbSubject.roninFhirId != subject.roninFhirId) {
                throw IllegalArgumentException("Subject number currently bound to different patient")
            } else {
                // This shouldn't be possible through frontend but just in case
                throw IllegalArgumentException("Subject is already bound with this subject number")
            }
        }

        // TODO: call c1 api to see if subject number is valid
        val c1Subject: Subject? = clinicalOneClient.validateSubjectNumber(subject)
        if (c1Subject !== null) {
            val studySite = getStudySiteByStudyIdAndSiteId(c1Subject.studyId, c1Subject.siteId)
            if (studySite != null) {
                subjectDAO.insertSubject(c1Subject.toSubjectDO())
                insertSubjectStatus(c1Subject.id, studySite.studySiteId, SubjectStatus.ACTIVE)
                return c1Subject
            } else {
                throw IllegalArgumentException("Study site not found")
            }
        } else {
            throw IllegalArgumentException("Subject with given subject number not found in Clinical One Trial")
        }
    }

    fun getSubjectBySubjectNumberAndSiteIdAndStudyId(
        subjectNumber: String,
        siteId: String,
        studyId: String,
    ): Subject? {
        return subjectDAO.getFullSubjectBySubjectNumberAndSiteIdAndStudyId(
            subjectNumber,
            siteId,
            studyId,
        )?.let {
            Subject(
                id = it.first.subjectId,
                roninFhirId = it.first.roninPatientId,
                siteId = it.third.siteId,
                status = it.second.status.toString(),
                studyId = it.third.studyId,
                number = it.first.subjectNumber,
            )
        }
    }

    fun updateSubjectStatus(
        subject: Subject,
        studySiteId: UUID,
        subjectStatus: SubjectStatus,
    ) {
        subjectStatusDAO.updateSubjectStatus(
            studySiteId,
            subject.id,
        ) {
            it.status = subjectStatus
        }
    }

    fun getFhirIdsByStatuses(status: List<SubjectStatus>): Set<String> = subjectDAO.getFhirIdsByStatus(status)

    fun getActiveFhirIds(): Set<String> = getFhirIdsByStatuses(listOf(SubjectStatus.ACTIVE, SubjectStatus.ENROLLED))

    fun insertSubjectStatus(
        subjectId: String,
        studySiteId: UUID,
        subjectStatus: SubjectStatus,
    ) {
        val subjectStatusDO = SubjectStatusDO()
        subjectStatusDO["studySiteId"] = studySiteId
        subjectStatusDO["subjectId"] = subjectId
        subjectStatusDO["status"] = subjectStatus
        subjectStatusDAO.insertSubjectStatus(
            subjectStatusDO,
        )
    }

    fun updateSubjectStatus(
        subjectId: String,
        studySiteId: UUID,
        subjectStatus: SubjectStatus,
    ): SubjectStatusDO? {
        return subjectStatusDAO.updateSubjectStatus(
            studySiteId,
            subjectId,
        ) {
            it.status = subjectStatus
        }
    }

    fun getStudySiteByStudyIdAndSiteId(
        studyId: String,
        siteId: String,
    ): StudySiteDO? {
        return studySiteDAO.getStudySiteByStudyIdAndSiteId(studyId, siteId)
    }

    private fun Subject.toSubjectDO(): SubjectDO =
        SubjectDO {
            subjectId = this@toSubjectDO.id
            roninPatientId = this@toSubjectDO.roninFhirId
            subjectNumber = this@toSubjectDO.number
        }
}
