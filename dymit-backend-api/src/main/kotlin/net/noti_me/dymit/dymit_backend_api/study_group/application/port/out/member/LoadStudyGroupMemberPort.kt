package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member.dto.StudyGroupMemberData

interface LoadStudyGroupMemberPort {

    fun loadById(memberId: String): StudyGroupMemberData?
}
