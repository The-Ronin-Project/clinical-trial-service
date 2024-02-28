package com.projectronin.clinical.trial.server.dataauthority

import com.projectronin.interop.fhir.r4.resource.Patient
import org.springframework.stereotype.Component

@Component
class PatientDAO(
    resourceDatabase: ClinicalTrialDataAuthorityDatabase,
) : BaseCollectionDAO<Patient>(
        resourceDatabase,
        Patient::class.java,
    )
