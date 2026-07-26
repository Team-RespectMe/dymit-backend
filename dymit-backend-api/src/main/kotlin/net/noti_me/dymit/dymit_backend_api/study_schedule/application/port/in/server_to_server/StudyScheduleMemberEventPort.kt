package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server

import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto.StudyScheduleMemberEventDto

interface StudyScheduleMemberEventPort {

    fun memberNicknameChanged(event: StudyScheduleMemberEventDto)

    fun memberProfileImageChanged(event: StudyScheduleMemberEventDto)
}
