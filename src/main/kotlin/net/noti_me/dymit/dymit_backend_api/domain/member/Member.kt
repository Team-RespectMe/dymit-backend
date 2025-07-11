package net.noti_me.dymit.dymit_backend_api.domain.member

import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.springframework.data.annotation.Id

@Document(collection = "members")
@CompoundIndex(name = "oidc_identity_idx", def = "{'oidcIdentities.provider': 1, 'oidcIdentities.subject': 1}", unique = true)
class Member(
    id: String? = null,
    nickname: String = "",
    oidcIdentities: MutableSet<OidcIdentity> = mutableSetOf(),
    profileImage: MemberProfileImageVo? = null,
    lastAccessAt: Instant = Instant.now(),
    deviceTokens: MutableSet<DeviceToken> = mutableSetOf(),
    refreshTokens: MutableSet<String> = mutableSetOf(),
) : BaseAggregateRoot<Member>() {

//    override fun getId(): String? {
//        return memberId
//    }
    @Id
    var id: String? = id
        private set

    val identifier: String
        get() = id ?: throw IllegalStateException("Member ID is not set")

    val deviceTokens: MutableSet<DeviceToken> = deviceTokens

    val refreshTokens: MutableSet<String> = refreshTokens

    @Indexed(unique = true)
    var nickname: String = nickname
        private set

    var oidcIdentities: MutableSet<OidcIdentity> = oidcIdentities
        private set

    var profileImage: MemberProfileImageVo? = profileImage
        private set

    var lastAccessAt: Instant = lastAccessAt
        private set

    fun changeNickname(newNickname: String) {

        if (newNickname.isBlank()) {
            throw IllegalArgumentException("Nickname cannot be blank")
        }

        if ( newNickname == this.nickname ) {
            return
        }

        if ( newNickname.length < 3 || newNickname.length > 20 ) {
            throw IllegalArgumentException("Nickname must be between 3 and 20 characters long")
        }

        this.nickname = newNickname
        updateLastAccessedAt()
    }

    fun updateProfileImage(profileImage: MemberProfileImageVo) {
        if ( !profileImage.isValid() ) {
            throw IllegalArgumentException("Invalid profile image data")
        }
        this.profileImage = profileImage
        updateLastAccessedAt()
    }

    fun deleteProfileImage() {
        this.profileImage = null
    }

    fun addDeviceToken(deviceToken: DeviceToken) {
        this.deviceTokens.add(deviceToken)
    }

    fun removeDeviceToken(deviceToken: DeviceToken) {
        this.deviceTokens.remove(deviceToken)
    }

    fun updateLastAccessedAt() {
        this.lastAccessAt = Instant.now()
    }

    fun addRefreshToken(token: String) {
        if ( refreshTokens.size >= 5 ) {
            refreshTokens.remove(refreshTokens.first());
            // 토큰은 5개까지만 생성 가능하므로 무작위로 하나 삭제한다.
        }
        refreshTokens.add(token)
        updateLastAccessedAt()
    }

    fun removeRefreshToken(token: String) {
        refreshTokens.remove(token)
        updateLastAccessedAt()
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

//    fun markAsDeleted() {
//        if (isDeleted) {
//            throw IllegalStateException("Member is already marked as deleted")
//        }
//        isDeleted = true
//    }
}
