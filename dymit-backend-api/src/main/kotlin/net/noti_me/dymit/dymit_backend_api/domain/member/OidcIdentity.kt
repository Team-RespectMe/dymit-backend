package net.noti_me.dymit.dymit_backend_api.domain.member

data class OidcIdentity(
    val provider: String,
    val subject: String,
    var email: String? = null,
//    val name: String? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OidcIdentity) return false

        if (provider != other.provider) return false
        if (subject != other.subject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = provider.hashCode()
        result = 31 * result + subject.hashCode()
        return result
    }
}