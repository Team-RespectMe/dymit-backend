package net.noti_me.dymit.dymit_backend_api.feed.domain

/**
 * 피드와 연결할 수 있는 리소스 종류입니다.
 */
enum class ResourceType {
    MEMBER,
    STUDY_GROUP,
    TASK,
    STUDY_GROUP_OWNER,
    STUDY_GROUP_MEMBER,
    STUDY_GROUP_SCHEDULE,
    STUDY_GROUP_SCHEDULE_COMMENT,
    STUDY_GROUP_BOARD,
    STUDY_GROUP_POST,
    STUDY_GROUP_POST_COMMENT
}
