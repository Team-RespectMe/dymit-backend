package net.noti_me.dymit.dymit_backend_api.domain.member

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.Instant
import net.noti_me.dymit.dymit_backend_api.domain.BaseEntity
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot

@Document(collection = "members")
@CompoundIndex(name = "oidc_identity_idx", def = "{'oidcIdentities.provider': 1, 'oidcIdentities.subject': 1}", unique = true)
class Member(
    id: String? = null,
    nickname: String = "",
    oidcIdentity: OidcIdentity = OidcIdentity(
        provider = "default",
        subject = "default-subject"
    ),
    profile: MemberProfileImageVo? = null,
    lastAccessedAt: Instant = Instant.now()
) : BaseAggregateRoot<Member>(id) {
    
    var nickname: String = nickname
        private set

    var lastAccessedAt: Instant = lastAccessedAt
        private set

    private val _deviceTokens: MutableSet<DeviceToken> = mutableSetOf()
        val deviceTokens: Set<DeviceToken> get() = _deviceTokens.toSet()

    private val _oidcIdentities: MutableSet<OidcIdentity> = mutableSetOf(oidcIdentity)
        val oidcIdentities: Set<OidcIdentity> get() = _oidcIdentities.toSet()

    private val activeRefreshTokens: MutableSet<String> = mutableSetOf()
        val refreshTokens: Set<String> get() = activeRefreshTokens.toSet()

    var profileImage: MemberProfileImageVo? = profile
        private set

    fun changeNickname(newNickname: String) {

        if (newNickname.isBlank()) {
            throw IllegalArgumentException("Nickname cannot be blank")
        }

        if ( newNickname == this.nickname ) {
            println("Nickname is already set to $newNickname, no change needed.")
            return
        }

        if ( newNickname.length < 3 || newNickname.length > 20 ) {
            throw IllegalArgumentException("Nickname must be between 3 and 20 characters long")
        }

        println("Changing nickname from $nickname to $newNickname")
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
        this._deviceTokens.add(deviceToken)
    }

    fun removeDeviceToken(deviceToken: DeviceToken) {
        this._deviceTokens.remove(deviceToken)
    }

    fun addRefreshToken(refreshToken: String) {
        this.activeRefreshTokens.add(refreshToken)
        updateLastAccessedAt()
    }

    fun removeRefreshToken(refreshToken: String) {
        this.activeRefreshTokens.remove(refreshToken)
    }

    fun updateLastAccessedAt() {
        this.lastAccessedAt = Instant.now()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false
        other as Member

        return this.identifier == other.identifier
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

