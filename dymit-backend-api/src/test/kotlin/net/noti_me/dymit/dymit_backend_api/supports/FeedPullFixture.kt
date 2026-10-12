package net.noti_me.dymit.dymit_backend_api.supports

import io.mockk.*
import net.noti_me.dymit.dymit_backend_api.feed.application.PullUserFeedsService
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.*
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto.FeedGroupMembershipDto
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto.FeedMemberDto
import net.noti_me.dymit.dymit_backend_api.feed.domain.*
import org.bson.types.ObjectId
import java.time.Instant

/** 외부 DB 없이 pull의 배치 조회와 저장 결과를 관찰하는 fixture입니다. */
class FeedPullFixture {
    val member = ObjectId()
    val group = ObjectId()
    val members = mockk<LoadFeedMemberPort>()
    val memberships = mockk<LoadFeedGroupMembershipPort>()
    val users = mockk<UserFeedPersistencePort>()
    val groups = mockk<GroupFeedPersistencePort>()
    val histories = mockk<UserFeedQueryHistoryPersistencePort>()
    var history: UserFeedQueryHistory? = UserFeedQueryHistory(memberId = member, lastFeedId = ObjectId("000000000000000000000000"))
    val source = mutableListOf<GroupFeed>()
    val saved = mutableListOf<UserFeed>()
    val watermarks = mutableListOf<ObjectId?>()
    val queries = mutableListOf<Pair<ObjectId?, Long>>()
    val events = mutableListOf<String>()
    val pull = PullUserFeedsService(members, memberships, users, groups, histories)

    init {
        every { members.loadById(member.toHexString()) } returns FeedMemberDto(Instant.EPOCH)
        every { memberships.loadByMemberId(member.toHexString()) } returns FeedGroupMembershipDto(listOf(group.toHexString()))
        every { histories.findByMemberId(member) } answers { history }
        every { histories.save(any()) } answers {
            history = firstArg()
            watermarks += history!!.lastFeedId
            events += "history"
            history!!
        }
        every { groups.findByGroupIdsOrderByIdAsc(listOf(group), any(), any()) } answers {
            val cursor = secondArg<ObjectId?>()
            val limit = thirdArg<Long>()
            queries += cursor to limit
            source.filter { cursor == null || it.id!! > cursor }.sortedBy { it.id }.take(limit.toInt())
        }
        every { users.saveAll(any()) } answers {
            saved += firstArg<List<UserFeed>>()
            events += "save"
        }
        every { users.countUnreadByMemberId(member.toHexString()) } answers {
            events += "count"
            saved.count { !it.isRead }.toLong()
        }
        every { users.markAllUnreadAsReadByMemberId(member.toHexString()) } answers {
            events += "read"
            saved.forEach { it.markAsRead() }
        }
    }

    fun add(index: Int, excluded: Boolean = false, deleted: Boolean = false): GroupFeed {
        val row = GroupFeed(
            id = ObjectId(index.toString(16).padStart(24, '0')),
            groupId = group,
            iconType = IconType.NOTICE,
            eventName = "EVENT_$index",
            messages = listOf(FeedMessage("메시지 $index")),
            excludedMemberIds = if (excluded) mutableSetOf(member) else mutableSetOf(),
            createdAt = Instant.EPOCH.plusSeconds(index.toLong()),
            isDeleted = deleted
        )
        source += row
        return row
    }
}
