package net.noti_me.dymit.dymit_backend_api.application.study_schedule.vo

import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.ScheduleLocation

class LocationVo(
    val type: ScheduleLocation.LocationType = ScheduleLocation.LocationType.OFFLINE,
    val value: String = "임의의 장소",
    val link: String? = null
) {

    companion object {

        fun from(location: ScheduleLocation): LocationVo {
            return LocationVo(
                type = location.type,
                value = location.value,
                link = location.link
            )
        }
    }
}