package net.noti_me.dymit.dymit_backend_api.domain

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
) : AbstractAggregateRoot<T>() {
    @CreatedDate
    var createdAt: LocalDateTime? = null
        protected set

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
        protected set

    var isDeleted: Boolean = false
        protected set

    @DomainEvents
    public fun listDomainEvents(): Collection<Any> {
        return super.domainEvents();
    }

    public override fun clearDomainEvents() {
        super.clearDomainEvents()
    }

    fun markAsDeleted() {
        if (isDeleted) {
            throw IllegalStateException("Aggregate is already marked as deleted")
        }
        isDeleted = true
    }   
}