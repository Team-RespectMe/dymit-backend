package net.noti_me.dymit.dymit_backend_api.domain.studyGroup.schedule

import org.bson.types.ObjectId

class ScheduleRole(
    val assignee: Assignee,
    val roleName: String = "",
    val roleDescription: String = ""
) {

    class Assignee(
        val memberId: ObjectId = ObjectId.get(),
        val nickname: String = "",
        val profileImage: String = ""
    )
}