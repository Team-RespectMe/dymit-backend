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
    createdAt: LocalDateTime? = LocalDateTime.now(),
    updatedAt: LocalDateTime? = LocalDateTime.now(),
    isDeleted: Boolean = false
) : AbstractAggregateRoot<T>() {

    val identifier: String
        get() = id?.toHexString() ?: throw IllegalStateException("Entity ID is null")

    var createdAt: LocalDateTime? = createdAt ?: LocalDateTime.now()
        protected set

    var updatedAt: LocalDateTime? = updatedAt ?: LocalDateTime.now()
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