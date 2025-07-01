package net.noti_me.dymit.dymit_backend_api.domain

import org.springframework.data.annotation.Id

abstract class BaseEntity(
    id: String? = null
) {
    @Id
    open var id: String? = id
        protected set

    protected var isDeleted: Boolean = false
        private set

    val identifier: String
        get() = id ?: throw IllegalStateException("Entity ID is not set")

    fun markAsDeleted() {
        if (isDeleted) {
            throw IllegalStateException("Entity is already marked as deleted")
        }
        isDeleted = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseEntity) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}