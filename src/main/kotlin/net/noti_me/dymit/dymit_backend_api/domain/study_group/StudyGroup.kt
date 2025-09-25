package net.noti_me.dymit.dymit_backend_api.domain.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.InviteCodeVo
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyGroupProfileImageDeleteEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.StudyGroupOwnerChangedEvent
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.study_group.events.GroupMemberBlacklistedEvent
import org.bson.types.ObjectId
import java.time.LocalDateTime
import kotlin.random.Random


/**
 * 스터디 그룹 도메인 객체
 * 이 객체는 스터디 그룹의 기본 정보를 포함하며, 그룹 이름, 설명, 소유자 ID, 프로필 이미지 등을 관리합니다.
 * 또한 그룹 이름과 설명을 변경하는 메서드, 소유자를 변경하는 메서드, 프로필 이미지를 삭제하는 메서드를 제공합니다.
 * 도메인 규칙에 따라 그룹 이름과 설명은 특정 길이 제한을 가지며, 소유자 변경은 현재 소유자만 수행할 수 있습니다.
 * 프로필 이미지를 삭제할 때는 이미지가 존재하는 경우에만 삭제할 수 있으며, 삭제 시 이벤트를 발생시킵니다.
 * 이 객체는 스프링 데이터 MongoDB의 문서로 매핑되어 데이터베이스에 저장됩니다.
 * 도메인 이벤트를 사용하여 그룹 소유자 변경 및 프로필 이미지 삭제와 같은 중요한 변경 사항을 다른 서비스에 알릴 수 있습니다.
 * 이 객체는 `BaseAggregateRoot`를 상속받아 도메인 이벤트를 관리합니다.
 * @param id 그룹의 고유 식별자 (선택적)
 * @param ownerId 그룹의 소유자 ID
 * @param boardId 그룹이 속한 게시판 ID
 * @param name 그룹의 이름
 * @param description 그룹의 설명
 * @param profile 그룹의 프로필 이미지 정보 (선택적)
 * @param inviteCode 그룹의 초대 코드 정보 (선택적)
 */
@Document("study_groups")
class StudyGroup(
//    @Id
//    val id: ObjectId = ObjectId.get(),
    id: ObjectId? = null,
    ownerId: ObjectId = ObjectId.get(),
    name: String = "",
    description: String = "",
    memberCount: Int = 0,
    profileImage: GroupProfileImageVo = GroupProfileImageVo(),
    inviteCode: InviteCodeVo = InviteCodeVo(""),
    recentSchedule: RecentScheduleVo? = null,
    recentPost: RecentPostVo? = null,
    blacklists: Set<BlackList> = setOf(),
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
): BaseAggregateRoot<StudyGroup>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    private val blacklists: MutableSet<BlackList> = blacklists.toMutableSet()

