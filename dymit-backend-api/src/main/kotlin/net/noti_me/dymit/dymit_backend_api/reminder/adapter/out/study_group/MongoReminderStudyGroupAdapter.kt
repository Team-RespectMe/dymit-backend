package net.noti_me.dymit.dymit_backend_api.reminder.adapter.`out`.study_group

import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.ReminderStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.dto.ReminderStudyGroupDto
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

/**
 * Reminder 그룹 조회 포트를 기존 MongoDB 컬렉션에 연결합니다.
 */
@Component
class MongoReminderStudyGroupAdapter(
    private val mongoTemplate: MongoTemplate
) : ReminderStudyGroupPort {

    /**
     * 그룹 문서를 조회해 Reminder 소유 DTO로 변환합니다.
     */
    override fun loadByGroupId(groupId: ObjectId): ReminderStudyGroupDto? {
        val group = mongoTemplate.findById(
            groupId,
            Document::class.java,
            COLLECTION_NAME
        ) ?: return null
        val profileImage = group.get("profileImage", Document::class.java)

        return ReminderStudyGroupDto(
            id = group.getObjectId("_id"),
            ownerId = group.getObjectId("ownerId"),
            name = group.getString("name"),
            profileImageThumbnail = profileImage.getString("thumbnail")
        )
    }

    private companion object {
        const val COLLECTION_NAME = "study_groups"
    }
}
