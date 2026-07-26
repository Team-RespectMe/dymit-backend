package net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberEventDto

interface StudyGroupMemberEventPort {

    fun memberCreated(member: StudyGroupMemberEventDto)

    fun memberDeleted(member: StudyGroupMemberEventDto)

    fun memberForceDeleted(memberId: String)

    fun memberNicknameChanged(memberId: String, nickname: String)

    fun memberProfileImageChanged(member: StudyGroupMemberEventDto)
}