//    val identifier: String
//        get() = id.toHexString()

    var description: String = description
        private set

    var name: String = name
        private set

    var ownerId: ObjectId = ownerId
        private set

    var memberCount : Int = memberCount
        private set

    var profileImage: GroupProfileImageVo = profileImage
        private set

    var inviteCode: InviteCodeVo = inviteCode
        private set

    var recentSchedule: RecentScheduleVo? = recentSchedule
        private set

    var recentPost: RecentPostVo? = recentPost
        private set

    /**
     * 그룹 이름을 변경하는 메서드
     * 이 메서드는 현재 그룹 소유자만 호출할 수 있으며, 새로운 그룹 이름이 비어있거나 길이가 3자 이상 30자 이하가 아닐 경우 예외를 발생시킵니다.
     * @param requesterId 요청자의 ID
     * @param newName 새로운 그룹 이름
     * @throws BadRequestException 그룹 이름이 비어있거나 길이가 3자 이상 30자 이하가 아닐 경우
     * @throws ForbiddenException 현재 소유자가 아닌 사용자가 그룹 이름을 변경하려고 할 경우
     */
    fun changeName(requesterId: String, newName: String) {
//        println("requesterId: $requesterId, ownerId: ${this.ownerId.toHexString()}")
        if ( this.ownerId.toHexString() != requesterId ) {
            throw ForbiddenException(message="그룹 소유자만 그룹 이름을 변경할 수 있습니다.")
        }

        if ( newName.isBlank() ) {
            throw BadRequestException(message="그룹 이름은 빈 문자열 일 수 없습니다.")
        }

        if ( newName.length < 1 || newName.length > 30 ) {
            throw BadRequestException(message="그룹 이름은 1자 이상 30자 이하이어야 합니다.")
        }

        this.name = newName
    }

    /**
     * 그룹 설명을 변경하는 메서드
     * 이 메서드는 현재 그룹 소유자만 호출할 수 있으며, 새로운 그룹 설명이 비어있거나 길이가 5자 이상 500자 이하가 아닐 경우
     * 예외를 발생시킵니다.
     * @param requesterId 요청자의 ID
     * @param newDescription 새로운 그룹 설명
     * @throws BadRequestException 그룹 설명이 비어있거나 길이가 5자 이상 500자 이하가 아닐 경우
     * @throws ForbiddenException 현재 소유자가 아닌 사용자가 그룹 설명을 변경하려고 할 경우
     */
    fun changeDescription(requesterId: String, newDescription: String) {
        if ( this.ownerId.toHexString() != requesterId ) {
            throw ForbiddenException(message="그룹 소유자만 그룹 설명을 변경할 수 있습니다.")
        }

        if ( newDescription.isBlank() ) {
            throw BadRequestException(message="스터디 그룹의 설명은 빈 문자열 일 수 없습니다.")
        }

        if ( newDescription.length < 5 || newDescription.length > 500 ) {
            throw BadRequestException(message="스터디 그룹의 설명은 5자 이상 500자 이하이어야 합니다.")
        }

        this.description = newDescription
    }

    /**
     * 그룹 소유자를 변경하는 메서드
     * 이 메서드는 현재 그룹 소유자만 호출할 수 있으며, 새로운 소유자 ID가 비어있거나 현재 소유자와 동일한 경우 예외를 발생시킵니다.
     * 새로운 소유자는 현재 그룹에 속한 멤버여야하며 서비스 레이어에서 검증되어야 합니다.
     * 이 메서드는 `StudyGroupOwnerChangedEvent` 이벤트를 발생시켜 그룹 소유자 변경을 알립니다.
     * @param requester 요청 그룹 멤버
     * @param newOwner 새로운 그룹 소유자 예정 멤버
     */
    fun changeOwner(requester: StudyGroupMember, newOwner: StudyGroupMember) {

        if ( this.ownerId != requester.memberId ) {
            throw ForbiddenException(message="그룹 소유자만 그룹 소유자를 변경할 수 있습니다.")
        }

        if ( requester.memberId == newOwner.memberId ) {
            return;
        }

        if ( newOwner.groupId != this.id ) {
            throw BadRequestException(message="새로운 그룹 소유자는 현재 그룹에 속한 멤버여야 합니다.")
        }

        this.ownerId = newOwner.memberId
        newOwner.changeRole(requester,GroupMemberRole.OWNER)
        requester.changeRole(requester, GroupMemberRole.MEMBER)
        val event = StudyGroupOwnerChangedEvent(this)
        this.registerEvent(event)
    }

    fun updateProfileImage(requesterId: String, profileImage: GroupProfileImageVo) {
        if ( this.ownerId.toHexString() != requesterId ) {
            throw ForbiddenException(message="그룹 소유자가 아닙니다.")
        }

        this.profileImage = profileImage
    }

    /**
     * 프로필 이미지를 삭제하는 메서드
     * 이 메서드는 프로필 이미지가 존재하는 경우에만 호출할 수 있으며,
     * 프로필 이미지가 삭제되면 `StudyGroupProfileImageDeleteEvent` 이벤트를 발생시킵니다.
     * @param requesterId 요청자의 ID
     */
    fun deleteProfileImage(requesterId: String) {
        if ( this.ownerId.toHexString() != requesterId)  {
            throw ForbiddenException(message="그룹 소유자만 프로필 이미지를 삭제할 수 있습니다.")
        }

        if ( this.profileImage.type == "external" ) {
            val event = StudyGroupProfileImageDeleteEvent(this.identifier, profileImage.filePath, this)
            this.registerEvent(event)
        }

        this.profileImage = GroupProfileImageVo(
            filePath = "",
            type = "preset",
            url = Random.nextInt(0, 8).toString(),
        )
    }

    /**
     * 그룹의 초대 코드를 갱신하는 메서드
     */
    fun updateInviteCode(inviteCode: String) {
        if ( inviteCode.length != 8 ) {
            throw IllegalStateException("초대 코드는 8자리 숫자여야 합니다.")
        }

        this.inviteCode = InviteCodeVo(
            code = inviteCode,
            createdAt = LocalDateTime.now(),
            expireAt = LocalDateTime.now().plusDays(30)
        )
    }

    /**
     * 최근 스케줄을 업데이트합니다.
     * 최근 스케줄을 삭제하는 경우 newSchedule을 null 입력합니다.
     * 현재 최신 스케줄이 null이면 newSchedule 을 바로 대입합니다.
     * 현재 최신 스케줄이 존재하는 경우, 새로운 스케줄과 현재 날짜의 차이를 비교하여
     * 더 가까운 날짜의 스케줄을 대입합니다.
     * @param newSchedule 새로운 최근 스케줄 정보
     */
    fun updateRecentSchedule(newSchedule: RecentScheduleVo?) {
        if (newSchedule == null ) {
            this.recentSchedule = null
            return;
        }
        if ( this.recentSchedule == null ) {
            this.recentSchedule = newSchedule
            return
        }
        val now = LocalDateTime.now()

        if (
            newSchedule!!.scheduleAt.isAfter(now) &&
            newSchedule!!.scheduleAt.isBefore( recentSchedule!!.scheduleAt )
        ) {
            this.recentSchedule = newSchedule
        }
    }

    /**
     * 최근 게시글을 업데이트합니다.
     * 최근 게시글을 삭제하는 경우 recentPost를 null 입력합니다.
     * 현재 최신 게시글이 null이면 recentPost 를 바로 대입합니다.
     * 현재 최신 게시글이 존재하는 경우, 새로운 게시글과 현재 날짜의 차
     * 이를 비교하여
     * 더 가까운 날짜의 게시글을 대입합니다.
     * @param recentPost 새로운 최근 게시글 정보
     */
    fun updateRecentPost(recentPost: RecentPostVo?) {
        if (recentPost == null ) {
            this.recentPost = null
            return;
        }
        if ( this.recentPost == null ) {
            this.recentPost = recentPost
            return
        }
        val now = LocalDateTime.now()

        if (
            recentPost!!.createdAt.isAfter(now) &&
            recentPost!!.createdAt.isAfter( this.recentPost!!.createdAt )
        ) {
            this.recentPost = recentPost
        }
    }

    /**
     * 블랙 리스트에 멤버를 추가합니다.
     * 이미 블랙리스트에 존재하는 멤버는 추가할 수 없습니다.
     * @param requester: StudyGroupMember 요청자,
     * @param targetMember: StudyGroupMember 추가할 멤버,
     * @param reason: String 추가 사유
     */
    fun addBlackList(
        requester: StudyGroupMember,
        targetMember: StudyGroupMember,
        reason: String
    ) {
        if ( requester.role != GroupMemberRole.OWNER || targetMember.role == GroupMemberRole.OWNER ) {
            throw ForbiddenException(message="블랙리스트 추가 권한이 없습니다.")
        }

        if ( targetMember.identifier == requester.identifier ) {
            throw BadRequestException(message="본인은 블랙리스트에 추가할 수 없습니다.")
        }

        val blacklist = BlackList(
            memberId = targetMember.memberId,
            nickname = targetMember.nickname,
            reason = reason
        )
        this.blacklists.add(blacklist)
        registerEvent(GroupMemberBlacklistedEvent(this,blacklist))
    }

    /**
     * 블랙 리스트에서 멤버를 제거합니다.
     * 블랙리스트에 존재하지 않는 멤버는 제거할 수 없습니다.
     * @param requester: StudyGroupMember 요청자,
     * @param targetMemberId: String 제거할 멤버 ID
     */
    fun removeBlackList(requester: StudyGroupMember, targetMemberId: String) {
        if ( requester.role != GroupMemberRole.OWNER ) {
            throw ForbiddenException(message="블랙리스트 제거 권한이 없습니다.")
        }

        val blacklisted = BlackList(
            memberId = ObjectId(targetMemberId),
            nickname = "",
            reason = ""
        )
        this.blacklists.remove(blacklisted)
    }

    /**
     * 멤버가 블랙리스트에 존재하는지 확인합니다.
     * @param memberId: String 확인할 멤버 ID
     * @return Boolean 블랙리스트에 존재하면 true, 그렇지 않으면 false
     */
    fun isBlackListed(memberId: String): Boolean {
        val blacklisted = BlackList(
            memberId = ObjectId(memberId),
            nickname = "",
            reason = ""
        )
        return this.blacklists.contains(blacklisted)
    }

    fun getBlacklisted(): Set<BlackList> {
        return this.blacklists.toSet()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StudyGroup) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }   
}