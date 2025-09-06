package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.domain.study_group.BlackList
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import org.springframework.context.ApplicationEvent

class GroupMemberBlacklistedEvent(
    val group: StudyGroup,
    val blacklisted: BlackList
): ApplicationEvent(blacklisted) {

}