package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.GroupFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.LoadFeedGroupMembershipPort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.LoadFeedMemberPort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedQueryHistoryPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeed
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeedQueryHistory
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date

/**
 * 미반영 그룹 피드를 로그인 회원의 개인 피드로 반영합니다.
 *
 * @param loadFeedMemberPort Feed 회원 조회 포트
 * @param loadFeedGroupMembershipPort Feed 그룹 가입 조회 포트
 * @param userFeedPersistencePort 개인 피드 영속성 포트
 * @param groupFeedPersistencePort 그룹 피드 영속성 포트
 * @param queryHistoryPersistencePort 조회 이력 영속성 포트
 */
@Service
class PullUserFeedsService(
    private val loadFeedMemberPort: LoadFeedMemberPort,
    private val loadFeedGroupMembershipPort: LoadFeedGroupMembershipPort,
    private val userFeedPersistencePort: UserFeedPersistencePort,
    private val groupFeedPersistencePort: GroupFeedPersistencePort,
    private val queryHistoryPersistencePort: UserFeedQueryHistoryPersistencePort
) {

    /**
     * 회원이 아직 반영하지 않은 그룹 피드를 모두 개인 피드로 저장합니다.
     *
     * @param memberId 회원 식별자
     */
    fun pull(memberId: String) {
        val memberObjectId = ObjectId(memberId)
        val history = initializeHistory(memberObjectId)
        val groupIds = loadFeedGroupMembershipPort.loadByMemberId(memberId)
            .groupIds
            .map(::ObjectId)

        do {
            val pendingFeeds = groupFeedPersistencePort.findByGroupIdsOrderByIdAsc(
                groupIds = groupIds,
                cursor = history.lastFeedId,
                size = DEFAULT_BATCH_SIZE + 1L
            )
            val hasNext = pendingFeeds.size > DEFAULT_BATCH_SIZE
            val currentBatch = pendingFeeds.take(DEFAULT_BATCH_SIZE.toInt())
            val lastId = currentBatch.lastOrNull()?.id ?: return
            val targets = currentBatch
                .asSequence()
                .filterNot { it.isDeleted || it.excludedMemberIds.contains(memberObjectId) }
                .map { UserFeed.create(memberObjectId, it) }
                .sortedByDescending { it.createdAt }
                .toList()

            // 조회 이력은 중복 방지 장치가 아닙니다. 동일 회원의 pull이 동시에 실행되거나
            // 개인 피드 저장 후 이력 저장이 실패해 재시도되면 같은 그룹 피드가 중복 생성될 수 있습니다.
            if (targets.isNotEmpty()) {
                userFeedPersistencePort.saveAll(targets)
            }
            history.updateLastGroupQueryId(lastId)
            queryHistoryPersistencePort.save(history)
        } while (hasNext)
    }

    private fun initializeHistory(memberId: ObjectId): UserFeedQueryHistory {
        val history = queryHistoryPersistencePort.findByMemberId(memberId)
            ?: queryHistoryPersistencePort.save(
                UserFeedQueryHistory(
                    memberId = memberId,
                    lastFeedId = null
                )
            )

        if (history.lastFeedId == null) {
            val createdAt = loadFeedMemberPort.loadById(memberId.toHexString())?.createdAt
                ?: Instant.now()
            history.updateLastGroupQueryId(ObjectId.getSmallestWithDate(Date.from(createdAt)))
            queryHistoryPersistencePort.save(history)
        }
        return history
    }

    private companion object {
        const val DEFAULT_BATCH_SIZE: Long = 100L
    }
}
