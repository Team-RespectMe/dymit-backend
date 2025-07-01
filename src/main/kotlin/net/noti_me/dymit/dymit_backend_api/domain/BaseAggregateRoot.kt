package net.noti_me.dymit.dymit_backend_api.domain

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.domain.AbstractAggregateRoot
import org.springframework.data.domain.DomainEvents

@Document
abstract class BaseAggregateRoot<T : AbstractAggregateRoot<T>>(
    id: String? = null
) : AbstractAggregateRoot<T>() {

    @Id
    var id: String? = id
        protected set

    val identifier: String
        get() = id ?: throw IllegalStateException("Aggregate ID is not set")

    var isDeleted: Boolean = false
        protected set

    @DomainEvents
    public fun listDomainEvents(): Collection<Any> {
        return super.domainEvents();
    }

    public override fun clearDomainEvents() {
        super.clearDomainEvents()
    }

    @CreatedDate
    lateinit var createdAt: String
        protected set

    @LastModifiedDate
    lateinit var updatedAt: String
        protected set

    fun markAsDeleted() {
        if (isDeleted) {
            throw IllegalStateException("Aggregate is already marked as deleted")
        }
        isDeleted = true
    }   
}