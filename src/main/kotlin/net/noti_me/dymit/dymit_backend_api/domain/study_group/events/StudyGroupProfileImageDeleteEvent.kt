package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import org.springframework.context.ApplicationEvent

/**
 * 도메인 이벤트, 스터디 그룹 프로필 이미지가 삭제된 경우 발행되는 이벤트
 * 이 이벤트를 구독하여 스터디 그룹 프로필 이미지가 삭제된 후처리를 수행할 수 있습니다.
 */
class StudyGroupProfileImageDeleteEvent (
    val thumbnail: String,
    val original: String,
    source: Any
) : ApplicationEvent(source)