package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.events

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.GroupProfileImageVo
import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroup
import org.springframework.context.ApplicationEvent

class StudyGroupProfileImageDeleteEvent (
    val studyGroupId: String,
    val profileImage: GroupProfileImageVo?,
    source: Any
) : ApplicationEvent(source)