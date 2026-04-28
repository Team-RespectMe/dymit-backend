package net.noti_me.dymit.dymit_backend_api.domain.member

import com.fasterxml.jackson.annotation.JsonIgnore
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberDeletedEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberNicknameChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageDeletedEvent
import org.bson.types.ObjectId
import java.time.LocalDateTime

/**
 * 멤버 도메인 엔티티
 * 이 도메인 엔티티를 임베딩 하는 경우 아래 이벤트를 구독하여 일관성을 유지해야 한다.
 * @see net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageChangedEvent
 */
@Document(collection = "members")
@CompoundIndex(name = "oidc_identity_idx", def = "{'oidcIdentities.provider': 1, 'oidcIdentities.subject': 1}", unique = true)
class Member(
    id: ObjectId? = null,
    nickname: String = "",
    oidcIdentities: MutableSet<OidcIdentity> = mutableSetOf(),
    profileImage: MemberProfileImageVo = MemberProfileImageVo(),
    lastAccessAt: LocalDateTime = LocalDateTime.now(),
    deviceTokens: MutableSet<DeviceToken> = mutableSetOf(),
    refreshTokens: MutableSet<RefreshToken> = mutableSetOf(),
    roles: MutableSet<MemberRole> = mutableSetOf(MemberRole.ROLE_MEMBER),
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false,
    interests: MutableSet<String>? = null
) : BaseAggregateRoot<Member>(id, createdAt, updatedAt, isDeleted) {

    val deviceTokens: MutableSet<DeviceToken> = deviceTokens

    @JsonIgnore
    val refreshTokens: MutableSet<RefreshToken> = refreshTokens

    @Indexed(unique = true)
    var nickname: String = nickname
        private set

    @JsonIgnore
    var oidcIdentities: MutableSet<OidcIdentity> = oidcIdentities
        private set

    var profileImage: MemberProfileImageVo = profileImage
        private set

    var lastAccessAt: LocalDateTime = lastAccessAt
        private set

    var roles: MutableSet<MemberRole> = roles
        private set

    var interests: MutableSet<String> = interests ?: mutableSetOf()
        private set

    fun updateInterests(newInterests: Set<String>) {
        this.interests = newInterests.toMutableSet()
        updateLastAccessedAt()
    }

    /**
     * 닉네임 변경 메서드
     * @param newNickname 새로운 닉네임
     * @throws IllegalArgumentException 닉네임이 비어있거나 20자를 초과하는 경우
     */
    fun changeNickname(newNickname: String) {
        require(newNickname.isNotBlank()) { "닉네임은 비워둘 수 없습니다." }
        require(newNickname.length <= 20) { "닉네임은 20자 이내로 설정해야 합니다." }
        if ( newNickname == this.nickname ) {
            return
        }
        this.nickname = newNickname
        registerEvent(MemberNicknameChangedEvent(this))
        updateLastAccessedAt()
    }

    /**
     * 프로필 이미지 변경 메서드
     * @param profileImage 새로운 프로필 이미지
     */
    fun changeProfileImage(profileImage: MemberProfileImageVo) {
        if ( this.profileImage == profileImage ) {
            return
        }
        this.profileImage = profileImage
        registerEvent(MemberProfileImageChangedEvent(this))
        updateLastAccessedAt()
    }

    fun deleteProfileImage() {
        if ( this.profileImage.type == ProfileImageType.EXTERNAL ) {
            val event = MemberProfileImageDeletedEvent(
                thumbnail = this.profileImage.thumbnail,
                original = this.profileImage.original,
                source = this
            )
            registerEvent(event)
        }

        this.profileImage = MemberProfileImageVo(
            type = ProfileImageType.PRESET,
            fileSize = 0L,
            width = 0,
            height = 0
        )

        registerEvent(MemberProfileImageChangedEvent(this))
    }

    fun addDeviceToken(deviceToken: DeviceToken) {
        this.deviceTokens.add(deviceToken)
    }

    fun removeDeviceToken(deviceToken: DeviceToken) {
        this.deviceTokens.remove(deviceToken)
    }

    fun updateLastAccessedAt() {
        this.lastAccessAt = LocalDateTime.now()
    }

    private fun removeExpiredToken() {
        val iter = refreshTokens.iterator()
        while ( iter.hasNext() ) {
            val token = iter.next()
            if ( token.isExpired() ) {
                iter.remove()
            }
        }
    }

    fun addRefreshToken(token: String, expireAt: Instant) {
        removeExpiredToken()
        // 최대 개수가 5개 이상이라면, 가장 오래된 토큰을 제거한다.
        if ( refreshTokens.size >= 5 ) {
            refreshTokens.remove(refreshTokens.first())
        }
        refreshTokens.add(RefreshToken(token, expireAt))
        updateLastAccessedAt()
    }

    fun removeRefreshToken(token: String) {
        refreshTokens.removeIf{ it.token == token }
        updateLastAccessedAt()
    }

    fun leaveService() {
        this.nickname = "탈퇴한_회원_${ObjectId.get().toHexString()}"
        this.oidcIdentities.clear()
        this.deviceTokens.clear()
        this.refreshTokens.clear()
        this.profileImage = MemberProfileImageVo(
            type = ProfileImageType.PRESET,
        )
        super.markAsDeleted()
        updateLastAccessedAt()
        registerEvent(MemberDeletedEvent(this))
    }

    fun assignRole(role: MemberRole) {
        this.roles.add(role)
    }

    fun revokeRole(role: MemberRole) {
        this.roles.remove(role)
    }

    fun updateOidcIdentity(identity: OidcIdentity) {
        val target = this.oidcIdentities.filter{ it == identity }.firstOrNull()
            ?: throw IllegalArgumentException("해당 OIDC 아이덴티티가 존재하지 않습니다. identity: $identity")
        this.oidcIdentities.remove(target)
        this.oidcIdentities.add(identity)
        updateLastAccessedAt()
    }

    fun isAdmin(): Boolean {
        return roles.contains(MemberRole.ROLE_ADMIN)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false
        other as Member

        if (id == null || other.id == null) return false

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}
