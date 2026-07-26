package net.noti_me.dymit.dymit_backend_api.server_notice.adapter.`out`.member

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.ServerNoticeMemberPort
import net.noti_me.dymit.dymit_backend_api.server_notice.application.port.`out`.member.dto.ServerNoticeMemberDto
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

/**
 * 서버 공지 전용 멤버 조회 포트를 MongoDB에 연결합니다.
 *
 * @param mongoTemplate 멤버 문서 조회용 MongoDB 템플릿
 */
@Component
class MongoServerNoticeMemberAdapter(
    private val mongoTemplate: MongoTemplate
) : ServerNoticeMemberPort {

    override fun loadById(memberId: String): ServerNoticeMemberDto? {
        if ( !ObjectId.isValid(memberId) ) {
            return null
        }

        val member = mongoTemplate.findById(
            ObjectId(memberId),
            Document::class.java,
            COLLECTION_NAME
        ) ?: return null
        val profileImage = member.get("profileImage", Document::class.java)
        val roles = member.getList("roles", String::class.java).orEmpty()

        return ServerNoticeMemberDto(
            id = member.getObjectId("_id"),
            nickname = member.getString("nickname"),
            imageType = ProfileImageType.valueOf(profileImage.getString("type")),
            imageUrl = profileImage.getString("thumbnail"),
            admin = ADMIN_ROLE in roles
        )
    }

    private companion object {
        const val COLLECTION_NAME = "members"
        const val ADMIN_ROLE = "ROLE_ADMIN"
    }
}
