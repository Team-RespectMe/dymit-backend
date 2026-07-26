package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.member

import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupProfileImageType
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member.LoadStudyGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.member.dto.StudyGroupMemberData
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Component
class MemberPersistenceAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadStudyGroupMemberPort {

    override fun loadById(memberId: String): StudyGroupMemberData? {
        val member = mongoTemplate.findById(
            ObjectId(memberId),
            Document::class.java,
            MEMBER_COLLECTION
        ) ?: return null
        val profileImage = member["profileImage"] as? Map<*, *>

        return StudyGroupMemberData(
            id = member.getObjectId("_id").toHexString(),
            nickname = member.getString("nickname"),
            profileImageType = profileImage?.get("type").toProfileImageType(),
            profileImageThumbnail = profileImage?.get("thumbnail") as? String ?: "",
            profileImageOriginal = profileImage?.get("original") as? String ?: "",
            roles = (member["roles"] as? Collection<*>)
                .orEmpty()
                .mapNotNull { it?.toString() },
            createdAt = member["createdAt"].toLocalDateTime()
        )
    }

    private fun Any?.toProfileImageType(): StudyGroupProfileImageType =
        when (this) {
            is StudyGroupProfileImageType -> this
            is String -> runCatching { StudyGroupProfileImageType.valueOf(this) }
                .getOrDefault(StudyGroupProfileImageType.PRESET)
            else -> StudyGroupProfileImageType.PRESET
        }

    private fun Any?.toLocalDateTime(): LocalDateTime? =
        when (this) {
            is LocalDateTime -> this
            is Date -> LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())
            is Instant -> LocalDateTime.ofInstant(this, ZoneId.systemDefault())
            else -> null
        }

    companion object {
        private const val MEMBER_COLLECTION = "members"
    }
}
