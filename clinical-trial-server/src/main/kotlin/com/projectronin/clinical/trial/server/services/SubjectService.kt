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
    val subjectStatusDAO: SubjectStatusDAO
) {
    fun getSubjectStatus(subjectId: String, studySiteId: UUID): SubjectStatusDO? {
        return subjectStatusDAO.getSubjectStatusBySubjectId(
            subjectId
        ).filter { it.studySiteId == studySiteId }.firstOrNull()
    }

    fun createSubject(subject: Subject): Subject? =
        getStudySiteByStudyIdAndSiteId(
            subject.studyId,
            subject.siteId
        )?.let { studySite ->
            subjectDAO.getSubjectByFhirId(subject.roninFhirId)?.let {
                updateSubjectStatus(it, studySite.studySiteId, SubjectStatus.ACTIVE)
                return subject.copy(id = it, status = SubjectStatus.ACTIVE.toString())
            }

            val newSubjectId = clinicalOneClient.getSubjectId(subject.siteId, subject.studyId)
            val newSubject = subject.copy(id = newSubjectId, status = SubjectStatus.ACTIVE.toString())

            subjectDAO.insertSubject(newSubject.toSubjectDO())
            insertSubjectStatus(newSubjectId, studySite.studySiteId, SubjectStatus.ACTIVE)
            newSubject
        }

    fun updateSubjectStatus(subject: Subject, studySiteId: UUID, subjectStatus: SubjectStatus) {
        subjectStatusDAO.updateSubjectStatus(
            studySiteId,
            subject.id
        ) {
            it.status = subjectStatus
        }
    }

    fun getFhirIdsByStatuses(status: List<SubjectStatus>): Set<String> =
        subjectDAO.getFhirIdsByStatus(status)

    fun getActiveFhirIds(): Set<String> =
        getFhirIdsByStatuses(listOf(SubjectStatus.ACTIVE, SubjectStatus.ENROLLED))

    fun insertSubjectStatus(subjectId: String, studySiteId: UUID, subjectStatus: SubjectStatus) {
        val subjectStatusDO = SubjectStatusDO()
        subjectStatusDO["studySiteId"] = studySiteId
        subjectStatusDO["subjectId"] = subjectId
        subjectStatusDO["status"] = subjectStatus
        subjectStatusDAO.insertSubjectStatus(
            subjectStatusDO
        )
    }

    fun updateSubjectStatus(subjectId: String, studySiteId: UUID, subjectStatus: SubjectStatus): SubjectStatusDO? {
        return subjectStatusDAO.updateSubjectStatus(
            studySiteId,
            subjectId
        ) {
            it.status = subjectStatus
        }
    }

    fun getStudySiteByStudyIdAndSiteId(studyId: String, siteId: String): StudySiteDO? {
        return studySiteDAO.getStudySiteByStudyIdAndSiteId(studyId, siteId)
    }

    private fun Subject.toSubjectDO(): SubjectDO =
        SubjectDO {
            subjectId = this@toSubjectDO.id
            roninPatientId = this@toSubjectDO.roninFhirId
        }
}
