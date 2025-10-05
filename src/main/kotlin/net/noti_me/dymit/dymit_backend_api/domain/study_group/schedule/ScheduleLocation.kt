package net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule

import net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo.LocationVo

/**
 * 스터디 그룹 일정의 장소
 * @param type 장소 유형 (온라인, 오프라인)
 * @param value 장소 이름 또는 URL
 */
class ScheduleLocation(
    val type: LocationType = LocationType.OFFLINE,
    val value: String = "",
    val link: String? = null
) {

    companion object {
        fun from(vo: LocationVo): ScheduleLocation {
            return ScheduleLocation(
                type = vo.type,
                value = vo.value,
                link = vo.link
            )
        }
    }

    enum class LocationType {
        ONLINE,
        OFFLINE
    }
}