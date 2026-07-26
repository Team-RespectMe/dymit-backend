package net.noti_me.dymit.dymit_backend_api.study_group.domain.events

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroup
import org.bson.types.ObjectId

data class StudyGroupOwnerChangedEvent(
    val groupId: String,
    val groupName: String,
    val ownerId: ObjectId
) {

    companion object {
        const val EVENT_NAME = "STUDY_GROUP_OWNER_CHANGED"

        fun of(group: StudyGroup) = StudyGroupOwnerChangedEvent(
            groupId = group.identifier,
            groupName = group.name,
            ownerId = group.ownerId
        )
    }
}
