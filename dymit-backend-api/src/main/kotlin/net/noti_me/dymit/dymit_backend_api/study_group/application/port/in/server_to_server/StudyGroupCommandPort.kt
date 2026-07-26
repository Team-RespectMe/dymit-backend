package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto

interface StudyGroupCommandPort {

    fun persist(studyGroup: StudyGroupDto): StudyGroupDto

    fun update(studyGroup: StudyGroupDto): StudyGroupDto

    fun delete(studyGroup: StudyGroupDto): Boolean
}
