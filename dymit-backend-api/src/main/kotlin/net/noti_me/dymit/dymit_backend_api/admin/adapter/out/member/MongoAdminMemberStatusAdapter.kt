package net.noti_me.dymit.dymit_backend_api.admin.adapter.`out`.member

import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.member.AdminMemberStatusPort
import net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.member.dto.AdminMemberStatusDto
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 관리자 회원 현황 포트를 기존 MongoDB 컬렉션에 연결합니다.
 */
@Component
class MongoAdminMemberStatusAdapter(
    private val mongoTemplate: MongoTemplate
) : AdminMemberStatusPort {

    /**
     * 생성 시각 범위에 해당하는 회원 현황을 관리자 소유 DTO로 변환합니다.
     */
    override fun findAllByCreatedAtBetween(
        start: LocalDateTime,
        end: LocalDateTime
    ): List<AdminMemberStatusDto> {
        val query = Query(Criteria.where("createdAt").gte(start).lt(end))
        return mongoTemplate.find(
            query,
            AdminMemberStatusDocument::class.java,
            COLLECTION_NAME
        ).map {
            AdminMemberStatusDto(
                newMemberCount = it.newMemberCount,
                activeMemberCount = it.activeMemberCount,
                leaveMemberCount = it.leaveMemberCount,
                totalMemberCount = it.totalMemberCount,
                createdAt = it.createdAt
            )
        }
    }

    /**
     * 기존 일별 회원 현황 MongoDB 문서의 어댑터 내부 투영입니다.
     */
    private data class AdminMemberStatusDocument(
        val id: ObjectId? = null,
        val newMemberCount: Long = 0L,
        val activeMemberCount: Long = 0L,
        val leaveMemberCount: Long = 0L,
        val totalMemberCount: Long = 0L,
        val createdAt: LocalDateTime = LocalDateTime.now()
    )

    private companion object {
        const val COLLECTION_NAME = "daily_member_status"
    }
}
