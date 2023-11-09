package com.projectronin.clinical.trial.server.services

import com.projectronin.clinical.trial.server.data.StudySiteDAO
import com.projectronin.clinical.trial.server.data.SubjectStatusDAO
import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubjectService(
    val subjectStatusDAO: SubjectStatusDAO,
    val studySiteDAO: StudySiteDAO
) {
    fun getSubjectStatus(subjectId: String, studySiteId: UUID): SubjectStatusDO? {
        return subjectStatusDAO.getSubjectStatusBySubjectId(
            subjectId
        ).filter { it.studySiteId == studySiteId }.firstOrNull()
    }

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
}
