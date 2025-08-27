package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import org.springframework.context.ApplicationEvent

class StudyGroupProfileImageDeleteEvent (
    val studyGroupId: String,
    val filePath: String,
    source: Any
) : ApplicationEvent(source)