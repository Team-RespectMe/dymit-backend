package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.`in`.server_to_server.dto

import org.bson.types.ObjectId
import java.time.Instant

data class StudyScheduleServerDto(
    val id: ObjectId = ObjectId.get(),
    val groupId: ObjectId = ObjectId.get(),
    val title: String = "",
    val session: Long = 1L,
    val scheduleAt: Instant
)
