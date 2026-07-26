package net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.study_schedule

import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.study_schedule.dto.StudyGroupScheduleData

interface StudyGroupSchedulePort {

    fun loadUpcomingByGroupIds(groupIds: List<String>): Map<String, StudyGroupScheduleData>
}
