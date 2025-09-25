package net.noti_me.dymit.dymit_backend_api.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.AbstractAggregateRoot
import org.springframework.data.domain.DomainEvents
import org.springframework.data.domain.Persistable
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Document
abstract class BaseAggregateRoot<T : AbstractAggregateRoot<T>>(
    @Id
    @Indexed(unique = true)
    val id: ObjectId? = null,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
) : AbstractAggregateRoot<T>() {

    val identifier: String
        get() = id?.toHexString() ?: throw IllegalStateException("Entity ID is null")

    @CreatedDate
    var createdAt: LocalDateTime? = createdAt
        protected set

    @LastModifiedDate
    var updatedAt: LocalDateTime? = updatedAt
        protected set

    var isDeleted: Boolean = isDeleted
        protected set

    @DomainEvents
    public fun listDomainEvents(): Collection<Any> {
        return super.domainEvents();
    }

    public override fun clearDomainEvents() {
        super.clearDomainEvents()
    }

    fun markAsDeleted() {
        isDeleted = true
    }   
}