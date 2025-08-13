package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule

/**
 * 스터디 그룹 일정의 장소
 * @param type 장소 유형 (온라인, 오프라인)
 * @param name 장소 이름
 * @param url 온라인 장소의 URL (온라인인 경우에만 사용)
 */
class ScheduleLocation(
    val type: LocationType = LocationType.OFFLINE,
    val name: String = "장소",
    val url: String? = null
) {

    enum class LocationType {
        ONLINE,
        OFFLINE
    }
}