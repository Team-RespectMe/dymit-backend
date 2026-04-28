package net.noti_me.dymit.dymit_backend_api.domain.member

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "daily_member_status")
class DailyMemberStatus(
    val id: ObjectId? = null,
    val newMemberCount: Long = 0L,
    val activeMemberCount: Long = 0L,
    val leaveMemberCount: Long = 0L,
    val totalMemberCount: Long = 0L,
    @CreatedDate
    @Indexed
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

}