package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup

interface SaveStudyGroupPort {

    fun persist(studyGroup: StudyGroup): StudyGroup

    fun update(studyGroup: StudyGroup): StudyGroup

    fun delete(studyGroup: StudyGroup): Boolean
}