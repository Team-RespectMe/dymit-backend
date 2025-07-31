package net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroup

interface SaveStudyGroupPort {

    fun persist(studyGroup: StudyGroup): StudyGroup

    fun update(studyGroup: StudyGroup): StudyGroup

    fun delete(studyGroup: StudyGroup): Boolean
}