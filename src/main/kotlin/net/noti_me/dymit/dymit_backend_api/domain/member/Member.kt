package net.noti_me.dymit.dymit_backend_api.domain.member

import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.annotation.PersistenceCreator
import java.time.Instant
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "members")
@CompoundIndex(name = "oidc_identity_idx", def = "{'oidcIdentities.provider': 1, 'oidcIdentities.subject': 1}", unique = true)
class Member(
//    @Id
    id: String? = null,
    nickname: String = "",
    oidcIdentities: MutableSet<OidcIdentity> = mutableSetOf(),
    profileImage: MemberProfileImageVo? = null,
    lastAccessAt: Instant = Instant.now(),
    deviceTokens: MutableSet<DeviceToken> = mutableSetOf(),
    refreshTokens: MutableSet<String> = mutableSetOf(),
//    @CreatedDate
//    var createdAt: Instant? = null,
//    @LastModifiedDate
//    var updatedAt: Instant? = null,
//    var isDeleted: Boolean = false
) : BaseAggregateRoot<Member>() {

//    override fun getId(): String? {
//        return memberId
//    }
    var id: String? = id
        private set

    val identifier: String
        get() = id ?: throw IllegalStateException("Member ID is not set")

    var deviceTokens: MutableSet<DeviceToken> = mutableSetOf()
        private set

    var refreshTokens: MutableSet<String> = mutableSetOf()
        private set

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
        this.deviceTokens.add(deviceToken)
    }

    fun removeDeviceToken(deviceToken: DeviceToken) {
        this.deviceTokens.remove(deviceToken)
    }

    fun addRefreshToken(refreshToken: String) {
        this.refreshTokens.add(refreshToken)
        updateLastAccessedAt()
    }

    fun removeRefreshToken(refreshToken: String) {
        this.refreshTokens.remove(refreshToken)
    }

    fun updateLastAccessedAt() {
        this.lastAccessAt = Instant.now()
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
